package storage;

import entity.*;

import java.math.BigDecimal;
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

    /**
     * Saves a new customer to storage.
     *
     * @param customer the customer to save
     * @return the generated customer ID
     */
    @Override
    public int saveCustomer(Customer customer) {
        if (customer.getId() == 0) {
            customer.setId(nextCustomerId++);
        }
        customers.put(customer.getId(), customer);
        return customer.getId();
    }

    /**
     * Saves a new customer with password for login capability.
     *
     * @param customer the customer to save
     * @param salt the password salt for security
     * @return the generated customer ID
     */
    @Override
    public int saveCustomerWithPassword(Customer customer, String salt) {
        int customerId = saveCustomer(customer);
        customerSalts.put(customer.getEmail(), salt);
        return customerId;
    }

    /**
     * Retrieves a customer by ID.
     *
     * @param customerId the customer ID
     * @return the customer or null if not found
     */
    @Override
    public Customer getCustomer(int customerId) {
        return customers.get(customerId);
    }

    /**
     * Retrieves a customer by email address.
     *
     * @param email the customer's email
     * @return the customer or null if not found
     */
    @Override
    public Customer getCustomerByEmail(String email) {
        return customers.values().stream()
                .filter(customer -> email.equals(customer.getEmail()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Deletes a customer by ID.
     *
     * @param customerId the customer ID to delete
     * @return true if deletion was successful, false otherwise
     */
    @Override
    public boolean deleteCustomer(int customerId) {
        Customer removed = customers.remove(customerId);
        if (removed != null) {
            customerSalts.remove(removed.getEmail());
            return true;
        }
        return false;
    }

    /**
     * Gets all customers for a specific branch.
     *
     * @param branchId the branch ID
     * @return list of customers in the branch
     */
    @Override
    public List<Customer> getCustomersByBranch(int branchId) {
        return customers.values().stream()
                .filter(customer -> customer.getBranchId() == branchId)
                .collect(Collectors.toList());
    }

    /**
     * Gets the password salt for a customer.
     *
     * @param customerId the customer ID
     * @return the password salt or null if not found
     */
    @Override
    public String getSaltForCustomer(int customerId) {
        Customer customer = customers.get(customerId);
        if (customer != null) {
            return customerSalts.get(customer.getEmail());
        }
        return null;
    }

    // Employee operations
    /**
     * Saves a new employee to storage.
     *
     * @param employee the employee to save
     * @return the generated employee ID
     */
    @Override
    public int saveEmployee(Employee employee) {
        if (employee.getId() == 0) {
            employee.setId(nextEmployeeId++);
        }
        employees.put(employee.getId(), employee);
        return employee.getId();
    }

    /**
     * Saves a new employee with password for login capability.
     *
     * @param employee the employee to save
     * @param salt the password salt for security
     * @return the generated employee ID
     */
    @Override
    public int saveEmployeeWithPassword(Employee employee, String salt) {
        int employeeId = saveEmployee(employee);
        employeeSalts.put(employee.getEmail(), salt);
        return employeeId;
    }

    /**
     * Retrieves an employee by ID.
     *
     * @param employeeId the employee ID
     * @return the employee or null if not found
     */
    @Override
    public Employee getEmployee(int employeeId) {
        return employees.get(employeeId);
    }

    /**
     * Retrieves an employee by email address.
     *
     * @param email the employee's email
     * @return the employee or null if not found
     */
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
    /**
     * Saves a new account to storage.
     *
     * @param account the account to save
     * @return the generated account number
     */
    @Override
    public int saveAccount(SavingsAccount account) {
        if (account.getAccountNo() == 0) {
            int accountNo = nextAccountId++;
            SavingsAccount newAccount = new SavingsAccount.Builder(account.getCustomerId(), account.getBranchId())
                                                    .accountNo(accountNo)
                                                    .balance(account.getBalance())
                                                    .build();
            accounts.put(accountNo, newAccount);
            return accountNo;
        } else {
            accounts.put(account.getAccountNo(), account);
            return account.getAccountNo();
        }
    }

    /**
     * Retrieves an account by account number.
     *
     * @param accountNo the account number
     * @return the account or null if not found
     */
    @Override
    public SavingsAccount getAccount(int accountNo) {
        return accounts.get(accountNo);
    }

    /**
     * Updates an existing account.
     *
     * @param account the account to update
     */
    @Override
    public void updateAccount(SavingsAccount account) {
        accounts.put(account.getAccountNo(), account);
    }

    /**
     * Deletes an account by account number.
     *
     * @param accountNo the account number to delete
     * @return true if deletion was successful, false otherwise
     */
    @Override
    public boolean deleteAccount(int accountNo) {
        return accounts.remove(accountNo) != null;
    }

    /**
     * Gets all accounts for a specific branch.
     *
     * @param branchId the branch ID
     * @return list of accounts in the branch
     */
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
    /**
     * Saves a branch to storage.
     *
     * @param branch the branch to save
     */
    @Override
    public void saveBranch(Branch branch) {
        branches.put(branch.getBranchId(), branch);
    }
    

    /**
     * Retrieves a branch by ID.
     *
     * @param branchId the branch ID
     * @return the branch or null if not found
     */
    @Override
    public Branch getBranch(int branchId) {
        return branches.get(branchId);
    }

    // Transaction operations (for consistency with DatabaseStorage)
    /**
     * Withdraws money from an account.
     *
     * @param accountNo the account number
     * @param amount the amount to withdraw
     * @return true if withdrawal was successful, false otherwise
     */
    @Override
    public boolean withdrawFromAccount(int accountNo, BigDecimal amount) {
        SavingsAccount account = accounts.get(accountNo);
        if (account != null && account.canWithdraw(amount)) {
            if (account.withdraw(amount)) {
                accounts.put(accountNo, account);
                return true;
            }
        }
        return false;
    }

    /**
     * Deposits money to an account.
     *
     * @param accountNo the account number
     * @param amount the amount to deposit
     * @return true if deposit was successful, false otherwise
     */
    @Override
    public boolean depositToAccount(int accountNo, BigDecimal amount) {
        SavingsAccount account = accounts.get(accountNo);
        if (account != null) {
            account.deposit(amount);
            accounts.put(accountNo, account);
            return true;
        }
        return false;
    }

    /**
     * Updates an existing customer's information.
     *
     * @param customer the customer to update
     * @throws SQLException if database operation fails
     */
	@Override
	public void updateCustomer(Customer customer) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	/**
     * Adds a customer to an existing account as joint holder.
     * Default implementation throws UnsupportedOperationException.
     *
     * @param customerId the customer ID to add
     * @param accountNo the account number
     * @param role the customer's role (PRIMARY, JOINT)
     * @return the result status
     * @throws UnsupportedOperationException if not supported by implementation
     */
	@Override
	public int addCustomerToAccount(int customerId, int accountNo, String role) {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
     * Removes a customer from a joint account.
     * Default implementation throws UnsupportedOperationException.
     *
     * @param customerId the customer ID to remove
     * @param accountNo the account number
     * @return true if removal was successful, false otherwise
     * @throws UnsupportedOperationException if not supported by implementation
     */

	@Override
	public boolean removeCustomerFromAccount(int customerId, int accountNo) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
     * Gets all customers who are holders of an account.
     * Default implementation throws UnsupportedOperationException.
     *
     * @param accountNo the account number
     * @return list of account holders
     * @throws UnsupportedOperationException if not supported by implementation
     */
	@Override
	public List<Customer> getCustomersByAccount(int accountNo) {
		// TODO Auto-generated method stub
		return null;
	}
    
}