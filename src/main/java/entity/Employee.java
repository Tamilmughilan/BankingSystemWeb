package entity;

public class Employee extends AbstractEmployee {
    private final int branch_id;

    protected Employee(Builder builder) {
        super(builder.id, builder.name, builder.email, builder.role, builder.password);
        this.branch_id = builder.branchId;
    }

    public static class Builder {
        // Required fields
        private final String name;
        private final String email;
        private final String role;
        private final int branchId;
        
        // Optional fields
        private int id = 0;
        private String password;

        public Builder(String name, String email, String role, int branchId) {
            this.name = name;
            this.email = email;
            this.role = role;
            this.branchId = branchId;
        }

        // Builder methods
        public Builder id(int id) { this.id = id; return this; }
        public Builder password(String password) { this.password = password; return this; }

        public Employee build() {
            return new Employee(this);
        }
    }

    public int getBranchId() { return branch_id; }

    @Override
    public String toString() {
        return "Employee: " + name + " (" + id + ")\n" +
               "Role: " + role + "\n" +
               "Email: " + email + "\n" +
               "Branch ID: " + branch_id;
    }
}
