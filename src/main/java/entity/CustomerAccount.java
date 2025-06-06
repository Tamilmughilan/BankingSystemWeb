package entity;

public class CustomerAccount {
    private int id;
    private int customerId;
    private int accountNo;
    private String accountRole; 
    
    
    // Constructors
    public CustomerAccount(int customerId, int accountNo, String accountRole) {
        this.customerId = customerId;
        this.accountNo = accountNo;
        this.accountRole = accountRole;
    }
    
    
    public static class Builder {
        private int id;
        private final int customerId;
        private final int accountNo;
        private String accountRole = "PRIMARY";
        
        
        public Builder(int customerId, int accountNo) {
            this.customerId = customerId;
            this.accountNo = accountNo;
        }
        
        public Builder id(int id) { this.id = id; return this; }
        public Builder accountRole(String role) { this.accountRole = role; return this; }
        
        
        public CustomerAccount build() {
            CustomerAccount ca = new CustomerAccount(customerId, accountNo, accountRole);
            ca.id = this.id;
            return ca;
        }
    }
    
    // getters and setters
    public int getId() { return id; }
    public int getCustomerId() { return customerId; }
    public int getAccountNo() { return accountNo; }
    public String getAccountRole() { return accountRole; }
    
    
    public void setId(int id) { this.id = id; }
    public void setAccountRole(String accountRole) { this.accountRole = accountRole; }
}