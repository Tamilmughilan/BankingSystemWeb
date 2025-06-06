package entity;
import java.math.BigDecimal;
import java.math.RoundingMode;

public abstract class AbstractAccount {
    protected int accountNo;
    protected int customerId;
    protected BigDecimal balance;  //updated from double to Big Decimal
    protected String accountType;
    
    //constructor
    protected AbstractAccount(int accountNo, int customerId, BigDecimal balance, String accountType) {
        this.accountNo = accountNo;
        this.customerId = customerId;
        this.balance = balance;
        this.accountType = accountType;
    }
    
    public abstract int getBranchId();
    
    // withdrawal eligibility
    public boolean canWithdraw(BigDecimal amount) {
        return amount.compareTo(BigDecimal.ZERO) > 0 && 
               amount.compareTo(balance) <= 0;
    }

    // account methods
    public boolean withdraw(BigDecimal amount) {
        if (canWithdraw(amount)) {
            balance = balance.subtract(amount);
            return true;
        }
        return false;
    }

    public void deposit(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            balance = balance.add(amount);
        }
    }
    
   //validation
    public boolean isValidAccount() {
        return customerId > 0 && balance.compareTo(BigDecimal.ZERO) >= 0;
    }
    
    //getters/setters
    public int getAccountNo() { return accountNo; }
    public int getCustomerId() { return customerId; }
    public BigDecimal getBalance() { return balance; }  
    public String getAccountType() { return accountType; }
    
    public void setBalance(BigDecimal balance) { this.balance = balance; }  // Changed parameter type
}