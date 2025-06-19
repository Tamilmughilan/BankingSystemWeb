package job;

import entity.SavingsAccount;
import storage.DataStorage;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.logging.Logger;

public class InterestCalculationJob implements BankingJob {
    private static final Logger logger = Logger.getLogger(InterestCalculationJob.class.getName());
    private static final BigDecimal ANNUAL_INTEREST_RATE = new BigDecimal("0.035"); 
    private static final BigDecimal MONTHS_IN_YEAR = new BigDecimal("12");
    
    private final DataStorage dataStorage;
    
    public InterestCalculationJob(DataStorage dataStorage) {
        this.dataStorage = dataStorage;
    }
    
    @Override
    public void execute() {
      
        System.out.println(" INTEREST CALCULATION JOB STARTED ");

        
        int totalAccountsProcessed = 0;
        BigDecimal totalInterestCredited = BigDecimal.ZERO;
        
        try {
            // Getting all branches
            for (int branchId = 1; branchId <= 3; branchId++) {
                System.out.println("Processing Branch ID: " + branchId);
                //Retrieve the accounts from each pranch
                List<SavingsAccount> accounts = dataStorage.getAccountsByBranch(branchId);
                
                //Calculate interest and credit to the savings accounts
                for (SavingsAccount account : accounts) {
                    BigDecimal interest = calculateAndCreditInterest(account);
                    if (interest.compareTo(BigDecimal.ZERO) > 0) {
                        totalAccountsProcessed++;
                        totalInterestCredited = totalInterestCredited.add(interest);
                    }
                }
            }
            

            System.out.println(" JOB COMPLETED SUCCESSFULLY!");
            System.out.println(" Accounts Processed: " + totalAccountsProcessed);
            System.out.println(" Total Interest Credited: Rs." + totalInterestCredited);
 
        } catch (Exception e) {
            System.err.println("❌ ERROR in Interest Calculation Job: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private BigDecimal calculateAndCreditInterest(SavingsAccount account) {
        try {
        	//Get the existing balance
            BigDecimal currentBalance = account.getBalance();
            
            // Calculate monthly interest
            BigDecimal monthlyRate = ANNUAL_INTEREST_RATE.divide(MONTHS_IN_YEAR, 6, RoundingMode.HALF_UP);
            BigDecimal interest = currentBalance.multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);
            
            // Credit interest only if it is greater than 0
            if (interest.compareTo(BigDecimal.ZERO) > 0) {
            	//If the account has funds deposit the interest to the account
                boolean success = dataStorage.depositToAccount(account.getAccountNo(), interest);
                
                if (success) {
                    System.out.println(" Acc " + account.getAccountNo() + 
                        " : Previous Balance: Rs." + currentBalance + 
                        " | Interest: Rs." + interest + 
                        " | New Balance: Rs." + currentBalance.add(interest));
                    return interest;
                } else {
                    System.err.println("Failed to credit interest to account: " + account.getAccountNo());
                }
            }
        } catch (Exception e) {
            System.err.println("Error processing account " + account.getAccountNo() + ": " + e.getMessage());
        }
        return BigDecimal.ZERO;
    }
    
    @Override
    public String getJobName() {
        return "Interest Calculation Job";
    }
    
    @Override
    public String getJobDescription() {
        return "Calculates and credits monthly interest (3.5% p.a.) to all savings accounts";
    }
}