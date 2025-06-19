package job;

import storage.DatabaseStorage;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import javax.servlet.ServletContext;

public class JobManager {
    private static final Logger logger = Logger.getLogger(JobManager.class.getName());
    private static JobManager instance;
    private JobScheduler scheduler;
    
    private JobManager() {
        scheduler = new JobScheduler();
    }
    
    //Singleton for Job manager to maintain consistency
    public static synchronized JobManager getInstance() {
        if (instance == null) {
            instance = new JobManager();
        }
        return instance;
    }
    
    public void initializeJobs(ServletContext context) throws SQLException {
        logger.info("Starting Simple Interest Job...");
        
        DatabaseStorage dataStorage = new DatabaseStorage();
        InterestCalculationJob interestJob = new InterestCalculationJob(dataStorage);
        
        // Run every 2 minutes for console based output
        scheduler.scheduleJob(interestJob, 10, 120, TimeUnit.SECONDS);
        
        logger.info("Interest Job scheduled to run every 2 minutes");
    }
    
    public void shutdown() {
        logger.info("Shutting down Job Manager...");
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }
}