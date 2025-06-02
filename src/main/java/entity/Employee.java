package entity;

public class Employee extends AbstractEmployee {
    private int branch_id;

    // Constructor without password (for creating new employees)
    public Employee(int id, String name, String email, String role, int branch_id) {
        super(id, name, email, role, null);
        this.branch_id = branch_id;
    }
    
    // Constructor with password (for database retrieval)
    public Employee(int id, String name, String email, String role, int branch_id, String password) {
        super(id, name, email, role, password);
        this.branch_id = branch_id;
    }

    public int getBranchId() {
        return branch_id;
    }

    public void setBranchId(int branch_id) {
        this.branch_id = branch_id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Employee: " + name + " (" + id + ")" + 
               "\nRole: " + role + 
               "\nEmail: " + email + 
               "\nBranch ID: " + branch_id;
    }
}