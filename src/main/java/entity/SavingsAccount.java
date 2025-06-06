package entity;
import java.math.BigDecimal;

public class SavingsAccount extends AbstractAccount {
    private final int branch_id;
    private static final BigDecimal MIN_BALANCE = new BigDecimal("100.00");  // Changed to BigDecimal

    private SavingsAccount(Builder builder) {
        super(builder.accountNo, builder.customerId, builder.balance, "Savings");
        this.branch_id = builder.branchId;
    }

    public static class Builder {
        private final int customerId;
        private final int branchId;
        
        private int accountNo = 0;
        private BigDecimal balance = BigDecimal.ZERO;  // Changed to BigDecimal

        public Builder(int customerId, int branchId) {
            this.customerId = customerId;
            this.branchId = branchId;
        }

        public Builder accountNo(int accountNo) { 
            this.accountNo = accountNo; 
            return this; 
        }
        
        public Builder balance(BigDecimal balance) {  // Changed parameter type
            this.balance = balance; 
            return this; 
        }
        
        // Convenience method for string input
        public Builder balance(String balance) {
            this.balance = new BigDecimal(balance);
            return this;
        }

        public SavingsAccount build() {
            return new SavingsAccount(this);
        }
    }

    public int getBranchId() { return branch_id; }

    @Override
    public boolean canWithdraw(BigDecimal amount) {
        return super.canWithdraw(amount) && 
               (balance.subtract(amount)).compareTo(MIN_BALANCE) >= 0;
    }

    @Override
    public String toString() {
        return "SavingsAccount: AC " + accountNo + "\n" +
               "Balance: Rs." + balance.toString() + "\n" +  // Use toString() for display
               "Customer ID: " + customerId + "\n" +
               "Branch ID: " + branch_id;
    }
}