# StreamVault – Content Streaming & Analytics Platform

## 📖 Project Description

StreamVault is a full-stack Java web application developed to simulate a modern content streaming platform with integrated analytics capabilities.
The system is designed using a layered architecture that separates concerns across data models, business logic, and request handling.

This project demonstrates practical implementation of backend service design, web application structure, and data-driven user interaction within a servlet-based environment.

---

## 🎯 Objectives

* Implement a structured **multi-layer architecture**
* Develop **scalable backend services**
* Handle **user authentication and session management**
* Provide **content browsing and filtering**
* Integrate **analytics for user/content insights**

---

## 🧠 System Architecture

The application follows a **3-layer architecture**:

* **Presentation Layer**

  * JSP pages (UI rendering)
  * HTML, CSS, JavaScript

* **Controller Layer**

  * Java Servlets
  * Handles HTTP requests/responses

* **Service Layer**

  * Business logic implementation
  * Handles validation, processing, and coordination

* **Data Layer**

  * Database interaction via JDBC
  * Structured models and queries

---

## ⚙️ Technologies Used

| Category   | Technology            |
| ---------- | --------------------- |
| Backend    | Java (Servlets, JSP)  |
| Server     | Apache Tomcat 10.1    |
| Database   | MySQL                 |
| Build Tool | Maven                 |
| Frontend   | HTML, CSS, JavaScript |

---

## 🚀 Core Features

* 🔐 User Registration & Authentication
* 🎬 Content Browsing & Filtering
* 📊 Analytics Dashboard
* 🧩 Modular Service-Based Design
* 🔄 MVC-inspired request handling

---

## 📂 Project Structure

```
src/main/java/com/streamvault/
│
├── models/        # Data entities
├── services/      # Business logic
├── servlets/      # Controllers
└── db/            # Database interaction

webapp/
├── assets/
├── css/
├── js/
├── WEB-INF/
└── *.jsp          # UI pages
```

---

## 🏃 How to Run the Project

### Prerequisites

* Java JDK 17+
* Apache Tomcat 10+
* MySQL Server
* IntelliJ IDEA (recommended)

### Steps

1. Clone the repository:

   ```
   git clone https://github.com/saraammarhusham/StreamVault_Phase4.git
   ```

2. Open the project in IntelliJ IDEA

3. Configure Tomcat Server:

   * Add artifact
   * Set deployment directory

4. Configure Database:

   * Update JDBC connection settings
   * Ensure database schema is created

5. Run the application

6. Access:

   ```
   http://localhost:8080/streamvault
   ```

---

## 🔍 Key Implementation Details

* Uses **JDBC** for direct database communication
* Implements **service-layer abstraction** for maintainability
* Separates UI and backend logic for clarity
* Supports **extensible architecture** for future features

---

## ⚠️ Known Issues / Limitations

* No role-based authorization system
* Limited error handling in edge cases
* Requires manual database configuration

---

## 📈 Future Improvements

* Add REST API layer
* Implement role-based access control
* Improve UI/UX responsiveness
* Add caching for performance optimization

---

## 👥 Contributors

* **Sara Ammar**
* *Heba Daher*

---

## 📜 License

This project is developed for academic purposes and is not intended for commercial use.

---
