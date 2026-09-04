# HireTrack Backend 🎯

Enterprise recruitment platform built with **Spring Boot 3**, **jOOQ**, and **JWT Authentication**. Streamlines the hiring lifecycle with role-based access control, candidate pipeline management, interview scheduling with real-time conflict prevention, and structured feedback collection.

---

## 🛠️ Tech Stack

- **Java 21**
- **Spring Boot 3.4.2**
- **jOOQ 3.19** (Type-safe SQL & Complex Joins)
- **PostgreSQL 18**
- **Flyway** (Version-controlled DB Migrations)
- **Spring Security + JWT** (Stateless Authentication)
- **Lombok**
- **Jakarta Validation**
- **Maven Wrapper (mvnw)**

---

## 🚀 Quick Start

### Prerequisites

- Java 21 or higher
- PostgreSQL running on `localhost:5432`
- Terminal or PowerShell

### Setup & Run

#### 1. Clone the Repository

```bash
git clone https://github.com/YOUR_USERNAME/HireTrackBackend.git
cd HireTrackBackend
```

#### 2. Configure Database

Open `src/main/resources/application.properties` and update your PostgreSQL password:

```properties
spring.datasource.password=your_postgres_password
spring.flyway.password=your_postgres_password
```

#### 3. Run the Application

**macOS/Linux**

```bash
./mvnw spring-boot:run
```

**Windows (PowerShell/CMD)**

```bash
.\mvnw spring-boot:run
```

Maven will automatically:

- Download dependencies
- Run Flyway migrations
- Create the database schema
- Seed 3 demo users on first startup

#### 4. Verify Startup

Application URL:

```text
http://localhost:8080
```

Check the console for:

```text
🔥 Seeded 3 demo users
Started HireTrackBackendApplication
```

---

## 🔐 Demo Credentials

**Password for all accounts:** `password123`

| Email | Role | Permissions |
|---------|---------|-------------|
| admin@test.com | ADMIN | Full system access & user management |
| recruiter@test.com | RECRUITER | Manage jobs, candidates, interviews, view feedback |
| interviewer@test.com | INTERVIEWER | View assigned interviews, mark complete, submit feedback |

---

## 📡 API Reference

**Base URL**

```text
http://localhost:8080/api
```

**Authentication**

Add the following header to all protected endpoints:

```http
Authorization: Bearer <your_jwt_token>
```

**Get JWT Token**

```http
POST /auth/login
```

Request Body:

```json
{
  "email": "recruiter@test.com",
  "password": "password123"
}
```

---

## 💼 Jobs API (`/jobs`)

| Method | Endpoint | Role | Description |
|----------|----------|----------|----------|
| POST | /jobs | Recruiter/Admin | Create a new job (default status: OPEN) |
| GET | /jobs?status=&search=&page=&size= | All Authenticated Users | Paginated job listing with filters |
| PATCH | /jobs/{id}/status?status= | Recruiter/Admin | Update job status |

Supported Status Flow:

```text
OPEN → ON_HOLD → CLOSED
```

---

## 👤 Candidates API (`/candidates`)

| Method | Endpoint | Role | Description |
|----------|----------|----------|----------|
| POST | /candidates | Recruiter/Admin | Add candidate to a job |
| GET | /candidates?jobId=&status=&page= | All Authenticated Users | Filtered candidate pipeline |
| POST | /candidates/{id}/resume | Recruiter/Admin | Upload resume |
| PATCH | /candidates/{id}/status?status= | Recruiter/Admin | Update candidate status |

Candidate Pipeline:

```text
APPLIED → SELECTED / REJECTED
```

---

## 📅 Interviews API (`/interviews`)

| Method | Endpoint | Role | Description |
|----------|----------|----------|----------|
| POST | /interviews | Recruiter/Admin | Schedule interview with overlap validation |
| GET | /interviews/my | Interviewer | View assigned interviews |
| PATCH | /interviews/{id}/complete | Interviewer | Mark interview completed |
| PATCH | /interviews/{id}/cancel | Recruiter/Admin | Cancel interview and revert candidate status |

---

## ⭐ Feedback API

### Endpoint

```text
/interviews/{id}/feedback
```

| Method | Endpoint | Role | Description |
|----------|----------|----------|----------|
| POST | /interviews/{id}/feedback | Interviewer | Submit interview feedback (once per interview) |
| GET | /candidates/{id}/feedbacks | Recruiter/Admin | View all candidate feedback |

---

## 📊 Dashboard API (`/dashboard`)

| Method | Endpoint | Role | Description |
|----------|----------|----------|----------|
| GET | /dashboard | All Authenticated Users | View role-based hiring metrics |

---

## 🏗️ Architecture & Key Features

### Layered Architecture

```text
Controller → Service → Repository (jOOQ)
```

Ensures:

- Separation of concerns
- Maintainability
- Testability

### Key Features

✅ Role-Based Access Control using Spring Security

✅ JWT Stateless Authentication

✅ Real-time Interview Conflict Prevention

✅ Candidate Pipeline Tracking

✅ Structured Interview Feedback

✅ Dashboard Metrics

✅ Flyway Database Versioning

✅ Type-Safe SQL with jOOQ

✅ Centralized Exception Handling

✅ Secure Resume Uploads with UUID Storage

---

## 🔄 Business Rules

### Job State Machine

```text
OPEN → ON_HOLD → CLOSED
```

- Closed jobs cannot be reopened.
- Closed jobs cannot be modified.

### Interview Scheduling Rules

- Interviewers cannot be double-booked.
- Calendar overlap is validated in real time.
- Invalid schedules are rejected before creation.

### Feedback Rules

- One feedback submission per interviewer per interview.
- Ratings and recommendations are stored for auditability.

---

## 🧪 Testing & Frontend Integration

### Authentication Flow

```text
Login
 ↓
Receive JWT Token
 ↓
Attach Bearer Token
 ↓
Access Protected APIs
```

### Validation Errors

Example response:

```json
{
  "details": {
    "fieldName": "Error message"
  }
}
```

### Resume Upload

- Content Type: `multipart/form-data`
- Field Name: `file`
- Maximum Size: `5 MB`

### Logs

Run:

```bash
.\mvnw spring-boot:run
```

View:

- jOOQ SQL execution logs
- JWT validation logs
- Spring Security filter logs

---

## 🎯 3-Minute Client Demo Flow

### Recruiter

1. Login as Recruiter
2. Create a **Java Developer** job
3. Add a candidate
4. Upload candidate resume
5. Shortlist candidate
6. Schedule interview

### System

- Validates interviewer availability
- Prevents scheduling conflicts

### Interviewer

1. Login
2. View assigned interview
3. Mark interview as completed
4. Submit feedback

Example:

```text
Recommendation: SELECT
Technical Rating: 4/5
```

### Recruiter

1. Review feedback
2. Mark candidate as SELECTED
3. Dashboard metrics update automatically

---

## 💡 Elevator Pitch

> "HireTrack provides a complete recruitment workflow with secure role-based access, interview conflict prevention, candidate pipeline management, structured feedback collection, and real-time hiring insights. Built using Spring Boot, PostgreSQL, jOOQ, Flyway, and JWT authentication for enterprise-ready deployment."

---

## 📄 License

MIT License © Vishnu Divakar
