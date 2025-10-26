package entity;

import java.math.BigDecimal;

/**
 * Represents a savings account with a minimum balance constraint.
 * Extends the {@link AbstractAccount} class.
 * 
 * @author TAMIL
 * @see AbstractAccount
 */
public class SavingsAccount extends AbstractAccount {
    private final int branch_id;
    private static final BigDecimal MIN_BALANCE = new BigDecimal("100.00");

    private SavingsAccount(Builder builder) {
        super(builder.accountNo, builder.customerId, builder.balance, "Savings");
        this.branch_id = builder.branchId;
    }

    /**
     * Builder for creating {@link SavingsAccount} instances.
     */
    public static class Builder {
        private final int customerId;
        private final int branchId;
        private int accountNo = 0;
        private BigDecimal balance = BigDecimal.ZERO;

        /**
         * Constructs a Builder with required fields.
         * 
         * @param customerId the customer ID
         * @param branchId   the branch ID
         */
        public Builder(int customerId, int branchId) {
            this.customerId = customerId;
            this.branchId = branchId;
        }

        public Builder accountNo(int accountNo) { this.accountNo = accountNo; return this; }
        public Builder balance(BigDecimal balance) { this.balance = balance; return this; }

        /**
         * Sets balance using a string (e.g., from user input).
         * 
         * @param balance balance as string
         * @return the builder instance
         */
        public Builder balance(String balance) {
            this.balance = new BigDecimal(balance);
            return this;
        }

        /**
         * Builds and returns a {@link SavingsAccount} instance.
         * 
         * @return a new SavingsAccount
         */
        public SavingsAccount build() {
            return new SavingsAccount(this);
        }
    }

    /**
     * Gets the branch ID where the account is held.
     * 
     * @return the branch ID
     */
    public int getBranchId() { return branch_id; }

    /**
     * Checks if withdrawal is allowed, enforcing minimum balance.
     * 
     * @param amount amount to withdraw
     * @return {@code true} if withdrawal is allowed; {@code false} otherwise
     */
    @Override
    public boolean canWithdraw(BigDecimal amount) {
        return super.canWithdraw(amount) &&
               (balance.subtract(amount)).compareTo(MIN_BALANCE) >= 0;
    }

    @Override
    public String toString() {
        return "SavingsAccount: AC " + accountNo + "\n" +
               "Balance: Rs." + balance.toString() + "\n" +
               "Customer ID: " + customerId + "\n" +
               "Branch ID: " + branch_id;
    }
}
