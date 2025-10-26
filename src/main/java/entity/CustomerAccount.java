package entity;

/**
 * Represents the relationship between a customer and an account.
 * Each record indicates whether the customer is primary/secondary on the account.
 * 
 * @author TAMIL MUGHILAN
 */
public class CustomerAccount {
    private int id;
    private int customerId;
    private int accountNo;
    private String accountRole;

    /**
     * Constructs a CustomerAccount.
     * 
     * @param customerId  customer ID
     * @param accountNo   account number
     * @param accountRole account role (e.g., "PRIMARY")
     */
    public CustomerAccount(int customerId, int accountNo, String accountRole) {
        this.customerId = customerId;
        this.accountNo = accountNo;
        this.accountRole = accountRole;
    }

    /**
     * Builder for creating {@link CustomerAccount} instances.
     */
    public static class Builder {
        private int id;
        private final int customerId;
        private final int accountNo;
        private String accountRole = "PRIMARY";

        /**
         * Constructs a builder with required fields.
         * 
         * @param customerId customer ID
         * @param accountNo  account number
         */
        public Builder(int customerId, int accountNo) {
            this.customerId = customerId;
            this.accountNo = accountNo;
        }

        public Builder id(int id) { this.id = id; return this; }
        public Builder accountRole(String role) { this.accountRole = role; return this; }

        /**
         * Builds and returns a {@link CustomerAccount} instance.
         * 
         * @return a new CustomerAccount
         */
        public CustomerAccount build() {
            CustomerAccount ca = new CustomerAccount(customerId, accountNo, accountRole);
            ca.id = this.id;
            return ca;
        }
    }

    public int getId() { return id; }

    public int getCustomerId() { return customerId; }

    public int getAccountNo() { return accountNo; }

    public String getAccountRole() { return accountRole; }

    public void setId(int id) { this.id = id; }

    public void setAccountRole(String accountRole) { this.accountRole = accountRole; }
}
