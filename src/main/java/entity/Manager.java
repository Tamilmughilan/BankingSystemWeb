package entity;

/**
 * Represents a manager in the banking system.
 * Inherits from Employee with manager-specific logic.
 * 
 * @author TAMIL MUGHILAN
 */
public class Manager extends Employee {

    /**
     * Constructs a Manager with required details.
     * 
     * @param id        manager ID
     * @param name      manager name
     * @param email     manager email
     * @param branchId  manager's branch ID
     * @param password  manager's password
     */
    public Manager(int id, String name, String email, int branchId, String password) {
        super(new Employee.Builder(name, email, "MANAGER", branchId)
              .id(id)
              .password(password)
        );
    }

    /**
     * Checks if manager can override transaction limits.
     * 
     * @return true always
     */
    public boolean canOverrideTransactionLimits() {
        return true;
    }

    @Override
    public String toString() {
        return "Manager: " + getName() + " (" + getId() + ")\n" +
               "Email: " + getEmail() + "\n" +
               "Branch ID: " + getBranchId();
    }
}
