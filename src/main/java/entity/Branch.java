package entity;

/**
 * Represents a bank branch.
 * Contains basic details such as ID, name, and address.
 * 
 * @author TAMIL MUGHILAN
 */
public class Branch {
    private int branchId;
    private String branchName;
    private String address;

    /**
     * Constructs a Branch.
     * 
     * @param branchId   branch ID
     * @param branchName name of the branch
     * @param address    address of the branch
     */
    public Branch(int branchId, String branchName, String address) {
        this.branchId = branchId;
        this.branchName = branchName;
        this.address = address;
    }

    /**
     * Gets the branch ID.
     * 
     * @return the branch ID
     */
    public int getBranchId() {
        return branchId;
    }

    /**
     * Gets the name of the branch.
     * 
     * @return the branch name
     */
    public String getBranchName() {
        return branchName;
    }

    /**
     * Gets the branch address.
     * 
     * @return the branch address
     */
    public String getAddress() {
        return address;
    }

    public void setBranchId(int branchId) {
        this.branchId = branchId;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "Branch ID: " + branchId + ", Name: " + branchName + ", Address: " + address;
    }
}
