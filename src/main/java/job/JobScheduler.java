package job;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;


/**
 * Schedules and executes background jobs using thread pool.
 *
 * @author TAMIL MUGHILAN
 */
public class JobScheduler {
    private static final Logger logger = Logger.getLogger(JobScheduler.class.getName());
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    
    /**
     * Schedules a job to run repeatedly at fixed intervals.
     *
     * @param job the banking job to schedule
     * @param initialDelay delay before first execution
     * @param period time between executions
     * @param timeUnit unit for delay and period (SECONDS, MINUTES, etc.)
     */
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
    
    /**
     * Shuts down the job scheduler only if no jobs are running.
     */
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