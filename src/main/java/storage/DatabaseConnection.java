package storage;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton class for managing database connections to MySQL
 * Ensures only one instance exists and provides connection pooling
 * 
 * @author TAMIL MUGHILAN
 */
public class DatabaseConnection {
    private static DatabaseConnection instance;
    
 
    private final String url = "jdbc:mysql://localhost:3306/BankingSystem?" +
        "useSSL=true&" +
        "serverTimezone=UTC&" +
        "autoReconnect=true&" +
        "useUnicode=true&" +
        "characterEncoding=UTF-8&" +
        "allowPublicKeyRetrieval=false&" +
        "connectTimeout=5000&" +
        "socketTimeout=10000";
    private final String USER = "root";
    private final String PASSWORD = "root";
    
    /**
     * Private constructor to prevent direct instantiation.
     * Loads MySQL JDBC driver when instance is created.
     *
     * @throws SQLException if JDBC driver is not found
     */
    private DatabaseConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found", e);
        }
    }
    
    /**
     * Gets the singleton instance of DatabaseConnection.
     * Uses double-checked locking for thread safety.
     *
     * @return the singleton DatabaseConnection instance
     * @throws SQLException if instance creation fails
     */
    public static DatabaseConnection getInstance() throws SQLException {
        if (instance == null) {
            synchronized (DatabaseConnection.class) {
                if (instance == null) {
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }
    
    /**
     * Creates and returns a new database connection.
     *
     * @return a new Connection object to the database
     * @throws SQLException if connection fails
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, USER, PASSWORD);
    }
}