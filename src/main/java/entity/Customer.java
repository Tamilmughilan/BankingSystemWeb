package entity;

public class Customer extends AbstractCustomer {
    private int branch_id;

    public Customer(int id, String name, String phone, String email, int branch_id) {
        super(id, name, phone, email);
        this.branch_id = branch_id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setBranchId(int branch_id) {
        this.branch_id = branch_id;
    }

    public int getBranchId() {
        return branch_id;
    }

    public String getDisplayName() {
        return "\nCustomer: " + name + " (" + id + ")";
    }

    @Override
    public String toString() {
        return getDisplayName() + "\nPhone: " + phone + "\nEmail: " + email + "\nBranch ID: " + branch_id;
    }
}