package entity;

public class Branch {
    private int branchId;
    private String branchName;
    private String address;

    public Branch(int branchId, String branchName, String address) {
        this.branchId = branchId;
        this.branchName = branchName;
        this.address = address;
    }

    public int getBranchId() {
        return branchId;
    }

    public String getBranchName() {
        return branchName;
    }

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
