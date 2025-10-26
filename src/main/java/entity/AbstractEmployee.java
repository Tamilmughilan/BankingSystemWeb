package entity;

/**
 * Abstract base class for employees in the banking system.
 * Implements common fields and behavior.
 * 
 * @author TAMIL MUGHILAN
 */
public abstract class AbstractEmployee implements Person {
    protected int id;
    protected String name;
    protected String email;
    protected String role;
    protected String password;

    /**
     * Constructs an AbstractEmployee.
     * 
     * @param id       employee ID
     * @param name     employee name
     * @param email    employee email
     * @param role     role of the employee
     * @param password password
     */
    protected AbstractEmployee(int id, String name, String email, String role, String password) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.password = password;
    }

    public String getPassword() { return password; }

    public void setPassword(String password) { this.password = password; }

    /**
     * Gets the branch ID.
     * 
     * @return branch ID
     */
    public abstract int getBranchId();

    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }

    /**
     * Checks if the employee can approve transactions.
     * 
     * @return true if manager, false otherwise
     */
    public boolean canApproveTransactions() {
        return "Manager".equals(role);
    }

    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setRole(String role) { this.role = role; }
}
