package entity;

public abstract class AbstractEmployee implements Person {
    protected int id;
    protected String name;
    protected String email;
    protected String role;
    protected String password;
    
    protected AbstractEmployee(int id, String name, String email, String role, String password) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.password = password;
    }

    
    //New getter and setter added after adding password 
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }

    public abstract int getBranchId(); //New method added after adding branch

    //Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }

    public boolean canApproveTransactions() {
        return "Manager".equals(role);
    }
    
    //Setters
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setRole(String role) { this.role = role; }
}
