
# IBMS – Integrated Banking Management System

## Project Overview

IBMS is a microservices-based banking management system developed using
Spring Boot. The system is divided into independent services for
authentication, transactions, notifications, and loan management.

## Microservices

### 1. Auth Service

**Port:** 8081  
**Database:** auth_db

Responsibilities:
- User registration
- User information retrieval
- Authentication-related operations

### 2. Transaction Service

**Port:** 8082  
**Database:** transaction_db

Responsibilities:
- Account creation
- Deposits
- Withdrawals
- Transfers
- Transaction tracking

### 3. Notification Service

**Port:** 8083  
**Database:** notification_db

Responsibilities:
- User notifications
- Transaction notifications
- Loan notifications

### 4. Loan Service

**Port:** 8084  
**Database:** loan_db

Responsibilities:
- Loan applications
- Loan status management

## Architecture

```text
                    AUTH SERVICE
                       :8081
                         |
                      OpenFeign
                         |
                         v
                 TRANSACTION SERVICE
                       :8082


                    LOAN SERVICE
                       :8084
                         |
                      OpenFeign
                         |
                         v
                NOTIFICATION SERVICE
                       :8083
````

Each microservice is independently deployable and uses its own database.

## Technology Stack

* Java 21
* Spring Boot
* Spring Web
* Spring Data JPA
* MySQL
* OpenFeign
* Maven
* Swagger / OpenAPI

## Database Distribution

| Microservice         | Database        |
| -------------------- | --------------- |
| Auth Service         | auth_db         |
| Transaction Service  | transaction_db  |
| Notification Service | notification_db |
| Loan Service         | loan_db         |

## API Endpoints

### Auth Service – 8081

```text
POST /auth/register
GET  /auth/users/{id}
```

### Transaction Service – 8082

```text
POST /accounts
GET  /accounts/{id}
POST /transactions/deposit
POST /transactions/withdraw
POST /transactions/transfer
GET  /transactions/{id}
```

### Notification Service – 8083

```text
POST /notifications
GET  /notifications/{userId}
```

### Loan Service – 8084

```text
POST /loans/apply
GET  /loans/{id}
```

## Microservice Communication

OpenFeign is used for service-to-service communication.

* Transaction Service → Auth Service
* Loan Service → Notification Service

## Swagger Documentation

Swagger UI is available at:

```text
Auth:
http://localhost:8081/swagger-ui/index.html

Transaction:
http://localhost:8082/swagger-ui/index.html

Notification:
http://localhost:8083/swagger-ui/index.html

Loan:
http://localhost:8084/swagger-ui/index.html
```

## How to Run

1. Start MySQL.
2. Create the required databases:

   * auth_db
   * transaction_db
   * notification_db
   * loan_db
3. Open each microservice in IntelliJ IDEA.
4. Run the services on ports 8081–8084.
5. Open the Swagger URLs to test the APIs.

## Project Status

* Auth Service: Completed
* Transaction Service: Completed
* Notification Service: Completed
* Loan Service: Completed
* MySQL Database Integration: Completed
* OpenFeign Communication: Completed
* Swagger API Documentation: Completed

