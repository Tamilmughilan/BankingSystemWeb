package entity;

public class Manager extends Employee {
    
    // Constructor with password for database retrieval
    public Manager(int id, String name, String email, int branch_id, String password) {
        super(id, name, email, "MANAGER", branch_id, password);
    }

    public boolean canOverrideTransactionLimits() {
        return true;
    }

    @Override
    public String toString() {
        return "Manager: " + name + " (" + id + ")" + 
               "\nEmail: " + email + 
               "\nBranch ID: " + getBranchId();
    }
}