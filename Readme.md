## BANKING API

<details>
<summary><b>Table of Contents</b></summary>

- [Overview](#overview)
- [Features](#Key-features)
- [Tech Stack](#Tech-Stack)
- [Project Structure](#project-structure)
- [Setup and Installation](#Environment-Setup-/-Installation)
- [Authentication](#authentication&authorization)
- [API Endpoints](#api-endpoints)
- [Usage Instructions](#API-Usage-Instructions)
- [Contributing](#contributing)

</details> 

## OVERVIEW
BankingAPI is a robust and scalable backend application designed to simulate the core functionalities of a modern banking system. It provides a secure and efficient platform for handling user authentication, account creation, fund transfers, and transaction history management. Built with a focus on security, modularity, and performance, BankingAPI demonstrates the application of RESTful API principles, JWT-based authentication, and data integrity enforcement through validation and structured domain models.

This project is ideal for showcasing enterprise-grade backend development practices, including layered architecture, dependency injection, and token-based session management — all within a clean and extensible Spring Boot framework.

## KEY FEATURES:

- User Authentication & Authorization: Secure registration, login, and JWT-based session handling.

- Account Management: Create and manage multiple user accounts with unique identifiers.

- Fund Transfers: Perform peer-to-peer transfers between user accounts with real-time balance updates.

- Transaction History: Retrieve detailed transaction records for accountability and tracking.

- Modular Architecture: Service, Repository, and Controller layers for maintainability and scalability.

- Robust Validation: Input validation and structured error handling for safe API interaction.

## Tech Stack
- Java 17+
- Spring Boot 3
- Spring Security (JWT-based authentication)
- Hibernate / JPA
- H2 database
- Maven

## Project Structure
src/
 ├── config/              # Security & JWT configurations
 ├── controllers/         # REST controllers
 ├── models/              # Entity models
 ├── repositories/        # JPA repositories
 ├── services/            # Business logic layer
 └── dto/                 # Data Transfer Objects for cleaner request/response handling
 └── hooks/               # Reusable logic or interceptors (e.g., event triggers )
 └── handlers/            # Global exception handlers, response builders, and error middleware. 


## Environment Setup / Installation
1. Clone the repository
2. Configure choice of database, I used H2 and update application.properties.
3. Run `mvn spring-boot:run`
4. API runs on http://localhost:8080


## Authentication & Authorization
BankingAPI implements a JWT (JSON Web Token) based authentication system to ensure secure access control across all protected routes.

Every request made to sensitive endpoints—such as account management, fund transfers, and card operations—must include a valid JWT token in the request header.

The system enforces role-based authorization, distinguishing between regular users and administrators to maintain data privacy and operational integrity.

Authentication Flow
- A new user registers via POST `/api/v1/users`.
- The user logs in using POST `/api/v1/users/login`, providing valid credentials.
- Upon successful authentication, the server issues a JWT token signed with a secret key.
- This token must accompany all subsequent requests in the Authorization header.

Example header:
Authorization: Bearer <your-jwt-token>

Roles & Access Control
Role        Permissions    
**USER**    Can view, update, and manage their own account, perform transfers,  and view personal transactions. 

**ADMIN**   Full system access — including managing users, viewing all transactions, and deleting records. 

# Token Lifecycle
- Tokens are time-bound and will expire after a configured duration (defined in your JWT settings).
- Expired or invalid tokens automatically return a 401 UNAUTHORIZED response.
- Role violations return a 403 FORBIDDEN response.

Response Examples
# Successful Login
{
  "message": "Login successful",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}

# ❌ Unauthorized Access
{
  "error": "Access denied. Invalid or missing token."
}


## Database Schema / Entities Overview
- UserModel: id, username, email, password, role, e.t.c.
- AccountModel: id, balance, user, accountType, dailyLimit, e.t.c.
- CardModel: id, cardNumber, cvv, expiryDate, pin.
- TransactionModel: id, fromAccount, toAccount, amount, timestamp, e.t.c.

## API Endpoints

### api/v1/users/\*\* - controls all users CRUD activities.

### api/v1/accounts/\*\* - controls all users account CRUD activities,.

### api/v1/transactions/\*\* - controls all users transaction CRUD activities,.

### api/v1/cards/\*\* - controls all users account card CRUD activities.

## -- FOR USERS --

`POST - /api/v1/users`

Allows new users to create an account by providing valid credentials. Supports optional role assignment during registration, also auto creates user account.

Example Payload:
{
"username": "victor",
"email": "victor565@gmail.com",
"password": "victorapp123"
}

Responses:
→ 201 CREATED — User successfully created.
→ 400 BAD REQUEST — Invalid input fields.

`POST - /api/v1/users/login`
Authenticates an existing user and returns a JWT token for secure access to protected routes.

Example Payload:
{
"email": "victor565@gmail.com",
"password": "victorapp123"
}

Responses:
→ 200 OK — User logged in successfully.
→ 400 BAD REQUEST — Invalid input fields.

`GET - /api/v1/users/email/{email}`
Fetches a user’s details by their registered email address.
Access is restricted to authenticated users.
Returns user information such as ID, username, email, role, and account status.

Example Response:
→ 200 OK — Returns the user object if found.
→ 404 NOT FOUND — If no user exists with the given email.

`GET - /api/v1/users/{id}`
Retrieves a specific user’s information using their unique ID.
Only accessible by admins or the user themselves.

Example Response:
→ 200 OK — Returns user details.
→ 403 FORBIDDEN — If a non-admin tries to access another user’s record.
→ 404 NOT FOUND — If user ID does not exist.

`PUT - /api/v1/users/{id}`
Updates user details (e.g., username, email, or password).
Admin can update any user; normal users can only update their own accounts.

Example Payload:
{
  "username": "newVictor",
  "email": "newvictor@gmail.com",
  "password": "updatedPass123",
  "isActive":"false"
}

Responses:
→ 200 OK — User information successfully updated.
→ 400 BAD REQUEST — Invalid input fields.
→ 403 FORBIDDEN — Unauthorized update attempt.

`GET – /api/v1/users`
Retrieves all registered users.
Only accessible by admin accounts.

Response:
→ 200 OK — Returns an array of all users.
→ 403 FORBIDDEN — Access denied for non-admin users.

`DELETE - /api/v1/users/{id}`
Deletes a user account permanently from the system.
Action restricted to admins.

Responses:
→ 200 OK — User deleted successfully.
→ 403 FORBIDDEN — Non-admin attempt to delete another account.
→ 404 NOT FOUND — If user ID doesn’t exist.

## -- FOR ACCOUNT --

`POST - /api/v1/create/{userId}`
Creates a new account for a specific user.
Although an account is automatically generated upon user registration, this endpoint allows manual creation for advanced scenarios or two-factor (2FA) account setups.

Example Response:
→ 201 CREATED — Account successfully created for user.
→ 404 NOT FOUND — User not found.
→ 400 BAD REQUEST — Account already exists or invalid data provided.
→ 401 UNAUTHORIZED — user is not authenticated ( missing jw token ).

`POST - /api/v1/accounts/deposit`
Allows a user to deposit funds into their account.
The user must be authenticated, and the account must be active before performing any deposit operations.

Example Payload:
{
  "accountNumber": "1234567890",
  "amount": 5000.00
}

Responses:
→ 200 OK — Deposit successful; account balance updated.
→ 400 BAD REQUEST — Invalid amount or inactive account.
→ 404 NOT FOUND — Account not found.
→ 401 UNAUTHORIZED — user is not authenticated ( missing jw token ).

`POST - /api/v1/accounts/withdraw`
Processes a withdrawal transaction from a user’s account.
This endpoint integrates with the withdrawWithCard service logic to handle card-based withdrawals securely.

Example Payload:

{
    "accountNumber": "1234567890",
    "amount": 2500.00,
    "cardNumber": "4321 8765 2109 6543",
    "pin":1245,
    "cvv": "123",
}

Responses:
→ 200 OK — Withdrawal successful; balance updated.
→ 400 BAD REQUEST — Invalid amount or insufficient balance.
→ 404 NOT FOUND — Account or card not found.
→ 401 UNAUTHORIZED — user is not authenticated ( missing jw token ).

`POST - /api/v1/accounts/transfer`
Enables users to transfer funds between accounts.
Transfers are validated for sufficient balance, and both sender and receiver accounts must be active.

Example Payload:
{
    "fromAccountNumber": "1234567890",
    "toAccountNumber": "0987654321",
    "amount": 1000.00,
    "pin":"1234"
}

Responses:
→ 200 OK — Transfer completed successfully.
→ 400 BAD REQUEST — Invalid details or insufficient funds.
→ 404 NOT FOUND — Account not found.
→ 401 UNAUTHORIZED — user is not authenticated ( missing jw token ).


`PUT/POST - /api/v1/accounts/set-pin`
Allows a user to set or update their account transaction PIN.
The PIN is used for additional verification during withdrawals and transfers.

Example Payload:
{
    "accountNumber": "1234567890",
    "pin": "4321"
}

Responses:
→ 200 OK — PIN successfully set or updated.
→ 400 BAD REQUEST — Invalid PIN format.
→ 404 NOT FOUND — Account not found.
→ 401 UNAUTHORIZED — user is not authenticated ( missing jw token ).

## -- FOR TRANSACTIONS --
`GET - /api/v1/transactions/`
Retrieves all transactions in the system.
This route is available for all users.
If no transactions exist, an empty list is returned with a message indicating “No transactions yet.”

Example Response:
→ 200 OK — Returns all transactions or an empty array if none exist.
→ 401 UNAUTHORIZED — missing jw token.
→ 404 NOT FOUND — If user authentication fails.
→ 401 UNAUTHORIZED — user is not authenticated ( missing jw token ).


`GET - /api/v1/transactions/all`
Retrieves all transactions in the system.
This route is restricted to users with the ADMIN role.
If no transactions exist, an empty list is returned with a message indicating “No transactions yet.”

Example Response:
→ 200 OK — Returns all transactions or an empty array if none exist.
→ 403 FORBIDDEN — Access denied for non-admin users.
→ 404 NOT FOUND — If user authentication fails.
→ 401 UNAUTHORIZED — user is not authenticated ( missing jw token ).

`GET - /api/v1/transactions/{id}`
Fetches a specific transaction by its unique ID.
This endpoint provides complete details of a single transaction, including fromAccount, toAccount, amount, and timestamp.

Example Response:
→ 200 OK — Returns the transaction object.
→ 404 NOT FOUND — Transaction not found with the provided ID.
→ 401 UNAUTHORIZED — user is not authenticated ( missing jw token ).

## -- FOR CARDS --
`GET - /api/v1/cards/card-info/{id}`
Fetches detailed card information for a specific user by their ID.
Only accessible to authenticated users.

Example Response:
{
  "cardNumber": "4000123412341234",
  "expiryDate": "2028-05",
  "cvv": "123",
  "hasPin": true
}

Responses:
→ 200 OK — Returns card information successfully.
→ 404 NOT FOUND — User or card not found.
→ 500 INTERNAL SERVER ERROR — Unexpected error while fetching card info.
→ 401 UNAUTHORIZED — user is not authenticated ( missing jw token ).

`GET - /api/v1/cards/all-cards`
Retrieves all issued cards in the system.
This route is restricted to Admin users only.

Example Response:
[
  {
    "cardId": 1,
    "cardNumber": "4000123412341234",
    "expiryDate": "2028-05",
    "cvv": "123",
    "user": "victor"
  },
  {
    "cardId": 2,
    "cardNumber": "4000123488881234",
    "expiryDate": "2029-10",
    "cvv": "452",
    "user": "emma"
  }
]

Responses:
→ 200 OK — Returns all cards successfully.
→ 401 UNAUTHORIZED — Missing or invalid token.
→ 403 FORBIDDEN — Access denied (non-admin user).

`DELETE - /api/v1/cards/{cardId}`
Deletes a card permanently from the system.
Only Admins can perform this action.

Example Request:
{
  "cardId": 5
}
Example Response:
{
  "message": "Card deleted successfully"
}
Responses:
→ 200 OK — Card deleted successfully.
→ 403 FORBIDDEN — Non-admin attempt to delete card.
→ 404 NOT FOUND — Card not found.
→ 401 UNAUTHORIZED — Missing or invalid token.

## API Usage Instructions
The BankingAPI exposes a RESTful interface for secure and efficient interaction between clients and the server.
`api/v1/accounts`, `api/v1/transactions` and `api/v1/cards` are protected based on user roles and authentication state, while `api/v1/users` endpoint does not require a token — it validates the user’s credentials and returns a simulated login response.

# Testing with Postman
- Open Postman and set the base URL to http://localhost:8080.
- Create a POST request for `/api/v1/users/login`.
- Copy the JWT token from the response.
- Under the “Authorization” tab, select Bearer Token and paste the token.
- Test other secured endpoints like `/api/v1/accounts/transfer` or `/api/v1/users/email/{email}.`


Developed by Victor — Software Engineer
📧 Email: <a href="timivictor565@gmail.com">timivictor565@gmail.com</a>
🔗 GitHub: github.com/alphaboy20023
