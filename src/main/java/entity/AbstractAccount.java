package entity;

//All the abstract methods must be implemented
//Common methods are defined
public abstract class AbstractAccount {
	//States of the account object
    protected int accountNo;
    protected int customerId;
    protected double balance;
    protected String accountType;
    
    //Constructor
    protected AbstractAccount(int accountNo, int customerId, double balance, String accountType) {
        this.accountNo = accountNo;
        this.customerId = customerId;
        this.balance = balance;
        this.accountType = accountType;
    }
    
    //GET - branch ID of an account
    public abstract int getBranchId();
    
    //Withdraw eligibility
    public boolean canWithdraw(double amount) {
        return amount > 0 && amount <= balance;
    }

    //Account methods - withdraw and deposit
    public boolean withdraw(double amount) {
        if (canWithdraw(amount)) {
            balance -= amount;
            return true;
        }
        return false;
    }

    public void deposit(double amount) {
        if (amount > 0) {
            balance += amount;
        }
    }
    
    //Validation
    public boolean isValidAccount() {
        return customerId > 0 && balance >= 0;
    }
    
    //Getters
    public int getAccountNo() { return accountNo; }
    public int getCustomerId() { return customerId; }
    public double getBalance() { return balance; }
    public String getAccountType() { return accountType; }
    
    //Setter
    public void setBalance(double balance) { this.balance = balance; }
}
