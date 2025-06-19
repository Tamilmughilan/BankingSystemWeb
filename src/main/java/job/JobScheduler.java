package job;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class JobScheduler {
    private static final Logger logger = Logger.getLogger(JobScheduler.class.getName());
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    public void scheduleJob(BankingJob job, long initialDelay, long period, TimeUnit timeUnit) {
        logger.info(String.format("Scheduling %s to run every %d %s", 
            job.getJobName(), period, timeUnit.toString()));
        
        scheduler.scheduleAtFixedRate(() -> {
            try {
                logger.info("Executing : " + job.getJobName());
                job.execute();
            } catch (Exception e) {
                logger.severe("Error " + job.getJobName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }, initialDelay, period, timeUnit);
    }
    
    public void shutdown() {
        logger.info("Shutting down job scheduler...");
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
    }
}