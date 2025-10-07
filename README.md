# Welfare Connect 🤝

Welcome to **Welfare Connect**, an emotionally intelligent Java Swing desktop application designed to create a system of trust, transparency, and care between citizens and welfare administrators. This project aims to be a smart, secure, and empathetic platform where citizens can discover, apply for, and track welfare schemes efficiently.

---

## ✨ Core Features

The application is built with distinct roles, each having a unique and powerful interface.

### For Citizens 🧑‍🤝‍🧑
* **Dynamic & Secure Login:** A beautiful, animated login screen that adapts based on user sub-roles (Student, Farmer, etc.) with role-specific authentication.
* **Intelligent Scheme Discovery:** A personalized dashboard that recommends eligible schemes and provides powerful search and filter capabilities.
* **Seamless Application Process:** Easy application forms with integrated document uploading (PDF/JPG).
* **Real-time Tracking:** A color-coded table to track the status of all submitted applications.
* **Personal Profile & Settings:** A dedicated space to manage personal information, change passwords, and switch application themes (Light/Dark Mode).

### For Officers 🏢
* **Efficient Application Queue:** A clear, sortable list of all pending applications.
* **Comprehensive Review Panel:** A detailed view of each application, including applicant data and attached documents.
* **Clear Action Buttons:** Functionality to Approve, Reject (with a reason), or Request More Information from citizens.

### For Admins 🛠️
* **Full Scheme Management:** Complete CRUD (Create, Read, Update, Delete) control over all welfare schemes.
* **User Administration:** Manage officer accounts within the system.
* **Powerful Analytics:** An interactive dashboard with charts from JFreeChart, date-range filters, and the ability to **export data to CSV**.

---

## 💾 Tech Stack

* **UI Framework:** Java Swing with the modern [FlatLaf](https://www.formdev.com/flatlaf/) look-and-feel.
* **Database:** MySQL (designed for) & SQLite (supported via JDBC).
* **Connectivity:** JDBC with `PreparedStatements` for security.
* **Charting:** JFreeChart for analytics.
* **Build Tool:** Apache Maven.
* **Architecture:** Strict Model-View-Controller (MVC).

---

## 🚀 Getting Started

Follow these instructions to get a copy of the project up and running on your local machine for development and testing.

### Prerequisites

* **Java Development Kit (JDK)** version 11 or higher.
* **Apache Maven** to build the project.
* **SQLite** (or a configured MySQL database).

### Installation & Running

1.  **Clone the repository:**
    ```bash
    git clone <your-repository-url>
    cd <repository-folder>
    ```

2.  **Build the project with Maven:**
    This command compiles the code and packages it into an executable JAR file with all dependencies.
    ```bash
    mvn clean package
    ```

3.  **Run the application:**
    Use the following command from the root folder of the project. This command sets the database path using a system property and then runs the application.

    ```bash
    java -Dwc.db.url=jdbc:sqlite:/path/to/your/welfare_connect.db -cp target/welfare-connect-0.1.0-SNAPSHOT-jar-with-dependencies.jar com.welfareconnect.App
    ```
    **Important:** Make sure to replace `/path/to/your/welfare_connect.db` with the actual absolute path to your database file. For example, on a Linux/macOS system, it might be `/home/username/projects/welfare-connect/welfare_connect.db`.
