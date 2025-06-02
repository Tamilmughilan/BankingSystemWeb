package storage;

import entity.*;

import java.sql.SQLException;
import java.util.List;

public abstract class AbstractDataStorage implements DataStorage {

    // Common email validation logic
    protected boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    // Customer operations
    public abstract int saveCustomer(Customer customer);
    public abstract int saveCustomerWithPassword(Customer customer, String salt);
    public abstract Customer getCustomer(int customerId);
    public abstract Customer getCustomerByEmail(String email);
    public abstract boolean deleteCustomer(int customerId);
    public abstract List<Customer> getCustomersByBranch(int branchId);
    public abstract String getSaltForCustomer(int customerId);

    // Employee operations
    public abstract int saveEmployee(Employee employee);
    public abstract int saveEmployeeWithPassword(Employee employee, String salt);
    public abstract Employee getEmployee(int employeeId);
    public abstract Employee getEmployeeByEmail(String email);
    public abstract Manager getManager(int employeeId);
    @Override
    public abstract String getSaltForEmployee(int employeeId) throws SQLException; 
    
    // Account operations
    public abstract int saveAccount(SavingsAccount account);
    public abstract SavingsAccount getAccount(int accountNo);
    public abstract void updateAccount(SavingsAccount account);
    public abstract boolean deleteAccount(int accountNo);
    public abstract List<SavingsAccount> getAccountsByBranch(int branchId);
    public abstract List<SavingsAccount> getAccountsByCustomer(int customerId);
    
    // Branch operations
    public abstract void saveBranch(Branch branch);
    public abstract Branch getBranch(int branchId);

    // Transaction operations
    public abstract boolean withdrawFromAccount(int accountNo, double amount);
    public abstract boolean depositToAccount(int accountNo, double amount);

    @Override
    public abstract void updateCustomer(Customer customer) throws SQLException;
}