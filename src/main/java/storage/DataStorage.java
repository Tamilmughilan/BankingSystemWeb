package storage;

import entity.*;

import java.sql.SQLException;
import java.util.List;

public interface DataStorage {
    // Customer operations
    int saveCustomer(Customer customer);
    int saveCustomerWithPassword(Customer customer, String salt);
    Customer getCustomer(int customerId);
    Customer getCustomerByEmail(String email);
    boolean deleteCustomer(int customerId);
    List<Customer> getCustomersByBranch(int branchId);
    String getSaltForCustomer(int customerId);
    void updateCustomer(Customer customer) throws SQLException;
    
    // Employee operations
    int saveEmployee(Employee employee);
    int saveEmployeeWithPassword(Employee employee, String salt);
    Employee getEmployee(int employeeId);
    Employee getEmployeeByEmail(String email);
    Manager getManager(int employeeId);
    String getSaltForEmployee(int employeeId);

    // Account operations
    int saveAccount(SavingsAccount account);
    SavingsAccount getAccount(int accountNo);
    void updateAccount(SavingsAccount account);
    boolean deleteAccount(int accountNo);
    List<SavingsAccount> getAccountsByBranch(int branchId);
    List<SavingsAccount> getAccountsByCustomer(int customerId);

    // Branch operations
    void saveBranch(Branch branch);
    Branch getBranch(int branchId);
    
    // Transaction operations
    boolean withdrawFromAccount(int accountNo, double amount);
    boolean depositToAccount(int accountNo, double amount);
}