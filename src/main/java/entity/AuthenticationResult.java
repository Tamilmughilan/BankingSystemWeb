package entity;

public class AuthenticationResult {
    private final boolean success;
    private final String role;
    private final int userId;
    private final String userName;

    public AuthenticationResult(boolean success, String role, int userId, String userName) {
        this.success = success;
        this.role = role;
        this.userId = userId;
        this.userName = userName;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getRole() {
        return role;
    }

    public int getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }
}
