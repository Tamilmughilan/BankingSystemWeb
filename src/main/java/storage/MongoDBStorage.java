package storage;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.types.ObjectId;

import entity.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MongoDBStorage extends AbstractDataStorage {
    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> customers;
    private MongoCollection<Document> employees;
    private MongoCollection<Document> accounts;
    private MongoCollection<Document> branches;
    private MongoCollection<Document> customerAccounts;
    private MongoCollection<Document> userSalts;

    public MongoDBStorage() {
        try {
            mongoClient = MongoClients.create("mongodb://localhost:27017");
            database = mongoClient.getDatabase("bankingsystem");
            
            customers = database.getCollection("customers");
            employees = database.getCollection("employees");
            accounts = database.getCollection("accounts");
            branches = database.getCollection("branches");
            customerAccounts = database.getCollection("customer_accounts");
            userSalts = database.getCollection("user_salts");
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to connect to MongoDB", e);
        }
    }

    // Helper method to get integer ID from document
    private int getIdAsInteger(Document doc) {
        Object id = doc.get("_id");
        if (id instanceof Integer) {
            return (Integer) id;
        } else if (id instanceof ObjectId) {
            // Convert ObjectId to hash code for compatibility
            return id.hashCode();
        } else {
            return 0;
        }
    }

    // Helper method to create document with proper ID handling
    private Document createDocumentWithId(int id, Document doc) {
        if (id > 0) {
            doc.append("_id", id);
        }
        return doc;
    }

    //customer operations
    @Override
    public int saveCustomer(Customer customer) {
        Document doc = new Document("name", customer.getName())
                .append("phone", customer.getPhone())
                .append("email", customer.getEmail())
                .append("branch_id", customer.getBranchId())
                .append("password", customer.getPassword());
        
      
        if (customer.getId() > 0) {
            doc.append("_id", customer.getId());
        }
        
        customers.insertOne(doc);
        return getIdAsInteger(doc);
    }

    @Override
    public int saveCustomerWithPassword(Customer customer, String salt) {
        Document customerDoc = new Document("name", customer.getName())
                .append("phone", customer.getPhone())
                .append("email", customer.getEmail())
                .append("branch_id", customer.getBranchId())
                .append("password", customer.getPassword());
        
        
        if (customer.getId() > 0) {
            customerDoc.append("_id", customer.getId());
        }
        
        customers.insertOne(customerDoc);
        int customerId = getIdAsInteger(customerDoc);
        
        //Save salt
        Document saltDoc = new Document("user_id", customerId)
                .append("user_type", "CUSTOMER")
                .append("salt", salt);
        userSalts.insertOne(saltDoc);
        
        return customerId;
    }

    @Override
    public Customer getCustomer(int customerId) {
        Document doc = customers.find(Filters.eq("_id", customerId)).first();
        if (doc == null) return null;
        
        return new Customer.Builder(
                doc.getString("name"),
                doc.getString("email"),
                doc.getInteger("branch_id"))
                .id(customerId)
                .phone(doc.getString("phone"))
                .password(doc.getString("password"))
                .build();
    }

    @Override
    public Customer getCustomerByEmail(String email) {
        Document doc = customers.find(Filters.eq("email", email)).first();
        if (doc == null) return null;
        
        int customerId = getIdAsInteger(doc);
        return new Customer.Builder(
                doc.getString("name"),
                doc.getString("email"),
                doc.getInteger("branch_id"))
                .id(customerId)
                .phone(doc.getString("phone"))
                .password(doc.getString("password"))
                .build();
    }

    @Override
    public void updateCustomer(Customer customer) throws SQLException {
        customers.updateOne(
                Filters.eq("_id", customer.getId()),
                Updates.combine(
                        Updates.set("name", customer.getName()),
                        Updates.set("phone", customer.getPhone()),
                        Updates.set("email", customer.getEmail()),
                        Updates.set("branch_id", customer.getBranchId())
                )
        );
    }

    @Override
    public boolean deleteCustomer(int customerId) {
        return customers.deleteOne(Filters.eq("_id", customerId)).getDeletedCount() > 0;
    }

    @Override
    public List<Customer> getCustomersByBranch(int branchId) {
        List<Customer> result = new ArrayList<>();
        for (Document doc : customers.find(Filters.eq("branch_id", branchId))) {
            int customerId = getIdAsInteger(doc);
            Customer customer = new Customer.Builder(
                    doc.getString("name"),
                    doc.getString("email"),
                    doc.getInteger("branch_id"))
                    .id(customerId)
                    .phone(doc.getString("phone"))
                    .password(doc.getString("password"))
                    .build();
            result.add(customer);
        }
        return result;
    }

    @Override
    public String getSaltForCustomer(int customerId) {
        Document doc = userSalts.find(Filters.and(
                Filters.eq("user_id", customerId),
                Filters.eq("user_type", "CUSTOMER")
        )).first();
        return doc != null ? doc.getString("salt") : null;
    }

    // Employee operations
    @Override
    public int saveEmployee(Employee employee) {
        Document doc = new Document("name", employee.getName())
                .append("email", employee.getEmail())
                .append("role", employee.getRole())
                .append("branch_id", employee.getBranchId())
                .append("password", employee.getPassword());
        
        // If employee has an ID, use it; otherwise let MongoDB generate ObjectId
        if (employee.getId() > 0) {
            doc.append("_id", employee.getId());
        }
        
        employees.insertOne(doc);
        return getIdAsInteger(doc);
    }

    @Override
    public int saveEmployeeWithPassword(Employee employee, String salt) {
        Document employeeDoc = new Document("name", employee.getName())
                .append("email", employee.getEmail())
                .append("role", employee.getRole())
                .append("branch_id", employee.getBranchId())
                .append("password", employee.getPassword());
        
       
        if (employee.getId() > 0) {
            employeeDoc.append("_id", employee.getId());
        }
        
        employees.insertOne(employeeDoc);
        int employeeId = getIdAsInteger(employeeDoc);
        
        //Save salt
        Document saltDoc = new Document("user_id", employeeId)
                .append("user_type", employee.getRole().toUpperCase())
                .append("salt", salt);
        userSalts.insertOne(saltDoc);
        
        return employeeId;
    }

    @Override
    public Employee getEmployee(int employeeId) {
        Document doc = employees.find(Filters.eq("_id", employeeId)).first();
        if (doc == null) return null;
        
        if ("Manager".equals(doc.getString("role"))) {
            return new Manager(employeeId, doc.getString("name"), 
                    doc.getString("email"), doc.getInteger("branch_id"), 
                    doc.getString("password"));
        } else {
            return new Employee.Builder(
                    doc.getString("name"),
                    doc.getString("email"),
                    doc.getString("role"),
                    doc.getInteger("branch_id"))
                    .id(employeeId)
                    .password(doc.getString("password"))
                    .build();
        }
    }

    @Override
    public Employee getEmployeeByEmail(String email) {
        Document doc = employees.find(Filters.eq("email", email)).first();
        if (doc == null) return null;
        
        int employeeId = getIdAsInteger(doc);
        if ("Manager".equals(doc.getString("role"))) {
            return new Manager(employeeId, doc.getString("name"), 
                    doc.getString("email"), doc.getInteger("branch_id"), 
                    doc.getString("password"));
        } else {
            return new Employee.Builder(
                    doc.getString("name"),
                    doc.getString("email"),
                    doc.getString("role"),
                    doc.getInteger("branch_id"))
                    .id(employeeId)
                    .password(doc.getString("password"))
                    .build();
        }
    }

    @Override
    public Manager getManager(int employeeId) {
        Document doc = employees.find(Filters.and(
                Filters.eq("_id", employeeId),
                Filters.eq("role", "Manager")
        )).first();
        
        if (doc != null) {
            return new Manager(employeeId, doc.getString("name"), 
                    doc.getString("email"), doc.getInteger("branch_id"), 
                    doc.getString("password"));
        }
        return null;
    }

    @Override
    public String getSaltForEmployee(int employeeId) {
        Document doc = userSalts.find(Filters.and(
                Filters.eq("user_id", employeeId),
                Filters.in("user_type", "MANAGER", "EMPLOYEE")
        )).first();
        return doc != null ? doc.getString("salt") : null;
    }

    // Account operations
    @Override
    public int saveAccount(SavingsAccount account) {
        Document doc = new Document("customer_id", account.getCustomerId())
                .append("balance", account.getBalance().doubleValue())
                .append("account_type", "Savings")
                .append("branch_id", account.getBranchId());
        
        // If account has an account number, use it; otherwise let MongoDB generate ObjectId
        if (account.getAccountNo() > 0) {
            doc.append("_id", account.getAccountNo());
        }
        
        accounts.insertOne(doc);
        return getIdAsInteger(doc);
    }

    @Override
    public SavingsAccount getAccount(int accountNo) {
        Document doc = accounts.find(Filters.eq("_id", accountNo)).first();
        if (doc == null) return null;
        
        return new SavingsAccount.Builder(
                doc.getInteger("customer_id"),
                doc.getInteger("branch_id"))
                .accountNo(accountNo)
                .balance(BigDecimal.valueOf(doc.getDouble("balance")))
                .build();
    }

    @Override
    public void updateAccount(SavingsAccount account) {
        accounts.updateOne(
                Filters.eq("_id", account.getAccountNo()),
                Updates.set("balance", account.getBalance().doubleValue())
        );
    }

    @Override
    public boolean deleteAccount(int accountNo) {
        return accounts.deleteOne(Filters.eq("_id", accountNo)).getDeletedCount() > 0;
    }

    @Override
    public List<SavingsAccount> getAccountsByBranch(int branchId) {
        List<SavingsAccount> result = new ArrayList<>();
        for (Document doc : accounts.find(Filters.eq("branch_id", branchId))) {
            int accountNo = getIdAsInteger(doc);
            SavingsAccount account = new SavingsAccount.Builder(
                    doc.getInteger("customer_id"),
                    doc.getInteger("branch_id"))
                    .accountNo(accountNo)
                    .balance(BigDecimal.valueOf(doc.getDouble("balance")))
                    .build();
            result.add(account);
        }
        return result;
    }

    @Override
    public List<SavingsAccount> getAccountsByCustomer(int customerId) {
        List<SavingsAccount> result = new ArrayList<>();
        for (Document doc : accounts.find(Filters.eq("customer_id", customerId))) {
            int accountNo = getIdAsInteger(doc);
            SavingsAccount account = new SavingsAccount.Builder(
                    customerId,
                    doc.getInteger("branch_id"))
                    .accountNo(accountNo)
                    .balance(BigDecimal.valueOf(doc.getDouble("balance")))
                    .build();
            result.add(account);
        }
        return result;
    }

    //Transaction operations
    @Override
    public boolean withdrawFromAccount(int accountNo, BigDecimal amount) {
        Document account = accounts.find(Filters.eq("_id", accountNo)).first();
        if (account == null) return false;
        
        BigDecimal currentBalance = BigDecimal.valueOf(account.getDouble("balance"));
        BigDecimal minBalance = new BigDecimal("100.00");
        
        if (currentBalance.subtract(amount).compareTo(minBalance) >= 0) {
            accounts.updateOne(
                    Filters.eq("_id", accountNo),
                    Updates.set("balance", currentBalance.subtract(amount).doubleValue())
            );
            return true;
        }
        return false;
    }

    @Override
    public boolean depositToAccount(int accountNo, BigDecimal amount) {
        Document account = accounts.find(Filters.eq("_id", accountNo)).first();
        if (account == null) return false;
        
        BigDecimal currentBalance = BigDecimal.valueOf(account.getDouble("balance"));
        accounts.updateOne(
                Filters.eq("_id", accountNo),
                Updates.set("balance", currentBalance.add(amount).doubleValue())
        );
        return true;
    }

    // Branch operations
    @Override
    public void saveBranch(Branch branch) {
        Document doc = new Document("_id", branch.getBranchId())
                .append("branch_name", branch.getBranchName())
                .append("address", branch.getAddress());
        branches.insertOne(doc);
    }

    @Override
    public Branch getBranch(int branchId) {
        Document doc = branches.find(Filters.eq("_id", branchId)).first();
        if (doc == null) return null;
        
        return new Branch(branchId, doc.getString("branch_name"), doc.getString("address"));
    }

    // Joint account operations
    @Override
    public int addCustomerToAccount(int customerId, int accountNo, String role) {
        Document doc = new Document("customer_id", customerId)
                .append("account_no", accountNo)
                .append("account_role", role);
        customerAccounts.insertOne(doc);
        return getIdAsInteger(doc);
    }

    @Override
    public boolean removeCustomerFromAccount(int customerId, int accountNo) {
        return customerAccounts.deleteOne(Filters.and(
                Filters.eq("customer_id", customerId),
                Filters.eq("account_no", accountNo)
        )).getDeletedCount() > 0;
    }

    @Override
    public List<Customer> getCustomersByAccount(int accountNo) {
        List<Customer> result = new ArrayList<>();
        for (Document doc : customerAccounts.find(Filters.eq("account_no", accountNo))) {
            Customer customer = getCustomer(doc.getInteger("customer_id"));
            if (customer != null) {
                result.add(customer);
            }
        }
        return result;
    }

    // Method to clear all collections (useful for testing)
    public void clearAllCollections() {
        customers.deleteMany(new Document());
        employees.deleteMany(new Document());
        accounts.deleteMany(new Document());
        branches.deleteMany(new Document());
        customerAccounts.deleteMany(new Document());
        userSalts.deleteMany(new Document());
        System.out.println("All collections cleared.");
    }
    
    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }
}