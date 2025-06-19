package entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionLog {
    
    public enum TransactionType {
        DEPOSIT, WITHDRAWAL
    }
    
    public enum TransactionStatus {
        SUCCESS, FAILED
    }
    
    public enum UserType {
        CUSTOMER, EMPLOYEE, MANAGER
    }
    
    private Long transactionId;
    private int accountNo;
    private TransactionType transactionType;
    private BigDecimal amount;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private LocalDateTime transactionDate;
    private TransactionStatus status;
    private String description;
    private Integer createdByUserId;
    private UserType createdByUserType;
    
    // Constructors
    public TransactionLog() {}
    
    public TransactionLog(int accountNo, TransactionType transactionType, BigDecimal amount, 
                         BigDecimal balanceBefore, BigDecimal balanceAfter, TransactionStatus status) {
        this.accountNo = accountNo;
        this.transactionType = transactionType;
        this.amount = amount;
        this.balanceBefore = balanceBefore;
        this.balanceAfter = balanceAfter;
        this.status = status;
        this.transactionDate = LocalDateTime.now();
    }
    
    public TransactionLog(int accountNo, TransactionType transactionType, BigDecimal amount, 
                         BigDecimal balanceBefore, BigDecimal balanceAfter, TransactionStatus status,
                         String description, Integer createdByUserId, UserType createdByUserType) {
        this(accountNo, transactionType, amount, balanceBefore, balanceAfter, status);
        this.description = description;
        this.createdByUserId = createdByUserId;
        this.createdByUserType = createdByUserType;
    }
    
    // Getters and Setters
    public Long getTransactionId() {
        return transactionId;
    }
    
    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }
    
    public int getAccountNo() {
        return accountNo;
    }
    
    public void setAccountNo(int accountNo) {
        this.accountNo = accountNo;
    }
    
    public TransactionType getTransactionType() {
        return transactionType;
    }
    
    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public BigDecimal getBalanceBefore() {
        return balanceBefore;
    }
    
    public void setBalanceBefore(BigDecimal balanceBefore) {
        this.balanceBefore = balanceBefore;
    }
    
    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }
    
    public void setBalanceAfter(BigDecimal balanceAfter) {
        this.balanceAfter = balanceAfter;
    }
    
    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }
    
    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }
    
    public TransactionStatus getStatus() {
        return status;
    }
    
    public void setStatus(TransactionStatus status) {
        this.status = status;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Integer getCreatedByUserId() {
        return createdByUserId;
    }
    
    public void setCreatedByUserId(Integer createdByUserId) {
        this.createdByUserId = createdByUserId;
    }
    
    public UserType getCreatedByUserType() {
        return createdByUserType;
    }
    
    public void setCreatedByUserType(UserType createdByUserType) {
        this.createdByUserType = createdByUserType;
    }
    
    @Override
    public String toString() {
        return String.format("TransactionLog{id=%d, accountNo=%d, type=%s, amount=%.2f, " +
                           "balanceBefore=%.2f, balanceAfter=%.2f, date=%s, status=%s}",
                           transactionId, accountNo, transactionType, amount, 
                           balanceBefore, balanceAfter, transactionDate, status);
    }
}