package entity;

import java.math.BigDecimal;

/**
 * Represents an abstract bank account with basic operations like deposit and withdrawal.
 * Serves as a base class for specific account types like {@link SavingsAccount}.
 * 
 * @author TAMIL MUGHILAN
 * @see SavingsAccount
 */
public abstract class AbstractAccount {
    protected int accountNo;
    protected int customerId;
    protected BigDecimal balance;
    protected String accountType;

    /**
     * Constructs an AbstractAccount.
     * 
     * @param accountNo   account number
     * @param customerId  customer ID
     * @param balance     initial balance
     * @param accountType type of account (e.g., "Savings")
     */
    protected AbstractAccount(int accountNo, int customerId, BigDecimal balance, String accountType) {
        this.accountNo = accountNo;
        this.customerId = customerId;
        this.balance = balance;
        this.accountType = accountType;
    }

    /**
     * Gets the branch ID where the account is held.
     * 
     * @return the branch ID
     */
    public abstract int getBranchId();

    /**
     * Checks if a withdrawal of the given amount is allowed.
     * 
     * @param amount amount to withdraw
     * @return {@code true} if withdrawal is allowed; {@code false} otherwise
     */
    public boolean canWithdraw(BigDecimal amount) {
        return amount.compareTo(BigDecimal.ZERO) > 0 &&
               amount.compareTo(balance) <= 0;
    }

    /**
     * Attempts to withdraw the given amount from the account.
     * 
     * @param amount amount to withdraw
     * @return {@code true} if withdrawal successful; {@code false} otherwise
     */
    public boolean withdraw(BigDecimal amount) {
        if (canWithdraw(amount)) {
            balance = balance.subtract(amount);
            return true;
        }
        return false;
    }

    /**
     * Deposits the given amount to the account.
     * 
     * @param amount amount to deposit
     */
    public void deposit(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            balance = balance.add(amount);
        }
    }

    /**
     * Validates the account details.
     * 
     * @return {@code true} if the account is valid; {@code false} otherwise
     */
    public boolean isValidAccount() {
        return customerId > 0 && balance.compareTo(BigDecimal.ZERO) >= 0;
    }

    /**
     * Gets the account number.
     * 
     * @return the account number
     */
    public int getAccountNo() { return accountNo; }

    /**
     * Gets the customer ID.
     * 
     * @return the customer ID
     */
    public int getCustomerId() { return customerId; }

    /**
     * Gets the account balance.
     * 
     * @return the current balance
     */
    public BigDecimal getBalance() { return balance; }

    /**
     * Gets the account type.
     * 
     * @return the account type
     */
    public String getAccountType() { return accountType; }

    /**
     * Sets the account balance.
     * 
     * @param balance the new balance
     */
    public void setBalance(BigDecimal balance) { this.balance = balance; }
}
