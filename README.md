# 🚀 Smart Task Management System

A secure and scalable full-stack Task Management System built using **Spring Boot**, **MongoDB**, and a **custom JWT Authentication Starter**.

This application enables users to securely register, log in, and manage their personal tasks through a protected dashboard while following clean architecture and REST API best practices.

---

## 📋 Project Overview

The Smart Task Management System is designed to demonstrate:

* Secure Authentication & Authorization
* RESTful API Design
* MongoDB Integration
* Role-Based Access Control
* Clean Backend Architecture
* Frontend Integration
* Scalable and Maintainable Code Structure

Users can create, manage, update, and track their tasks while accessing the system through JWT-protected APIs.

---

# ✨ Features

## 🔐 Authentication & Security

* User Registration
* User Login
* JWT Authentication
* Secure Protected APIs
* Password Encryption
* Role-Based Access Control (USER / ADMIN)
* Custom JWT Starter Integration
* Request Validation
* Global Exception Handling

---

## 👤 User Management

* View User Profile
* Update User Profile
* Role Management Support

---

## ✅ Task Management

* Create Tasks
* View All Tasks
* Get Task By ID
* Update Tasks
* Delete Tasks
* Task Status Management
* Task Priority Management
* User-Specific Tasks
* Search Tasks
* Filter Tasks

---

## 📖 Documentation

* Swagger OpenAPI Integration
* Interactive API Testing
* Clean API Contracts

---

# 🛠 Tech Stack

## Backend

* Java 21
* Spring Boot
* Spring Security
* Spring Data MongoDB
* Maven
* Swagger/OpenAPI
* JUnit
* Mockito

## Frontend

* HTML
* CSS
* JavaScript

## Database

* MongoDB

## Authentication

Custom JWT Authentication Starter

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.HimanshuSingh924.jwt-auth-starter</groupId>
    <artifactId>jwt-auth-autoconfigure</artifactId>
    <version>1.0.1</version>
</dependency>
```

---

# 📂 Project Structure

```text
smart-task-management/
│
├── backend/
│   │
│   ├── auth/
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── service/
│   │   └── config/
│   │
│   ├── users/
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── entity/
│   │   ├── repository/
│   │   └── service/
│   │
│   ├── tasks/
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── entity/
│   │   ├── repository/
│   │   └── service/
│   │
│   ├── common/
│   │   ├── exception/
│   │   ├── response/
│   │   ├── mapper/
│   │   └── util/
│   │
│   ├── config/
│   │
│   └── src/main/resources/
│
└── frontend/
    ├── index.html
    ├── style.css
    └── app.js
```

---

# 🏗 Architecture

The project follows a clean layered architecture.

```text
Client
   │
   ▼
Controller Layer
   │
   ▼
Service Layer
   │
   ▼
Repository Layer
   │
   ▼
MongoDB
```

### Controller Layer

Handles incoming HTTP requests and API responses.

### Service Layer

Contains business logic and application rules.

### Repository Layer

Manages database interactions using MongoDB repositories.

### Security Layer

Responsible for authentication and authorization using JWT.

### Common Layer

Contains:

* Global Exception Handling
* Utility Classes
* Response Wrappers
* Shared Components

---

# 🗄 Database Design

## User Collection

```json
{
  "id": "ObjectId",
  "name": "Himanshu Singh",
  "email": "himanshu@example.com",
  "password": "encrypted_password",
  "role": "USER"
}
```

---

## Task Collection

```json
{
  "id": "ObjectId",
  "title": "Complete Assignment",
  "description": "Submit internship assignment",
  "status": "PENDING",
  "priority": "HIGH",
  "ownerId": "userId",
  "createdAt": "timestamp",
  "updatedAt": "timestamp"
}
```

---

# 🔐 Authentication Flow

```text
User Registration
        │
        ▼
Password Encryption
        │
        ▼
Store User in MongoDB
        │
        ▼
User Login
        │
        ▼
JWT Token Generated
        │
        ▼
Access Protected APIs
```

Authentication is implemented using a reusable custom JWT starter dependency.

---

# 🌐 API Endpoints

The application exposes RESTful APIs for authentication, user management, and task management.

---

## 🔐 Authentication APIs

### Register User

```http
POST /api/v1/auth/register
```

Creates a new user account.

**Access:** Public

---

### Login User

```http
POST /api/v1/auth/login
```

Authenticates a user and returns a JWT access token.

**Access:** Public

---

## 👤 User APIs

### Get User Profile

```http
GET /api/v1/users/profile
```

Returns the currently authenticated user's profile.

**Access:** Protected 🔒

---

### Update User Profile

```http
PUT /api/v1/users/profile
```

Updates user profile information.

**Access:** Protected 🔒

---

## ✅ Task APIs

### Create Task

```http
POST /api/v1/tasks
```

Creates a new task for the authenticated user.

**Access:** Protected 🔒

---

### Get All Tasks

```http
GET /api/v1/tasks
```

Returns all tasks belonging to the logged-in user.

**Access:** Protected 🔒

Supported Query Parameters:

| Parameter | Description        |
| --------- | ------------------ |
| status    | Filter by status   |
| priority  | Filter by priority |
| keyword   | Search tasks       |

Example:

```http
GET /api/v1/tasks?status=PENDING
```

---

### Get Task By ID

```http
GET /api/v1/tasks/{id}
```

Returns details of a specific task.

**Access:** Protected 🔒

---

### Update Task

```http
PUT /api/v1/tasks/{id}
```

Updates task details.

**Access:** Protected 🔒

---

### Delete Task

```http
DELETE /api/v1/tasks/{id}
```

Deletes a task permanently.

**Access:** Protected 🔒

---

# 📖 Swagger Documentation

Once the backend application is running, open:

```text
http://localhost:8080/swagger-ui/index.html
```

Swagger UI allows you to:

* Test APIs directly from browser
* View request and response schemas
* Validate API contracts
* Explore secured endpoints

---

# 🔒 Authorization Header

Protected APIs require a JWT token.

```http
Authorization: Bearer <your-jwt-token>
```

Example:

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

---

# ⚙️ Environment Configuration

Configure MongoDB inside:

```yaml
application.yml
```

Example:

```yaml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/taskdb
```

---

# ▶️ Running the Application

## Clone Repository

```bash
git clone <repository-url>
```

---

## Start Backend

```bash
cd backend

mvn clean install

mvn spring-boot:run
```

Backend runs on:

```text
http://localhost:8080
```

---

## Start Frontend

```bash
cd frontend
```

Open:

```text
index.html
```

or

```bash
npx serve .
```

---

# 🧪 Testing

Run backend tests:

```bash
mvn test
```

Included Tests:

* Authentication Tests
* User Service Tests
* Task Service Tests

---

# 📈 Scalability Considerations

The project follows a modular architecture and can be scaled further using:

* Redis Caching
* Docker Containerization
* API Gateway
* Microservices Architecture
* Load Balancing
* Message Queues (Kafka/RabbitMQ)


---

# 🎯 Assignment Coverage

✅ User Registration

✅ User Login

✅ JWT Authentication

✅ Role-Based Access Control

✅ MongoDB Integration

✅ CRUD Operations

✅ Validation

✅ Exception Handling

✅ Protected APIs

✅ Swagger Documentation

✅ Frontend Integration

✅ Scalable Architecture

---

# 👨‍💻 Author

**Himanshu Singh**

Java Backend Developer

GitHub: https://github.com/HimanshuSingh924

---

⭐ If you found this project useful, feel free to star the repository.
