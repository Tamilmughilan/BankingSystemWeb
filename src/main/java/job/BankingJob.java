package job;

/**
 * Interface for Banking related JOBS
 * 
 * @author TAMIL MUGHILAN
 */
public interface BankingJob {
    void execute();
    String getJobName();
    String getJobDescription();
}