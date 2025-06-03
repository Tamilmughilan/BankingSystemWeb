package entity;


//Abstract class implementing Person interface
public abstract class AbstractCustomer implements Person {
    protected int id;
    protected String name;
    protected String phone;
    protected String email;
    protected String password;
    
    public AbstractCustomer(int id, String name, String phone, String email) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }

    public abstract int getBranchId(); //New method added after including branch
    
    //Validations
    public boolean isValidPhone() {
        return phone != null && phone.length() >= 10;
    }

    public boolean isValidCustomer() {
        return isValidName() && isValidEmail() && isValidPhone();
    }
    
    //Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    
    //Setters
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
}
