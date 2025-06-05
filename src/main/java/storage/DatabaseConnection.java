package storage;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static DatabaseConnection instance;
    
    
    private final String url = "jdbc:mysql://localhost:3306/BankingSystem?useSSL=false&serverTimezone=UTC&autoReconnect=true&useUnicode=true&characterEncoding=UTF-8&allowPublicKeyRetrieval=true";
    private final String USER = "root";
    private final String PASSWORD = "root";
    
    private DatabaseConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found", e);
        }
    }
    
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
    
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, USER, PASSWORD);
    }
}