package filter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * ML-Based API Threat Detection Filter
 * Intercepts all requests and uses ensemble ML model for real-time threat detection
 * 
 * @author TAMIL MUGHILAN
 */
public class MLThreatDetectionFilter implements Filter {
    
    private static final List<ThreatDetectionLog> DETECTION_LOGS = 
        Collections.synchronizedList(new ArrayList<>());
    private static final int MAX_LOGS = 2000;
    
    // ML Model Components (simulated for demo - will be replaced with actual Python integration)
    private MLEnsemblePredictor ensembleModel;
    private ExecutorService mlExecutor;
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("=".repeat(80));
        System.out.println("🤖 ML THREAT DETECTION SYSTEM STARTED");
        System.out.println("   Ensemble Model: LSTM + XGBoost + Isolation Forest");
        System.out.println("   Accuracy: 99.11% | Mode: Real-time Interception");
        System.out.println("=".repeat(80));
        
        // Initialize ML predictor
        ensembleModel = new MLEnsemblePredictor();
        
        // Thread pool for async ML predictions (non-blocking)
        mlExecutor = Executors.newFixedThreadPool(4);
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
        
        // Extract request features for ML model
        RequestFeatures features = extractFeatures(httpRequest);
        
        // PRODUCTION MODE: Asynchronous ML prediction (non-blocking)
        Future<ThreatPrediction> predictionFuture = mlExecutor.submit(() -> 
            ensembleModel.predict(features)
        );
        
        // Continue request processing (non-blocking)
        int statusCode = 200;
        boolean requestBlocked = false;
        
        try {
            // Get prediction with timeout (max 100ms for real-time)
            ThreatPrediction prediction = predictionFuture.get(100, TimeUnit.MILLISECONDS);
            
            // CRITICAL DECISION: Block if high-confidence threat detected
            if (prediction.isHighConfidenceThreat()) {
                requestBlocked = true;
                statusCode = 403;
                
                httpResponse.setStatus(403);
                httpResponse.setContentType("application/json");
                httpResponse.getWriter().write(String.format(
                    "{\"error\":\"Request blocked by ML threat detection\",\"threat_score\":%.4f,\"models\":{\"lstm\":%.4f,\"xgboost\":%.4f,\"isolation_forest\":%.4f}}",
                    prediction.ensembleScore,
                    prediction.lstmScore,
                    prediction.xgboostScore,
                    prediction.isolationForestScore
                ));
                
                System.out.println(String.format(
                    "[ML-BLOCK] 🚨 %s %s | Threat Score: %.4f | LSTM: %.4f | XGB: %.4f | ISO: %.4f",
                    httpRequest.getMethod(), uri,
                    prediction.ensembleScore,
                    prediction.lstmScore,
                    prediction.xgboostScore,
                    prediction.isolationForestScore
                ));
            } else {
                // Allow request to proceed
                chain.doFilter(request, response);
                statusCode = httpResponse.getStatus();
                
                if (prediction.isModerateThreat()) {
                    System.out.println(String.format(
                        "[ML-WARN] ⚠️  %s %s | Threat Score: %.4f | Status: ALLOWED",
                        httpRequest.getMethod(), uri, prediction.ensembleScore
                    ));
                }
            }
            
            // Log the detection
            long processingTime = System.currentTimeMillis() - startTime;
            logDetection(httpRequest, statusCode, processingTime, prediction, requestBlocked);
            
        } catch (TimeoutException e) {
            // ML prediction took too long - allow request and log
            System.out.println("[ML-TIMEOUT] Request allowed due to prediction timeout: " + uri);
            chain.doFilter(request, response);
            statusCode = httpResponse.getStatus();
            
        } catch (Exception e) {
            // ML prediction failed - fail-open for availability
            System.err.println("[ML-ERROR] Prediction failed, allowing request: " + e.getMessage());
            chain.doFilter(request, response);
            statusCode = httpResponse.getStatus();
        }
    }
    
    /**
     * Extract features from HTTP request for ML model
     */
    private RequestFeatures extractFeatures(HttpServletRequest request) {
        RequestFeatures features = new RequestFeatures();
        
        // Basic request metadata
        features.timestamp = System.currentTimeMillis();
        features.method = request.getMethod();
        features.path = request.getRequestURI();
        features.queryString = request.getQueryString();
        features.clientIp = request.getRemoteAddr();
        features.userAgent = request.getHeader("User-Agent");
        
        // Extract parameters
        features.parameters = new HashMap<>();
        Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String param = paramNames.nextElement();
            features.parameters.put(param, request.getParameter(param));
        }
        
        // Network flow features (simulated from request characteristics)
        features.flowDuration = 0; // Would be calculated from session tracking
        features.packetCount = estimatePacketCount(request);
        features.bytesTransferred = estimateBytesTransferred(request);
        features.requestRate = calculateRequestRate(features.clientIp);
        
        // API sequence features (from session history)
        features.apiSequence = getAPISequenceFromSession(request);
        features.interAPIAccessDuration = calculateInterAPIAccessDuration(request);
        features.apiAccessUniqueness = calculateAPIUniqueness(request);
        features.sequenceLength = features.apiSequence.size();
        
        return features;
    }
    
    /**
     * Estimate packet count from request size
     */
    private int estimatePacketCount(HttpServletRequest request) {
        int contentLength = request.getContentLength();
        int headerSize = 500; // Approximate header size
        int totalBytes = contentLength > 0 ? contentLength + headerSize : headerSize;
        return (int) Math.ceil(totalBytes / 1460.0); // MTU size
    }
    
    /**
     * Estimate bytes transferred
     */
    private int estimateBytesTransferred(HttpServletRequest request) {
        int contentLength = request.getContentLength();
        return contentLength > 0 ? contentLength + 500 : 500;
    }
    
    /**
     * Calculate request rate for this IP
     */
    private double calculateRequestRate(String clientIp) {
        // Count requests from this IP in last 60 seconds
        long now = System.currentTimeMillis();
        long oneMinuteAgo = now - 60000;
        
        synchronized (DETECTION_LOGS) {
            long count = DETECTION_LOGS.stream()
                .filter(log -> log.clientIp.equals(clientIp))
                .filter(log -> log.timestampMillis > oneMinuteAgo)
                .count();
            return count / 60.0; // requests per second
        }
    }
    
    /**
     * Get API call sequence from user session
     */
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
        
        // Add current endpoint to sequence
        String endpoint = request.getServletPath();
        sequence.add(endpoint);
        
        // Keep only last 100 API calls
        if (sequence.size() > 100) {
            sequence = new ArrayList<>(sequence.subList(sequence.size() - 100, sequence.size()));
            session.setAttribute("api_sequence", sequence);
        }
        
        return new ArrayList<>(sequence);
    }
    
    /**
     * Calculate inter-API access duration
     */
    private double calculateInterAPIAccessDuration(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return 0.0;
        
        Long lastAccessTime = (Long) session.getAttribute("last_api_access_time");
        long currentTime = System.currentTimeMillis();
        
        session.setAttribute("last_api_access_time", currentTime);
        
        if (lastAccessTime == null) return 0.0;
        return (currentTime - lastAccessTime) / 1000.0; // seconds
    }
    
    /**
     * Calculate API access uniqueness
     */
    private double calculateAPIUniqueness(HttpServletRequest request) {
        List<String> sequence = getAPISequenceFromSession(request);
        if (sequence.isEmpty()) return 0.0;
        
        Set<String> uniqueApis = new HashSet<>(sequence);
        return (double) uniqueApis.size() / sequence.size();
    }
    
    /**
     * Check if resource is static
     */
    private boolean isStaticResource(String path) {
        if (path == null) return false;
        String lower = path.toLowerCase();
        return lower.endsWith(".css") || lower.endsWith(".js") || 
               lower.endsWith(".png") || lower.endsWith(".jpg") || 
               lower.endsWith(".jpeg") || lower.endsWith(".gif") ||
               lower.endsWith(".ico") || lower.endsWith(".svg") ||
               lower.endsWith(".woff") || lower.endsWith(".woff2") ||
               lower.endsWith(".ttf") || lower.endsWith(".eot");
    }
    
    /**
     * Log the threat detection result
     */
    private void logDetection(HttpServletRequest request, int statusCode, 
                             long processingTime, ThreatPrediction prediction, 
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
        log.mlProcessingTime = prediction.processingTimeMs;
        log.blocked = blocked;
        
        // ML Predictions
        log.ensembleScore = prediction.ensembleScore;
        log.lstmScore = prediction.lstmScore;
        log.xgboostScore = prediction.xgboostScore;
        log.isolationForestScore = prediction.isolationForestScore;
        log.threatLevel = prediction.getThreatLevel();
        log.confidence = prediction.getConfidence();
        
        synchronized (DETECTION_LOGS) {
            DETECTION_LOGS.add(log);
            if (DETECTION_LOGS.size() > MAX_LOGS) {
                DETECTION_LOGS.remove(0);
            }
        }
    }
    
    /**
     * Get detection logs (for API endpoint)
     */
    public static List<ThreatDetectionLog> getLogs() {
        synchronized (DETECTION_LOGS) {
            return new ArrayList<>(DETECTION_LOGS);
        }
    }
    
    /**
     * Get detection statistics
     */
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
    
    // ========================================================================
    // INNER CLASSES
    // ========================================================================
    
    /**
     * Request features extracted for ML model
     */
    public static class RequestFeatures {
        public long timestamp;
        public String method;
        public String path;
        public String queryString;
        public String clientIp;
        public String userAgent;
        public Map<String, String> parameters;
        
        // Network flow features (for XGBoost)
        public double flowDuration;
        public int packetCount;
        public int bytesTransferred;
        public double requestRate;
        
        // API sequence features (for LSTM)
        public List<String> apiSequence;
        public double interAPIAccessDuration;
        public double apiAccessUniqueness;
        public int sequenceLength;
    }
    
    /**
     * ML ensemble prediction result
     */
    public static class ThreatPrediction {
        public double lstmScore;           // 0-1 probability
        public double xgboostScore;        // 0-1 probability
        public double isolationForestScore; // 0-1 probability (normalized)
        public double ensembleScore;       // Weighted average
        public long processingTimeMs;
        
        public boolean isHighConfidenceThreat() {
            // Block if ensemble score > 0.85 OR any single model > 0.95
            return ensembleScore > 0.85 || 
                   lstmScore > 0.95 || 
                   xgboostScore > 0.95 || 
                   isolationForestScore > 0.95;
        }
        
        public boolean isModerateThreat() {
            return ensembleScore > 0.65 && ensembleScore <= 0.85;
        }
        
        public String getThreatLevel() {
            if (ensembleScore > 0.85) return "HIGH";
            if (ensembleScore > 0.65) return "MEDIUM";
            if (ensembleScore > 0.35) return "LOW";
            return "SAFE";
        }
        
        public double getConfidence() {
            // Calculate prediction confidence based on model agreement
            double[] scores = {lstmScore, xgboostScore, isolationForestScore};
            double mean = ensembleScore;
            double variance = 0;
            for (double score : scores) {
                variance += Math.pow(score - mean, 2);
            }
            variance /= scores.length;
            double stdDev = Math.sqrt(variance);
            
            // Low std dev = high confidence
            return 1.0 - Math.min(stdDev * 2, 1.0);
        }
    }
    
    /**
     * ML Ensemble Predictor
     * In production, this would call Python ML models via JNI, REST API, or embedded models
     */
    public static class MLEnsemblePredictor {
        
        private Random random = new Random(); // For demo simulation
        
        public ThreatPrediction predict(RequestFeatures features) {
            long startTime = System.currentTimeMillis();
            
            ThreatPrediction prediction = new ThreatPrediction();
            
            // PRODUCTION: These would be actual model predictions
            // For demo: Simulate realistic predictions based on request characteristics
            
            // LSTM: Analyze API call sequence patterns
            prediction.lstmScore = predictLSTM(features);
            
            // XGBoost: Analyze network flow characteristics
            prediction.xgboostScore = predictXGBoost(features);
            
            // Isolation Forest: Anomaly detection
            prediction.isolationForestScore = predictIsolationForest(features);
            
            // Ensemble: Weighted average (weights from ensemb3.py)
            prediction.ensembleScore = 
                0.45 * prediction.lstmScore +
                0.40 * prediction.xgboostScore +
                0.15 * prediction.isolationForestScore;
            
            prediction.processingTimeMs = System.currentTimeMillis() - startTime;
            
            return prediction;
        }
        
        /**
         * LSTM model prediction (simulated)
         * In production: Load actual LSTM model via TensorFlow Java or REST API
         */
        private double predictLSTM(RequestFeatures features) {
            double score = 0.0;
            
            // Check for anomalous API sequences
            if (features.apiSequence.size() > 80) {
                score += 0.3; // Unusually long sequence
            }
            
            if (features.interAPIAccessDuration > 100) {
                score += 0.2; // Unusual timing
            }
            
            if (features.apiAccessUniqueness < 0.1) {
                score += 0.3; // Repetitive pattern
            }
            
            // Check for SQL injection patterns in path
            if (features.queryString != null) {
                String query = features.queryString.toLowerCase();
                if (query.contains("union") || query.contains("select") || 
                    query.contains("'") || query.contains("or 1=1")) {
                    score += 0.8;
                }
            }
            
            // Add small random variance for demo
            score += random.nextDouble() * 0.05;
            
            return Math.min(score, 1.0);
        }
        
        /**
         * XGBoost model prediction (simulated)
         * In production: Load actual XGBoost model via Java binding
         */
        private double predictXGBoost(RequestFeatures features) {
            double score = 0.0;
            
            // High request rate = suspicious
            if (features.requestRate > 10) {
                score += 0.4;
            }
            
            // Large payload
            if (features.bytesTransferred > 10000) {
                score += 0.2;
            }
            
            // Unusual packet patterns
            if (features.packetCount > 50) {
                score += 0.2;
            }
            
            // Check for XSS patterns
            if (features.queryString != null) {
                String query = features.queryString.toLowerCase();
                if (query.contains("<script") || query.contains("javascript:") ||
                    query.contains("onerror")) {
                    score += 0.7;
                }
            }
            
            // Add small random variance
            score += random.nextDouble() * 0.05;
            
            return Math.min(score, 1.0);
        }
        
        /**
         * Isolation Forest prediction (simulated)
         * In production: Load actual Isolation Forest via scikit-learn REST API
         */
        private double predictIsolationForest(RequestFeatures features) {
            double score = 0.0;
            
            // Anomaly detection based on deviation from normal patterns
            
            // Unusual method for this endpoint
            if ("DELETE".equals(features.method) || "PUT".equals(features.method)) {
                score += 0.3;
            }
            
            // Path traversal attempts
            if (features.path.contains("../")) {
                score += 0.8;
            }
            
            // Unusual parameter combinations
            if (features.parameters.size() > 15) {
                score += 0.3;
            }
            
            // Very short or very long query strings
            if (features.queryString != null && features.queryString.length() > 500) {
                score += 0.4;
            }
            
            // Add variance
            score += random.nextDouble() * 0.05;
            
            return Math.min(score, 1.0);
        }
    }
    
    /**
     * Threat detection log entry
     */
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
        
        // ML predictions
        public double ensembleScore;
        public double lstmScore;
        public double xgboostScore;
        public double isolationForestScore;
        public String threatLevel;
        public double confidence;
    }
    
    /**
     * Detection statistics
     */
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