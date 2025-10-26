package storage;

import entity.*;

import java.sql.*;
import java.util.ArrayList;
import java.math.BigDecimal;
import java.util.List;

public class DatabaseStorage extends AbstractDataStorage {
    private final DatabaseConnection dbConnection;

    public DatabaseStorage() throws SQLException {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    /**
     * Saves a new customer to storage.
     *
     * @param customer the customer to save
     * @return the generated customer ID
     */
    @Override
    public int saveCustomer(Customer customer) {
        String sql = "INSERT INTO customers (name, phone, email, branch_id) VALUES (?, ?, ?, ?)";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, customer.getName());
            stmt.setString(2, customer.getPhone());
            stmt.setString(3, customer.getEmail());
            stmt.setInt(4, customer.getBranchId());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int customerId = generatedKeys.getInt(1);
                        customer.setId(customerId);
                        return customerId;
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error saving customer", e);
        }
        return 0;
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
        String sql = "INSERT INTO customers (name, phone, email, branch_id, password) VALUES (?, ?, ?, ?, ?)";
        String saltSql = "INSERT INTO user_salts (user_id, user_type, salt) VALUES (?, 'CUSTOMER', ?)";
        
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            conn.setAutoCommit(false);
            
            // Insert customer
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, customer.getName());
                stmt.setString(2, customer.getPhone());
                stmt.setString(3, customer.getEmail());
                stmt.setInt(4, customer.getBranchId());
                stmt.setString(5, customer.getPassword());
                
                int affectedRows = stmt.executeUpdate();
                if (affectedRows > 0) {
                    try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            int customerId = generatedKeys.getInt(1);
                            customer.setId(customerId);
                            
                            // Insert salt
                            try (PreparedStatement saltStmt = conn.prepareStatement(saltSql)) {
                                saltStmt.setInt(1, customerId);
                                saltStmt.setString(2, salt);
                                saltStmt.executeUpdate();
                            }
                            
                            conn.commit();
                            return customerId;
                        }
                    }
                }
            }
            
            conn.rollback();
            return 0;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw new RuntimeException("Error saving customer with password", e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * Updates an existing customer's information.
     *
     * @param customer the customer to update
     * @throws SQLException if database operation fails
     */
    @Override
    public void updateCustomer(Customer customer) throws SQLException {
        String sql = "UPDATE customers SET name=?, phone=?, email=?, branch_id=? WHERE customer_id=?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, customer.getName());
            stmt.setString(2, customer.getPhone());
            stmt.setString(3, customer.getEmail());
            stmt.setInt(4, customer.getBranchId());
            stmt.setInt(5, customer.getId());
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Customer update failed - no rows affected. Customer may not exist.");
            }
        }
    }


    /**
     * Retrieves a customer by ID.
     *
     * @param customerId the customer ID
     * @return the customer or null if not found
     */
    @Override
    public Customer getCustomer(int customerId) {
        String sql = "SELECT * FROM customers WHERE customer_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, customerId);
            try (ResultSet rs = stmt.executeQuery()) {
            	if (rs.next()) {
                    return new Customer.Builder(
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getInt("branch_id")
                    )
                    .id(rs.getInt("customer_id"))
                    .phone(rs.getString("phone"))
                    .password(rs.getString("password"))
                    .build();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving customer", e);
        }
        return null;
    }
    
    /**
     * Retrieves a customer by email address.
     *
     * @param email the customer's email
     * @return the customer or null if not found
     */
    @Override
    public Customer getCustomerByEmail(String email) {
        String sql = "SELECT * FROM customers WHERE email = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
            	if (rs.next()) {
                    return new Customer.Builder(
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getInt("branch_id")
                    )
                    .id(rs.getInt("customer_id"))
                    .phone(rs.getString("phone"))
                    .password(rs.getString("password"))
                    .build();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving customer by email", e);
        }
        return null;
    }

    /**
     * Deletes a customer by ID.
     *
     * @param customerId the customer ID to delete
     * @return true if deletion was successful, false otherwise
     */
    @Override
    public boolean deleteCustomer(int customerId) {
        String sql = "DELETE FROM customers WHERE customer_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, customerId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting customer", e);
        }
    }
    

    /**
     * Gets all customers for a specific branch.
     *
     * @param branchId the branch ID
     * @return list of customers in the branch
     */
    @Override
    public List<Customer> getCustomersByBranch(int branchId) {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM customers WHERE branch_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, branchId);
            try (ResultSet rs = stmt.executeQuery()) {
            	while (rs.next()) {
                    Customer customer = new Customer.Builder(
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getInt("branch_id")
                    )
                    .id(rs.getInt("customer_id"))
                    .phone(rs.getString("phone"))
                    .password(rs.getString("password"))
                    .build();
                    customers.add(customer);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving customers by branch", e);
        }
        return customers;
    }

    /**
     * Gets the password salt for a customer.
     *
     * @param customerId the customer ID
     * @return the password salt or null if not found
     */
    @Override
    public String getSaltForCustomer(int customerId) {
        String sql = "SELECT salt FROM user_salts WHERE user_id = ? AND user_type = 'CUSTOMER'";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, customerId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("salt");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving salt for customer", e);
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
        String sql = "INSERT INTO employees (name, email, role, branch_id) VALUES (?, ?, ?, ?)";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, employee.getName());
            stmt.setString(2, employee.getEmail());
            stmt.setString(3, employee.getRole());
            stmt.setInt(4, employee.getBranchId());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int employeeId = generatedKeys.getInt(1);
                        employee.setId(employeeId);
                        return employeeId;
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error saving employee", e);
        }
        return 0;
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
        String sql = "INSERT INTO employees (name, email, role, branch_id, password) VALUES (?, ?, ?, ?, ?)";
        String saltSql = "INSERT INTO user_salts (user_id, user_type, salt) VALUES (?, 'EMPLOYEE', ?)";
        
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            conn.setAutoCommit(false);
            
            // Insert employee
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, employee.getName());
                stmt.setString(2, employee.getEmail());
                stmt.setString(3, employee.getRole());
                stmt.setInt(4, employee.getBranchId());
                stmt.setString(5, employee.getPassword());
                
                int affectedRows = stmt.executeUpdate();
                if (affectedRows > 0) {
                    try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            int employeeId = generatedKeys.getInt(1);
                            employee.setId(employeeId);
                            
                            // Insert salt
                            try (PreparedStatement saltStmt = conn.prepareStatement(saltSql)) {
                                saltStmt.setInt(1, employeeId);
                                saltStmt.setString(2, salt);
                                saltStmt.executeUpdate();
                            }
                            
                            conn.commit();
                            return employeeId;
                        }
                    }
                }
            }
            
            conn.rollback();
            return 0;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw new RuntimeException("Error saving employee with password", e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * Retrieves an employee by ID.
     *
     * @param employeeId the employee ID
     * @return the employee or null if not found
     */
    @Override
    public Employee getEmployee(int employeeId) {
        String sql = "SELECT * FROM employees WHERE employee_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, employeeId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String role = rs.getString("role");
                    String name = rs.getString("name");
                    String email = rs.getString("email");
                    int branchId = rs.getInt("branch_id");
                    String password = rs.getString("password");
                    int id = rs.getInt("employee_id");

                    if ("MANAGER".equalsIgnoreCase(role)) {
                        return new Manager(id, name, email, branchId, password);
                    } else {
                        return new Employee.Builder(name, email, role, branchId)
                            .id(id)
                            .password(password)
                            .build();
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving employee", e);
        }
        return null;
    }

    /**
     * Retrieves an employee by email address.
     *
     * @param email the employee's email
     * @return the employee or null if not found
     */
    @Override
    public Employee getEmployeeByEmail(String email) {
        String sql = "SELECT * FROM employees WHERE email = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String role = rs.getString("role");
                    String name = rs.getString("name");
                    int branchId = rs.getInt("branch_id");
                    String password = rs.getString("password");
                    int id = rs.getInt("employee_id");

                    if ("MANAGER".equalsIgnoreCase(role)) {
                        return new Manager(id, name, email, branchId, password);
                    } else {
                        return new Employee.Builder(name, email, role, branchId)
                            .id(id)
                            .password(password)
                            .build();
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving employee by email", e);
        }
        return null;
    }


    @Override
    public Manager getManager(int employeeId) {
        Employee employee = getEmployee(employeeId);
        return (employee instanceof Manager) ? (Manager) employee : null;
    }

    @Override
    public String getSaltForEmployee(int employeeId) {
        String sql = "SELECT salt FROM user_salts WHERE user_id = ? AND user_type IN ('EMPLOYEE', 'MANAGER')";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, employeeId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getString("salt") : null;
            }
        } catch (SQLException e) {
            e.printStackTrace(); 
            return null; 
        }
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
        String sql = "INSERT INTO accounts (customer_id, balance, account_type, branch_id) VALUES (?, ?, ?, ?)";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, account.getCustomerId());
            stmt.setBigDecimal(2, account.getBalance());
            stmt.setString(3, account.getAccountType());
            stmt.setInt(4, account.getBranchId());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
            return 0; // Return 0 if account creation failed
        } catch (SQLException e) {
            // Check if it's a foreign key constraint violation 
            if (e.getMessage().contains("foreign key constraint") || 
                e.getMessage().contains("Cannot add or update a child row")) {
                throw new RuntimeException("Customer ID does not exist", e);
            }
            throw new RuntimeException("Error saving account", e);
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
        String sql = "SELECT * FROM accounts WHERE account_no = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, accountNo);
            try (ResultSet rs = stmt.executeQuery()) {
            	if (rs.next()) {
                    return new SavingsAccount.Builder(
                        rs.getInt("customer_id"),
                        rs.getInt("branch_id")
                    )
                    .accountNo(rs.getInt("account_no"))
                    .balance(rs.getBigDecimal("balance"))
                    .build();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving account", e);
        }
        return null;
    }

    /**
     * Updates an existing account.
     *
     * @param account the account to update
     */
    @Override
    public void updateAccount(SavingsAccount account) {
        String sql = "UPDATE accounts SET balance = ? WHERE account_no = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setBigDecimal(1, account.getBalance());
            stmt.setInt(2, account.getAccountNo());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating account", e);
        }
    }

    /**
     * Deletes an account by account number.
     *
     * @param accountNo the account number to delete
     * @return true if deletion was successful, false otherwise
     */
    @Override
    public boolean deleteAccount(int accountNo) {
        String sql = "DELETE FROM accounts WHERE account_no = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, accountNo);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting account", e);
        }
    }

    /**
     * Gets all accounts for a specific branch.
     *
     * @param branchId the branch ID
     * @return list of accounts in the branch
     */
    @Override
    public List<SavingsAccount> getAccountsByBranch(int branchId) {
        List<SavingsAccount> accounts = new ArrayList<>();
        String sql = "SELECT * FROM accounts WHERE branch_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, branchId);
            try (ResultSet rs = stmt.executeQuery()) {
            	while (rs.next()) {
                    accounts.add(new SavingsAccount.Builder(
                        rs.getInt("customer_id"),
                        rs.getInt("branch_id")
                    )
                    .accountNo(rs.getInt("account_no"))
                    .balance(rs.getBigDecimal("balance"))
                    .build());
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving accounts by branch", e);
        }
        return accounts;
    }

    @Override
    public List<SavingsAccount> getAccountsByCustomer(int customerId) {
        List<SavingsAccount> accounts = new ArrayList<>();
        String sql = "SELECT a.* FROM accounts a " +
                     "JOIN customer_accounts ca ON a.account_no = ca.account_no " +
                     "WHERE ca.customer_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, customerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    accounts.add(new SavingsAccount.Builder(
                        rs.getInt("customer_id"),
                        rs.getInt("branch_id")
                    )
                    .accountNo(rs.getInt("account_no"))
                    .balance(rs.getBigDecimal("balance"))
                    .build());
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving accounts by customer", e);
        }
        return accounts;
    }

    // Branch operations
    /**
     * Saves a branch to storage.
     *
     * @param branch the branch to save
     */
    @Override
    public void saveBranch(Branch branch) {
        String sql = "INSERT INTO branches (branch_name, address) VALUES (?, ?) ON DUPLICATE KEY UPDATE address = VALUES(address)";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, branch.getBranchName());
            stmt.setString(2, branch.getAddress());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving branch", e);
        }
    }

    /**
     * Retrieves a branch by ID.
     *
     * @param branchId the branch ID
     * @return the branch or null if not found
     */
    @Override
    public Branch getBranch(int branchId) {
        String sql = "SELECT * FROM branches WHERE branch_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, branchId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Branch(
                        rs.getInt("branch_id"),
                        rs.getString("branch_name"),
                        rs.getString("address")
                    );
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving branch", e);
        }
        return null;
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
    public int addCustomerToAccount(int customerId, int accountNo, String role) {
        String sql = "INSERT INTO customer_accounts (customer_id, account_no, account_role) VALUES (?, ?, ?)";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, customerId);
            stmt.setInt(2, accountNo);
            stmt.setString(3, role);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error adding customer to account", e);
        }
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

    public boolean removeCustomerFromAccount(int customerId, int accountNo) {
        String sql = "DELETE FROM customer_accounts WHERE customer_id = ? AND account_no = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, customerId);
            stmt.setInt(2, accountNo);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error removing customer from account", e);
        }
    }
    
    /**
     * Gets all customers who are holders of an account.
     * Default implementation throws UnsupportedOperationException.
     *
     * @param accountNo the account number
     * @return list of account holders
     * @throws UnsupportedOperationException if not supported by implementation
     */
    public List<Customer> getCustomersByAccount(int accountNo) {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT c.* FROM customers c " +
                     "JOIN customer_accounts ca ON c.customer_id = ca.customer_id " +
                     "WHERE ca.account_no = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, accountNo);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Customer customer = new Customer.Builder(
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getInt("branch_id")
                    )
                    .id(rs.getInt("customer_id"))
                    .phone(rs.getString("phone"))
                    .password(rs.getString("password"))
                    .build();
                    customers.add(customer);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving customers by account", e);
        }
        return customers;
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
        return depositToAccount(accountNo, amount, null, null, null);
    }

    // Overloaded method with user context for logging
    public boolean depositToAccount(int accountNo, BigDecimal amount, Integer userId, 
                                   TransactionLog.UserType userType, String description) {
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            conn.setAutoCommit(false);
            
            BigDecimal balanceBefore = null;
            
            //get current balance and check if account exists
            String checkSql = "SELECT balance FROM accounts WHERE account_no = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, accountNo);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (!rs.next()) {
                        // Log failed transaction
                        TransactionLog failedLog = new TransactionLog(accountNo, 
                            TransactionLog.TransactionType.DEPOSIT, amount, 
                            BigDecimal.ZERO, BigDecimal.ZERO, 
                            TransactionLog.TransactionStatus.FAILED,
                            "Account not found", userId, userType);
                        logTransactionInTransaction(conn, failedLog);
                        conn.commit();
                        return false;
                    }
                    balanceBefore = rs.getBigDecimal("balance");
                }
            }
            
            // Perform the deposit
            String sql = "UPDATE accounts SET balance = balance + ? WHERE account_no = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setBigDecimal(1, amount);
                stmt.setInt(2, accountNo);
                
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    BigDecimal balanceAfter = balanceBefore.add(amount);
                    
                    // Log successful transaction
                    TransactionLog successLog = new TransactionLog(accountNo, 
                        TransactionLog.TransactionType.DEPOSIT, amount, 
                        balanceBefore, balanceAfter, 
                        TransactionLog.TransactionStatus.SUCCESS,
                        description, userId, userType);
                    
                    if (logTransactionInTransaction(conn, successLog)) {
                        conn.commit();
                        return true;
                    } else {
                        conn.rollback();
                        return false;
                    }
                } else {
                    // Log failed transaction
                    TransactionLog failedLog = new TransactionLog(accountNo, 
                        TransactionLog.TransactionType.DEPOSIT, amount, 
                        balanceBefore, balanceBefore, 
                        TransactionLog.TransactionStatus.FAILED,
                        "Update failed", userId, userType);
                    logTransactionInTransaction(conn, failedLog);
                    conn.rollback();
                    return false;
                }
            }
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    // Log failed transaction
                    TransactionLog failedLog = new TransactionLog(accountNo, 
                        TransactionLog.TransactionType.DEPOSIT, amount, 
                        BigDecimal.ZERO, BigDecimal.ZERO, 
                        TransactionLog.TransactionStatus.FAILED,
                        "Database error: " + e.getMessage(), userId, userType);
                    logTransactionInTransaction(conn, failedLog);
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw new RuntimeException("Error depositing to account", e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Updated withdrawal method with transaction logging
    @Override
    /**
     * Withdraws money from an account.
     *
     * @param accountNo the account number
     * @param amount the amount to withdraw
     * @return true if withdrawal was successful, false otherwise
     */
    public boolean withdrawFromAccount(int accountNo, BigDecimal amount) {
        return withdrawFromAccount(accountNo, amount, null, null, null);
    }

    // Overloaded method with user context for logging
    public boolean withdrawFromAccount(int accountNo, BigDecimal amount, Integer userId, 
                                     TransactionLog.UserType userType, String description) {
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            conn.setAutoCommit(false);
            
            String selectSql = "SELECT balance FROM accounts WHERE account_no = ? FOR UPDATE";
            
            BigDecimal currentBalance;
            try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                selectStmt.setInt(1, accountNo);
                try (ResultSet rs = selectStmt.executeQuery()) {
                    if (rs.next()) {
                        currentBalance = rs.getBigDecimal("balance");
                    } else {
                        // Log failed transaction - account not found
                        TransactionLog failedLog = new TransactionLog(accountNo, 
                            TransactionLog.TransactionType.WITHDRAWAL, amount, 
                            BigDecimal.ZERO, BigDecimal.ZERO, 
                            TransactionLog.TransactionStatus.FAILED,
                            "Account not found", userId, userType);
                        logTransactionInTransaction(conn, failedLog);
                        conn.commit();
                        return false;
                    }
                }
            }
            
            if (currentBalance.compareTo(amount) < 0) {
                // Log failed transaction - insufficient funds
                TransactionLog failedLog = new TransactionLog(accountNo, 
                    TransactionLog.TransactionType.WITHDRAWAL, amount, 
                    currentBalance, currentBalance, 
                    TransactionLog.TransactionStatus.FAILED,
                    "Insufficient funds", userId, userType);
                logTransactionInTransaction(conn, failedLog);
                conn.commit();
                return false;
            }
            
            // Perform the withdrawal
            String updateSql = "UPDATE accounts SET balance = balance - ? WHERE account_no = ? AND balance >= ?";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setBigDecimal(1, amount);
                updateStmt.setInt(2, accountNo);
                updateStmt.setBigDecimal(3, amount);
                
                int rowsAffected = updateStmt.executeUpdate();
                if (rowsAffected > 0) {
                    BigDecimal balanceAfter = currentBalance.subtract(amount);
                    
                    // Log successful transaction
                    TransactionLog successLog = new TransactionLog(accountNo, 
                        TransactionLog.TransactionType.WITHDRAWAL, amount, 
                        currentBalance, balanceAfter, 
                        TransactionLog.TransactionStatus.SUCCESS,
                        description, userId, userType);
                    
                    if (logTransactionInTransaction(conn, successLog)) {
                        conn.commit();
                        return true;
                    } else {
                        conn.rollback();
                        return false;
                    }
                } else {
                    // Log failed transaction
                    TransactionLog failedLog = new TransactionLog(accountNo, 
                        TransactionLog.TransactionType.WITHDRAWAL, amount, 
                        currentBalance, currentBalance, 
                        TransactionLog.TransactionStatus.FAILED,
                        "Update failed", userId, userType);
                    logTransactionInTransaction(conn, failedLog);
                    conn.rollback();
                    return false;
                }
            }
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    // Log failed transaction
                    TransactionLog failedLog = new TransactionLog(accountNo, 
                        TransactionLog.TransactionType.WITHDRAWAL, amount, 
                        BigDecimal.ZERO, BigDecimal.ZERO, 
                        TransactionLog.TransactionStatus.FAILED,
                        "Database error: " + e.getMessage(), userId, userType);
                    logTransactionInTransaction(conn, failedLog);
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw new RuntimeException("Error withdrawing from account", e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * Gets transaction history for an account with specified limit.
     * 
     * @param conn
     * @param transactionLog
     * @return
     */
    private boolean logTransactionInTransaction(Connection conn, TransactionLog transactionLog) {
        String sql = "INSERT INTO transaction_logs (account_no, transaction_type, amount, " +
                    "balance_before, balance_after, status, description, created_by_user_id, created_by_user_type) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, transactionLog.getAccountNo());
            stmt.setString(2, transactionLog.getTransactionType().name());
            stmt.setBigDecimal(3, transactionLog.getAmount());
            stmt.setBigDecimal(4, transactionLog.getBalanceBefore());
            stmt.setBigDecimal(5, transactionLog.getBalanceAfter());
            stmt.setString(6, transactionLog.getStatus().name());
            stmt.setString(7, transactionLog.getDescription());
            
            if (transactionLog.getCreatedByUserId() != null) {
                stmt.setInt(8, transactionLog.getCreatedByUserId());
            } else {
                stmt.setNull(8, java.sql.Types.INTEGER);
            }
            
            if (transactionLog.getCreatedByUserType() != null) {
                stmt.setString(9, transactionLog.getCreatedByUserType().name());
            } else {
                stmt.setNull(9, java.sql.Types.VARCHAR);
            }
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    

    
    @Override
    public List<TransactionLog> getTransactionHistory(int accountNo) {
        return getTransactionHistory(accountNo, 50); // Default
    }

    /**
     * Gets transaction history for an account with specified limit.
     * Default implementation throws UnsupportedOperationException.
     *
     * @param accountNo the account number
     * @param limit maximum number of records to retrieve
     * @return list of transaction logs
     * @throws UnsupportedOperationException if not supported by implementation
     */
    @Override
    public List<TransactionLog> getTransactionHistory(int accountNo, int limit) {
        List<TransactionLog> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transaction_logs WHERE account_no = ? " +
                    "ORDER BY transaction_date DESC LIMIT ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, accountNo);
            stmt.setInt(2, limit);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    TransactionLog log = mapResultSetToTransactionLog(rs);
                    transactions.add(log);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return transactions;
    }

    

    // Helper method to map ResultSet to TransactionLog
    private TransactionLog mapResultSetToTransactionLog(ResultSet rs) throws SQLException {
        TransactionLog log = new TransactionLog();
        log.setTransactionId(rs.getLong("transaction_id"));
        log.setAccountNo(rs.getInt("account_no"));
        log.setTransactionType(TransactionLog.TransactionType.valueOf(rs.getString("transaction_type")));
        log.setAmount(rs.getBigDecimal("amount"));
        log.setBalanceBefore(rs.getBigDecimal("balance_before"));
        log.setBalanceAfter(rs.getBigDecimal("balance_after"));
        log.setTransactionDate(rs.getTimestamp("transaction_date").toLocalDateTime());
        log.setStatus(TransactionLog.TransactionStatus.valueOf(rs.getString("status")));
        log.setDescription(rs.getString("description"));
        
        int userId = rs.getInt("created_by_user_id");
        if (!rs.wasNull()) {
            log.setCreatedByUserId(userId);
        }
        
        String userType = rs.getString("created_by_user_type");
        if (userType != null) {
            log.setCreatedByUserType(TransactionLog.UserType.valueOf(userType));
        }
        
        return log;
    }


}