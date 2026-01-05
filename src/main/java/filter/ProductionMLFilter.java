package filter;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * Production ML-Based Threat Detection Filter
 * Calls Python ML service for real predictions using trained models
 * 
 * @author TAMIL MUGHILAN
 */
public class ProductionMLFilter implements Filter {
    
    private static final String ML_SERVICE_URL = "http://localhost:5000/predict";
    private static final int ML_TIMEOUT_MS = 200; // Max 200ms for ML prediction
    
    private static final List<ThreatDetectionLog> DETECTION_LOGS = 
        Collections.synchronizedList(new ArrayList<>());
    private static final int MAX_LOGS = 2000;
    
    private ExecutorService mlExecutor;
    private Gson gson;
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("=".repeat(80));
        System.out.println("🤖 PRODUCTION ML THREAT DETECTION SYSTEM");
        System.out.println("   Models: LSTM + XGBoost + Isolation Forest");
        System.out.println("   Python Service: " + ML_SERVICE_URL);
        System.out.println("   Mode: Real-time Interception with Trained Models");
        System.out.println("=".repeat(80));
        
        mlExecutor = Executors.newFixedThreadPool(8);
        gson = new Gson();
        
        // Test ML service connectivity
        testMLService();
    }
    
    private void testMLService() {
        try {
            URL url = new URL("http://localhost:5000/health");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(2000);
            
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                System.out.println("✓ ML Service is running and healthy");
            } else {
                System.out.println("⚠ ML Service returned status: " + responseCode);
            }
        } catch (Exception e) {
            System.out.println("⚠ WARNING: ML Service not reachable at " + ML_SERVICE_URL);
            System.out.println("   Make sure Python ML service is running: python ml_predictor.py");
        }
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String path = httpRequest.getServletPath();
        String uri = httpRequest.getRequestURI();
        
        // Skip static resources
        if (isStaticResource(path) || isStaticResource(uri)) {
            chain.doFilter(request, response);
            return;
        }
        
        long startTime = System.currentTimeMillis();
        
        // Extract features
        JsonObject features = extractFeatures(httpRequest);
        
        // Call ML service asynchronously
        Future<MLPrediction> predictionFuture = mlExecutor.submit(() -> 
            callMLService(features)
        );
        
        int statusCode = 200;
        boolean requestBlocked = false;
        MLPrediction prediction = null;
        
        try {
            // Wait for ML prediction (with timeout)
            prediction = predictionFuture.get(ML_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            
            // CRITICAL DECISION: Block high-confidence threats
            if (prediction.shouldBlock || prediction.ensembleScore > 0.85) {
                requestBlocked = true;
                statusCode = 403;
                
                httpResponse.setStatus(403);
                httpResponse.setContentType("application/json");
                httpResponse.getWriter().write(String.format(
                    "{\"error\":\"Blocked by ML threat detection\"," +
                    "\"threat_level\":\"%s\"," +
                    "\"ensemble_score\":%.4f," +
                    "\"lstm\":%.4f," +
                    "\"xgboost\":%.4f," +
                    "\"isolation_forest\":%.4f}",
                    prediction.threatLevel,
                    prediction.ensembleScore,
                    prediction.lstmScore,
                    prediction.xgboostScore,
                    prediction.isolationForestScore
                ));
                
                System.out.println(String.format(
                    "[🚨 BLOCKED] %s %s | Score: %.4f | Level: %s | LSTM: %.4f | XGB: %.4f | ISO: %.4f",
                    httpRequest.getMethod(), uri,
                    prediction.ensembleScore,
                    prediction.threatLevel,
                    prediction.lstmScore,
                    prediction.xgboostScore,
                    prediction.isolationForestScore
                ));
            } else {
                // Allow request
                chain.doFilter(request, response);
                statusCode = httpResponse.getStatus();
                
                if ("HIGH".equals(prediction.threatLevel) || "MEDIUM".equals(prediction.threatLevel)) {
                    System.out.println(String.format(
                        "[⚠️  ALLOWED] %s %s | Score: %.4f | Level: %s (Under threshold)",
                        httpRequest.getMethod(), uri,
                        prediction.ensembleScore,
                        prediction.threatLevel
                    ));
                }
            }
            
            // Log detection
            long processingTime = System.currentTimeMillis() - startTime;
            logDetection(httpRequest, statusCode, processingTime, prediction, requestBlocked);
            
        } catch (TimeoutException e) {
            // ML timeout - fail open (allow request)
            System.out.println("[⏱️  TIMEOUT] ML prediction timeout, allowing: " + uri);
            chain.doFilter(request, response);
            statusCode = httpResponse.getStatus();
            
            // Log with default prediction
            prediction = new MLPrediction();
            prediction.ensembleScore = 0.5;
            prediction.threatLevel = "UNKNOWN";
            logDetection(httpRequest, statusCode, System.currentTimeMillis() - startTime, 
                        prediction, false);
            
        } catch (Exception e) {
            // ML error - fail open
            System.err.println("[❌ ERROR] ML service error, allowing: " + e.getMessage());
            chain.doFilter(request, response);
            statusCode = httpResponse.getStatus();
            
            prediction = new MLPrediction();
            prediction.ensembleScore = 0.5;
            prediction.threatLevel = "ERROR";
            logDetection(httpRequest, statusCode, System.currentTimeMillis() - startTime, 
                        prediction, false);
        }
    }
    
    /**
     * Call Python ML service for prediction
     */
    private MLPrediction callMLService(JsonObject features) {
        try {
            URL url = new URL(ML_SERVICE_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(ML_TIMEOUT_MS);
            conn.setReadTimeout(ML_TIMEOUT_MS);
            
            // Send features
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = gson.toJson(features).getBytes("utf-8");
                os.write(input, 0, input.length);
            }
            
            // Read response
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "utf-8"));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line.trim());
                }
                
                // Parse ML prediction
                return gson.fromJson(response.toString(), MLPrediction.class);
            } else {
                System.err.println("ML service returned error: " + responseCode);
                return getDefaultPrediction();
            }
            
        } catch (Exception e) {
            System.err.println("Error calling ML service: " + e.getMessage());
            return getDefaultPrediction();
        }
    }
    
    private MLPrediction getDefaultPrediction() {
        MLPrediction pred = new MLPrediction();
        pred.ensembleScore = 0.5;
        pred.lstmScore = 0.5;
        pred.xgboostScore = 0.5;
        pred.isolationForestScore = 0.5;
        pred.threatLevel = "UNKNOWN";
        pred.confidence = 0.0;
        pred.shouldBlock = false;
        return pred;
    }
    
    /**
     * Extract features for ML model
     */
    private JsonObject extractFeatures(HttpServletRequest request) {
        JsonObject features = new JsonObject();
        
        // Basic request info
        features.addProperty("timestamp", System.currentTimeMillis());
        features.addProperty("method", request.getMethod());
        features.addProperty("path", request.getRequestURI());
        features.addProperty("queryString", request.getQueryString());
        features.addProperty("clientIp", request.getRemoteAddr());
        features.addProperty("userAgent", request.getHeader("User-Agent"));
        
        // Network flow features (estimated)
        features.addProperty("flowDuration", 0);
        features.addProperty("packetCount", estimatePacketCount(request));
        features.addProperty("bytesTransferred", estimateBytesTransferred(request));
        features.addProperty("requestRate", calculateRequestRate(request.getRemoteAddr()));
        
        // API sequence features
        List<String> apiSequence = getAPISequenceFromSession(request);
        features.add("apiSequence", gson.toJsonTree(apiSequence));
        features.addProperty("interAPIAccessDuration", calculateInterAPIAccessDuration(request));
        features.addProperty("apiAccessUniqueness", calculateAPIUniqueness(request));
        features.addProperty("sequenceLength", apiSequence.size());
        
        return features;
    }
    
    private int estimatePacketCount(HttpServletRequest request) {
        int contentLength = request.getContentLength();
        int totalBytes = contentLength > 0 ? contentLength + 500 : 500;
        return (int) Math.ceil(totalBytes / 1460.0);
    }
    
    private int estimateBytesTransferred(HttpServletRequest request) {
        int contentLength = request.getContentLength();
        return contentLength > 0 ? contentLength + 500 : 500;
    }
    
    private double calculateRequestRate(String clientIp) {
        long now = System.currentTimeMillis();
        long oneMinuteAgo = now - 60000;
        
        synchronized (DETECTION_LOGS) {
            long count = DETECTION_LOGS.stream()
                .filter(log -> log.clientIp.equals(clientIp))
                .filter(log -> log.timestampMillis > oneMinuteAgo)
                .count();
            return count / 60.0;
        }
    }
    
    private List<String> getAPISequenceFromSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return new ArrayList<>();
        }
        
        @SuppressWarnings("unchecked")
        List<String> sequence = (List<String>) session.getAttribute("api_sequence");
        if (sequence == null) {
            sequence = new ArrayList<>();
            session.setAttribute("api_sequence", sequence);
        }
        
        String endpoint = request.getServletPath();
        sequence.add(endpoint);
        
        if (sequence.size() > 100) {
            sequence = new ArrayList<>(sequence.subList(sequence.size() - 100, sequence.size()));
            session.setAttribute("api_sequence", sequence);
        }
        
        return new ArrayList<>(sequence);
    }
    
    private double calculateInterAPIAccessDuration(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return 0.0;
        
        Long lastAccessTime = (Long) session.getAttribute("last_api_access_time");
        long currentTime = System.currentTimeMillis();
        session.setAttribute("last_api_access_time", currentTime);
        
        if (lastAccessTime == null) return 0.0;
        return (currentTime - lastAccessTime) / 1000.0;
    }
    
    private double calculateAPIUniqueness(HttpServletRequest request) {
        List<String> sequence = getAPISequenceFromSession(request);
        if (sequence.isEmpty()) return 0.0;
        
        Set<String> uniqueApis = new HashSet<>(sequence);
        return (double) uniqueApis.size() / sequence.size();
    }
    
    private boolean isStaticResource(String path) {
        if (path == null) return false;
        String lower = path.toLowerCase();
        return lower.endsWith(".css") || lower.endsWith(".js") || 
               lower.endsWith(".png") || lower.endsWith(".jpg") || 
               lower.endsWith(".ico") || lower.endsWith(".svg");
    }
    
    private void logDetection(HttpServletRequest request, int statusCode, 
                             long processingTime, MLPrediction prediction, 
                             boolean blocked) {
        ThreatDetectionLog log = new ThreatDetectionLog();
        log.timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        log.timestampMillis = System.currentTimeMillis();
        log.method = request.getMethod();
        log.path = request.getRequestURI();
        log.queryString = request.getQueryString();
        log.clientIp = request.getRemoteAddr();
        log.statusCode = statusCode;
        log.processingTime = processingTime;
        log.mlProcessingTime = 50; // Approximate
        log.blocked = blocked;
        log.ensembleScore = prediction.ensembleScore;
        log.lstmScore = prediction.lstmScore;
        log.xgboostScore = prediction.xgboostScore;
        log.isolationForestScore = prediction.isolationForestScore;
        log.threatLevel = prediction.threatLevel;
        log.confidence = prediction.confidence;
        
        synchronized (DETECTION_LOGS) {
            DETECTION_LOGS.add(log);
            if (DETECTION_LOGS.size() > MAX_LOGS) {
                DETECTION_LOGS.remove(0);
            }
        }
    }
    
    public static List<ThreatDetectionLog> getLogs() {
        synchronized (DETECTION_LOGS) {
            return new ArrayList<>(DETECTION_LOGS);
        }
    }
    
    public static DetectionStats getStats() {
        synchronized (DETECTION_LOGS) {
            DetectionStats stats = new DetectionStats();
            stats.totalRequests = DETECTION_LOGS.size();
            stats.blockedRequests = (int) DETECTION_LOGS.stream().filter(l -> l.blocked).count();
            stats.allowedRequests = stats.totalRequests - stats.blockedRequests;
            
            stats.highThreatCount = (int) DETECTION_LOGS.stream()
                .filter(l -> "HIGH".equals(l.threatLevel)).count();
            stats.mediumThreatCount = (int) DETECTION_LOGS.stream()
                .filter(l -> "MEDIUM".equals(l.threatLevel)).count();
            stats.lowThreatCount = (int) DETECTION_LOGS.stream()
                .filter(l -> "LOW".equals(l.threatLevel)).count();
            
            if (stats.totalRequests > 0) {
                stats.avgProcessingTime = DETECTION_LOGS.stream()
                    .mapToLong(l -> l.processingTime).average().orElse(0);
                stats.avgMLProcessingTime = DETECTION_LOGS.stream()
                    .mapToLong(l -> l.mlProcessingTime).average().orElse(0);
                stats.avgThreatScore = DETECTION_LOGS.stream()
                    .mapToDouble(l -> l.ensembleScore).average().orElse(0);
            }
            
            return stats;
        }
    }
    
    @Override
    public void destroy() {
        if (mlExecutor != null) {
            mlExecutor.shutdown();
        }
        DETECTION_LOGS.clear();
    }
    
    // Inner classes
    public static class MLPrediction {
        public double ensembleScore;
        public double lstmScore;
        public double xgboostScore;
        public double isolationForestScore;
        public String threatLevel;
        public double confidence;
        public boolean shouldBlock;
    }
    
    public static class ThreatDetectionLog {
        public String timestamp;
        public long timestampMillis;
        public String method;
        public String path;
        public String queryString;
        public String clientIp;
        public int statusCode;
        public long processingTime;
        public long mlProcessingTime;
        public boolean blocked;
        public double ensembleScore;
        public double lstmScore;
        public double xgboostScore;
        public double isolationForestScore;
        public String threatLevel;
        public double confidence;
    }
    
    public static class DetectionStats {
        public int totalRequests;
        public int blockedRequests;
        public int allowedRequests;
        public int highThreatCount;
        public int mediumThreatCount;
        public int lowThreatCount;
        public double avgProcessingTime;
        public double avgMLProcessingTime;
        public double avgThreatScore;
    }
}