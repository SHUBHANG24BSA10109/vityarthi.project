# vityarthi.project
# 📚 Library Book Management System

![Java](https://img.shields.io/badge/Language-Java-orange) ![Framework](https://img.shields.io/badge/Framework-Swing-red) ![IDE](https://img.shields.io/badge/IDE-IntelliJ_IDEA-blue)

## 📖 Overview
The **Library Book Management System** is a desktop-based application developed using **Java Swing**. 

The primary goal of this system is to digitize library operations, replacing manual registers with an efficient digital database. It empowers librarians to manage book inventories, register members, and track the issue/return workflow seamlessly. This automation significantly reduces manual errors and provides real-time tracking of book availability.

---

## ✨ Features

### 📘 Book Management
* **Add, Update, & Delete:** comprehensive CRUD operations for library books.
* **Search:** Quickly find books by title.
* **Inventory Tracking:** Automatically updates stock upon issue or return.

### 👥 Member Management
* **Member Registration:** Add new student/faculty members.
* **Manage Records:** View, update, or remove member details.

### 🔄 Transaction System
* **Issue Books:** Assign books to members with a recorded issue date.
* **Return Books:** Process returns and update inventory instantly.
* **Validation:** Prevents deletion of books or members currently involved in active transactions.
* **History:** View complete transaction history (Issue/Return logs).

---

## 🛠️ Technologies & Tools Used

| Component | Specification |
| :--- | :--- |
| **Programming Language** | Java |
| **GUI Framework** | Java Swing (javax.swing) |
| **JDK Version** | JDK 17 or above |
| **IDE** | IntelliJ IDEA (Compatible with Eclipse/VS Code) |
| **Database/Storage** | In-Memory / File I/O / Database (As per implementation) |

---

## 🚀 Steps to Install and Run

1.  **Clone the Repository**
    Download the project ZIP or clone it using Git:
    ```bash
    git clone [https://github.com/YourUsername/LibraryManagementSystem.git](https://github.com/YourUsername/LibraryManagementSystem.git)
    ```

2.  **Open in IDE**
    * Launch **IntelliJ IDEA**.
    * Select **File > Open** and navigate to the project folder.

3.  **Configure JDK**
    * Ensure **JDK 17** or higher is selected in `File > Project Structure > Project`.

4.  **Run the Application**
    * Navigate to the `src` folder.
    * Locate and right-click on **`LibraryManagementSwingApp.java`**.
    * Select **Run 'LibraryManagementSwingApp.main()'**.

The application GUI window will launch immediately.

---

## 🧪 Instructions for Testing

Follow these steps to verify the system's functionality:

1.  **Populate Data:**
    * Go to the "Books" section and add 2-3 books with different titles and quantities.
    * Go to the "Members" section and register a new member.

2.  **Test Issue Workflow:**
    * Select a book and the new member.
    * Click **Issue**.
    * *Verification:* Check the Book List; the "Available Quantity" should decrease by 1.

3.  **Test Return Workflow:**
    * Select the same book and member.
    * Click **Return**.
    * *Verification:* The "Available Quantity" should increase back to the original number.

4.  **Test Validation:**
    * Issue a book to a member.
    * Try to **Delete** that specific book from the system.
    * *Result:* The system should show an error message preventing the deletion.

5.  **Test Search:**
    * Type a partial keyword (e.g., "Java") in the search bar to filter the book list.

---

## 📸 Screenshots
*(Optional: Add screenshots of your Main Menu, Issue Book form, or Book List here)*
