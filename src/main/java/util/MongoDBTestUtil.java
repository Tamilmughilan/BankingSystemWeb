package util;

import storage.MongoDBStorage;
import entity.*;
import java.math.BigDecimal;


public class MongoDBTestUtil{
    
    private MongoDBStorage mongoStorage;
    
    public MongoDBTestUtil() {
        this.mongoStorage = new MongoDBStorage();
    }
    
    
    public void clearAllData() {
        System.out.println("Clearing all existing data...");
        mongoStorage.clearAllCollections();
    }
    
   
    public void insertMySQLData() {
        System.out.println("Inserting your existing MySQL data into MongoDB...");
        
    
        Branch branch1 = new Branch(1, "mgp", "guruswamy road mogappair");
        Branch branch2 = new Branch(2, "Main Branch", "main");
        Branch branch3 = new Branch(3, "North Branch", "north");
        Branch branch4 = new Branch(4, "South Branch", "south");
        
        mongoStorage.saveBranch(branch1);
        mongoStorage.saveBranch(branch2);
        mongoStorage.saveBranch(branch3);
        mongoStorage.saveBranch(branch4);
        System.out.println("Branches inserted successfully");
        

        System.out.println("Inserting employees...");
        
        
        Manager manager1 = new Manager(
            1, 
            "Manager1", 
            "manager@gmail.com", 
            1, 
            "e09797c9aa9489339ebb45b1e2df16c48781f969f2b1cf8345cc55020c57e036"
        );
        mongoStorage.saveEmployeeWithPassword(manager1, "b569664210a09cd26ebd7588ab2644b9");
        System.out.println("Manager1 inserted with ID: 1");
        
       
        Employee employee2 = new Employee.Builder(
            "Employee2",
            "employee@gmail.com",
            "Employee",
            1)
            .id(2)
            .password("fcd3776c644a5e5844821d7ed8fbddda91296bb38730cf37e5f281a15b2dc160")
            .build();
        mongoStorage.saveEmployeeWithPassword(employee2, "09615478bdcc762ada3ace2094009d18");
        System.out.println("Employee2 inserted with ID: 2");
        
        System.out.println("Your MySQL employee data inserted successfully!");
    }
    
   
    public void insertSampleData() {
        System.out.println("Inserting additional sample data...");
        
        // Insert customers
        String customerSalt1 = PasswordUtil.generateSalt();
        String customerPassword1 = PasswordUtil.hashPassword("password123", customerSalt1);
        
        Customer customer1 = new Customer.Builder(
            "Santhosh",
            "santhoshdh@gmail.com",
            1)
            .phone("9765903761")
            .password(customerPassword1)
            .build();
        int customerId1 = mongoStorage.saveCustomerWithPassword(customer1, customerSalt1);
        System.out.println("Customer inserted with ID: " + customerId1);
        
        String customerSalt2 = PasswordUtil.generateSalt();
        String customerPassword2 = PasswordUtil.hashPassword("password456", customerSalt2);
        
        Customer customer2 = new Customer.Builder(
            "Shanthanu",
            "shanthanu@gmail.com",
            2)
            .phone("9873649320")
            .password(customerPassword2)
            .build();
        int customerId2 = mongoStorage.saveCustomerWithPassword(customer2, customerSalt2);
        System.out.println("Customer inserted with ID: " + customerId2);
        
        // Insert accounts
        SavingsAccount account1 = new SavingsAccount.Builder(customerId1, 1)
            .balance(new BigDecimal("5000.00"))
            .build();
        int accountId1 = mongoStorage.saveAccount(account1);
        System.out.println("Account Id for santhosh: " + accountId1);
        
        SavingsAccount account2 = new SavingsAccount.Builder(customerId2, 2)
            .balance(new BigDecimal("3500.50"))
            .build();
        int accountId2 = mongoStorage.saveAccount(account2);
        System.out.println("Account Id for shanthanu: " + accountId2);
        
      
        mongoStorage.addCustomerToAccount(customerId1, accountId1, "PRIMARY");
        mongoStorage.addCustomerToAccount(customerId2, accountId2, "PRIMARY");
        
        System.out.println("Sample data insertion completed!");
    }
   
    public void displayAllData() {
        System.out.println("\n=== Current Data in MongoDB ===");
        
        // Display branches
        System.out.println("\nBranches:");
        for (int i = 1; i <= 4; i++) {
            Branch branch = mongoStorage.getBranch(i);
            if (branch != null) {
                System.out.println("  " + branch.getBranchId() + ": " + branch.getBranchName() + " - " + branch.getAddress());
            }
        }
        
        // Display employees
        System.out.println("\nEmployees:");
        Employee manager = mongoStorage.getEmployeeByEmail("manager@gmail.com");
        if (manager != null) {
            System.out.println("  ID " + manager.getId() + ": " + manager.getName() + " (" + manager.getRole() + ") - " + manager.getEmail());
        }
        
        Employee employee = mongoStorage.getEmployeeByEmail("employee@gmail.com");
        if (employee != null) {
            System.out.println("  ID " + employee.getId() + ": " + employee.getName() + " (" + employee.getRole() + ") - " + employee.getEmail());
        }
        
        // Display customers
        System.out.println("\nCustomers:");
        Customer customer1 = mongoStorage.getCustomerByEmail("john.doe@email.com");
        if (customer1 != null) {
            System.out.println("  ID " + customer1.getId() + ": " + customer1.getName() + " - " + customer1.getEmail());
        }
        
        Customer customer2 = mongoStorage.getCustomerByEmail("jane.smith@email.com");
        if (customer2 != null) {
            System.out.println("  ID " + customer2.getId() + ": " + customer2.getName() + " - " + customer2.getEmail());
        }
        
        // Display accounts by branch
        System.out.println("\nAccounts by Branch:");
        for (int branchId = 1; branchId <= 4; branchId++) {
            java.util.List<SavingsAccount> branchAccounts = mongoStorage.getAccountsByBranch(branchId);
            if (!branchAccounts.isEmpty()) {
                System.out.println("  Branch " + branchId + " has " + branchAccounts.size() + " account(s)");
                for (SavingsAccount account : branchAccounts) {
                    System.out.println("    Account " + account.getAccountNo() + ": Balance " + account.getBalance());
                }
            }
        }
    }
    
    public void close() {
        if (mongoStorage != null) {
            mongoStorage.close();
        }
    }
 
    public static void main(String[] args) {
        MongoDBTestUtil test = new MongoDBTestUtil();
        
        try {
            
            //test.clearAllData();
            
            // Insert your MySQL data
            //test.insertMySQLData();
            
            // Insert additional sample data
            //test.insertSampleData();
            
           
            // Display all data
           test.displayAllData();
            
        } finally {
            test.close();
        }
    }
}