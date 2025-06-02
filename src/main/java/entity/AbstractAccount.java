package entity;

public abstract class AbstractAccount {
    protected int accountNo;
    protected int customerId;
    protected double balance;
    protected String accountType;

    protected AbstractAccount(int accountNo, int customerId, double balance, String accountType) {
        this.accountNo = accountNo;
        this.customerId = customerId;
        this.balance = balance;
        this.accountType = accountType;
    }

    public abstract int getBranchId();

    public boolean canWithdraw(double amount) {
        return amount > 0 && amount <= balance;
    }

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

    public boolean isValidAccount() {
        return customerId > 0 && balance >= 0;
    }

    public int getAccountNo() { return accountNo; }
    public int getCustomerId() { return customerId; }
    public double getBalance() { return balance; }
    public String getAccountType() { return accountType; }

    public void setBalance(double balance) { this.balance = balance; }
}
