package entity;

public class Manager extends Employee {
    public Manager(int id, String name, String email, int branchId, String password) {
        // Pass the Builder directly without calling .build()
        super(new Employee.Builder(name, email, "MANAGER", branchId)
              .id(id)
              .password(password)
        );
    }
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
