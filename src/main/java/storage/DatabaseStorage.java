package storage;

import entity.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseStorage extends AbstractDataStorage {
    private final DatabaseConnection dbConnection;

    public DatabaseStorage() throws SQLException {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    // Customer operations
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
    @Override 
    public int saveAccount(SavingsAccount account) {
        String sql = "INSERT INTO accounts (customer_id, balance, account_type, branch_id) VALUES (?, ?, ?, ?)";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, account.getCustomerId());
            stmt.setDouble(2, account.getBalance());
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
                    .balance(rs.getDouble("balance"))
                    .build();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving account", e);
        }
        return null;
    }

    @Override
    public void updateAccount(SavingsAccount account) {
        String sql = "UPDATE accounts SET balance = ? WHERE account_no = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDouble(1, account.getBalance());
            stmt.setInt(2, account.getAccountNo());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating account", e);
        }
    }

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
                    .balance(rs.getDouble("balance"))
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
        String sql = "SELECT * FROM accounts WHERE customer_id = ?";
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
                    .balance(rs.getDouble("balance"))
                    .build());
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving accounts by customer", e);
        }
        return accounts;
    }

    // Branch operations
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

    // Transaction operations
    @Override
    public boolean withdrawFromAccount(int accountNo, double amount) {
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            conn.setAutoCommit(false);
            
            String selectSql = "SELECT balance FROM accounts WHERE account_no = ? FOR UPDATE";
            String updateSql = "UPDATE accounts SET balance = balance - ? WHERE account_no = ? AND balance >= ?";
            
            double currentBalance;
            try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                selectStmt.setInt(1, accountNo);
                try (ResultSet rs = selectStmt.executeQuery()) {
                    if (rs.next()) {
                        currentBalance = rs.getDouble("balance");
                    } else {
                        conn.rollback();
                        return false;
                    }
                }
            }
            
            if (currentBalance < amount) {
                conn.rollback();
                return false;
            }
            
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setDouble(1, amount);
                updateStmt.setInt(2, accountNo);
                updateStmt.setDouble(3, amount);
                
                int rowsAffected = updateStmt.executeUpdate();
                if (rowsAffected > 0) {
                    conn.commit();
                    return true;
                } else {
                    conn.rollback();
                    return false;
                }
            }
        } catch (SQLException e) {
            if (conn != null) {
                try {
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

    @Override
    public boolean depositToAccount(int accountNo, double amount) {
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            conn.setAutoCommit(false);
            
            // First check if account exists
            String checkSql = "SELECT account_no FROM accounts WHERE account_no = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, accountNo);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return false; // Account doesn't exist
                    }
                }
            }
            
            String sql = "UPDATE accounts SET balance = balance + ? WHERE account_no = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setDouble(1, amount);
                stmt.setInt(2, accountNo);
                
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    conn.commit();
                    return true;
                } else {
                    conn.rollback();
                    return false;
                }
            }
        } catch (SQLException e) {
            if (conn != null) {
                try {
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
}