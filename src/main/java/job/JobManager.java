package job;

import storage.DatabaseStorage;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import javax.servlet.ServletContext;

/**
 * Manages all background jobs.
 * Uses Singleton pattern to ensure only one job manager exists.
 *
 * @author TAMIL MUGHILAN
 */
public class JobManager {
    private static final Logger logger = Logger.getLogger(JobManager.class.getName());
    private static JobManager instance;
    private JobScheduler scheduler;
    
    private JobManager() {
        scheduler = new JobScheduler();
    }
    
    /**
     * Gets the single instance of JobManager.
     * Creates new instance if none exists.
     *
     * @return the JobManager instance
     */
    public static synchronized JobManager getInstance() {
        if (instance == null) {
            instance = new JobManager();
        }
        return instance;
    }
    
    /**
     * Initializes and starts background jobs.
     *
     * @param context the servlet context
     * @throws SQLException if database connection fails
     */
    public void initializeJobs(ServletContext context) throws SQLException {
        logger.info("Starting Simple Interest Job...");
        
        DatabaseStorage dataStorage = new DatabaseStorage();
        InterestCalculationJob interestJob = new InterestCalculationJob(dataStorage);
        
        // Run every 2 minutes for console based output
        scheduler.scheduleJob(interestJob, 10, 600, TimeUnit.SECONDS);
        
        logger.info("Interest Job scheduled to run every 2 minutes");
    }
    
    /**
     * Shuts down the job manager.
     */
    public void shutdown() {
        logger.info("Shutting down Job Manager...");
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }
}