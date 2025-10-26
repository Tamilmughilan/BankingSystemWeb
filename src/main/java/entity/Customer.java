package entity;

/**
 * Represents a customer in the banking system.
 * Built using the Builder pattern.
 * 
 * @author TAMIL MUGHILAN
 */
public class Customer extends AbstractCustomer {
    private final int branch_id;
    private String salt;

    private Customer(Builder builder) {
        super(builder.id, builder.name, builder.phone, builder.email);
        this.branch_id = builder.branchId;
        this.password = builder.password;
        this.salt = builder.salt;
    }

    /**
     * Builder for creating Customer instances.
     */
    public static class Builder {
        private final String name;
        private final String email;
        private final int branchId;

        private int id = 0;
        private String phone;
        private String password;
        private String salt;

        public Builder(String name, String email, int branchId) {
            this.name = name;
            this.email = email;
            this.branchId = branchId;
        }

        public Builder id(int id) { this.id = id; return this; }
        public Builder phone(String phone) { this.phone = phone; return this; }
        public Builder password(String password) { this.password = password; return this; }
        public Builder salt(String salt) { this.salt = salt; return this; }

        public Customer build() {
            return new Customer(this);
        }
    }
    
    /**
     * Gets the branch ID.
     * 
     * @return the branch ID
     */
    public int getBranchId() { return branch_id; }
    
    /**
     * Gets the Salt for password.
     * 
     * @return salt for password
     */
    public String getSalt() { return salt; }
    
    
    @Override
    public String toString() {
        return "Customer: " + name + " (" + id + ")\n" +
               "Phone: " + phone + "\n" +
               "Email: " + email + "\n" +
               "Branch ID: " + branch_id;
    }
}
