package storage;

import entity.*;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class CollectionStorage extends AbstractDataStorage {
    private final Map<Integer, Customer> customers = new HashMap<>();
    private final Map<Integer, Employee> employees = new HashMap<>();
    private final Map<Integer, SavingsAccount> accounts = new HashMap<>();
    private final Map<Integer, Branch> branches = new HashMap<>();
    private final Map<String, String> customerSalts = new HashMap<>(); // email -> salt
    private final Map<String, String> employeeSalts = new HashMap<>(); // email -> salt
    
    private int nextCustomerId = 1;
    private int nextEmployeeId = 1;
    private int nextAccountId = 1;

    // Customer operations
    @Override
    public int saveCustomer(Customer customer) {
        if (customer.getId() == 0) {
            customer.setId(nextCustomerId++);
        }
        customers.put(customer.getId(), customer);
        return customer.getId();
    }

    @Override
    public int saveCustomerWithPassword(Customer customer, String salt) {
        int customerId = saveCustomer(customer);
        customerSalts.put(customer.getEmail(), salt);
        return customerId;
    }

    @Override
    public Customer getCustomer(int customerId) {
        return customers.get(customerId);
    }

    @Override
    public Customer getCustomerByEmail(String email) {
        return customers.values().stream()
                .filter(customer -> email.equals(customer.getEmail()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public boolean deleteCustomer(int customerId) {
        Customer removed = customers.remove(customerId);
        if (removed != null) {
            customerSalts.remove(removed.getEmail());
            return true;
        }
        return false;
    }

    @Override
    public List<Customer> getCustomersByBranch(int branchId) {
        return customers.values().stream()
                .filter(customer -> customer.getBranchId() == branchId)
                .collect(Collectors.toList());
    }

    @Override
    public String getSaltForCustomer(int customerId) {
        Customer customer = customers.get(customerId);
        if (customer != null) {
            return customerSalts.get(customer.getEmail());
        }
        return null;
    }

    // Employee operations
    @Override
    public int saveEmployee(Employee employee) {
        if (employee.getId() == 0) {
            employee.setId(nextEmployeeId++);
        }
        employees.put(employee.getId(), employee);
        return employee.getId();
    }

    @Override
    public int saveEmployeeWithPassword(Employee employee, String salt) {
        int employeeId = saveEmployee(employee);
        employeeSalts.put(employee.getEmail(), salt);
        return employeeId;
    }

    @Override
    public Employee getEmployee(int employeeId) {
        return employees.get(employeeId);
    }

    @Override
    public Employee getEmployeeByEmail(String email) {
        return employees.values().stream()
                .filter(employee -> email.equals(employee.getEmail()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Manager getManager(int employeeId) {
        Employee employee = employees.get(employeeId);
        return (employee instanceof Manager) ? (Manager) employee : null;
    }

    @Override
    public String getSaltForEmployee(int employeeId) {
        Employee employee = employees.get(employeeId);
        if (employee != null) {
            return employeeSalts.get(employee.getEmail());
        }
        return null;
    }

    // Account operations
    @Override
    public int saveAccount(SavingsAccount account) {
        if (account.getAccountNo() == 0) {
            int accountNo = nextAccountId++;
            SavingsAccount newAccount = new SavingsAccount(accountNo, account.getCustomerId(), 
                    account.getBalance(), account.getBranchId());
            accounts.put(accountNo, newAccount);
            return accountNo;
        } else {
            accounts.put(account.getAccountNo(), account);
            return account.getAccountNo();
        }
    }

    @Override
    public SavingsAccount getAccount(int accountNo) {
        return accounts.get(accountNo);
    }

    @Override
    public void updateAccount(SavingsAccount account) {
        accounts.put(account.getAccountNo(), account);
    }

    @Override
    public boolean deleteAccount(int accountNo) {
        return accounts.remove(accountNo) != null;
    }

    @Override
    public List<SavingsAccount> getAccountsByBranch(int branchId) {
        return accounts.values().stream()
                .filter(account -> account.getBranchId() == branchId)
                .collect(Collectors.toList());
    }

    @Override
    public List<SavingsAccount> getAccountsByCustomer(int customerId) {
        return accounts.values().stream()
                .filter(account -> account.getCustomerId() == customerId)
                .collect(Collectors.toList());
    }

    // Branch operations
    @Override
    public void saveBranch(Branch branch) {
        branches.put(branch.getBranchId(), branch);
    }

    @Override
    public Branch getBranch(int branchId) {
        return branches.get(branchId);
    }

    // Transaction operations (for consistency with DatabaseStorage)
    @Override
    public boolean withdrawFromAccount(int accountNo, double amount) {
        SavingsAccount account = accounts.get(accountNo);
        if (account != null && account.canWithdraw(amount)) {
            if (account.withdraw(amount)) {
                accounts.put(accountNo, account);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean depositToAccount(int accountNo, double amount) {
        SavingsAccount account = accounts.get(accountNo);
        if (account != null) {
            account.deposit(amount);
            accounts.put(accountNo, account);
            return true;
        }
        return false;
    }

	@Override
	public void updateCustomer(Customer customer) throws SQLException {
		// TODO Auto-generated method stub
		
	}
    
}