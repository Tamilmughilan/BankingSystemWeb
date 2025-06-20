# Banking System Web Application

A secure, enterprise grade banking application built with Java servlets and JSP.

##  Architecture

**3-Tier Architecture with 7 Packages:**
- **entity**: Core business objects (Customer, Account, Transaction)
- **servlet**: Web layer handling HTTP requests from JSP pages
- **filter**: 8-filter security chain for protection
- **service**: Business logic layer delegating requests to storage
- **storage**: Data access with multiple implementations (MySQL, MongoDB, In-Memory)
- **job**: Background processing for automated operations
- **util**: Utility classes for password generation and OTP

##  Technologies

- **Backend**: Java Servlets, JSP
- **Database**: MySQL, MongoDB
- **Security**: Custom filter chain, CSRF protection, CSP Headers etc
- **Frontend**: HTML, CSS, JavaScript, Ajax
- **Documentation**: Javadoc

## Security Features

- 8-Filter Security Chain
- CSRF Token Protection
- Input Sanitization
- Rate Limiting
- Activity Logging
- Authentication & Authorization

## Storage Options

- **DatabaseStorage**: MySQL implementation
- **MongoDBStorage**: NoSQL implementation  
- **CollectionStorage**: In-memory for testing

## Key Features

- Account Management (Deposit, Withdrawal, Balance)
- Customer Registration and Login
- Transaction History and Logging
- Background Interest Calculation using JOBS
- Multi storage Strategy Pattern
- Comprehensive Security Implementation
- Passwords - Hashing + Salt

## 📖 Documentation

Complete Javadoc documentation: [View Documentation](https://Tamilmughilan.github.io/BankingSystemWeb/)

## Quick Start

1. Clone the repository
2. Configure database connection in `DatabaseStorage.java`
3. Deploy to servlet container (Tomcat)
4. Access at `http://localhost:8080/BankingSystemWeb`

## Author

**Tamil Mughilan E**  
B.Tech Information Technology, SSN College of Engineering  
tamilmughilan2210630@ssn.edu.in
