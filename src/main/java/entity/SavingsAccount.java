package entity;

public class SavingsAccount extends AbstractAccount {
    private final int branch_id;
    private static final double MIN_BALANCE = 100.0;

    private SavingsAccount(Builder builder) {
        super(builder.accountNo, builder.customerId, builder.balance, "Savings");
        this.branch_id = builder.branchId;
    }

    public static class Builder {
        // Required fields
        private final int customerId;
        private final int branchId;
        
        // Optional fields
        private int accountNo = 0;
        private double balance = 0.0;

        public Builder(int customerId, int branchId) {
            this.customerId = customerId;
            this.branchId = branchId;
        }

        public Builder accountNo(int accountNo) { this.accountNo = accountNo; return this; }
        public Builder balance(double balance) { this.balance = balance; return this; }

        public SavingsAccount build() {
            return new SavingsAccount(this);
        }
    }

    public int getBranchId() { return branch_id; }

    @Override
    public boolean canWithdraw(double amount) {
        return super.canWithdraw(amount) && (balance - amount) >= MIN_BALANCE;
    }

    @Override
    public String toString() {
        return "SavingsAccount: AC " + accountNo + "\n" +
               "Balance: Rs." + balance + "\n" +
               "Customer ID: " + customerId + "\n" +
               "Branch ID: " + branch_id;
    }
}
