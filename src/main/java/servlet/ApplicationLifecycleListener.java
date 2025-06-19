package servlet;

import job.JobManager;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.logging.Logger;

@WebListener
public class ApplicationLifecycleListener implements ServletContextListener {
    private static final Logger logger = Logger.getLogger(ApplicationLifecycleListener.class.getName());
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("Banking System Application Starting...");
        
        try {
            // Initialize and start the job scheduler
            JobManager jobManager = JobManager.getInstance();
            jobManager.initializeJobs(sce.getServletContext());
            
            logger.info("Banking System Application Started Successfully");
        } catch (Exception e) {
            logger.severe("Failed to start Banking System Application: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("Banking System Application Shutting Down...");
        
        try {
            // Shutdown job scheduler
            JobManager jobManager = JobManager.getInstance();
            jobManager.shutdown();
            
            logger.info("Banking System Application Shutdown Complete");
        } catch (Exception e) {
            logger.severe("Error during application shutdown: " + e.getMessage());
            e.printStackTrace();
        }
    }
}