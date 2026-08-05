# 📋 Digital Notice Board
### A Java Swing + JDBC + MySQL Desktop Application

---

## 📁 Project Structure

```
DigitalNoticeBoard/
├── src/
│   └── com/noticeboard/
│       ├── Main.java                    ← Entry point
│       ├── db/
│       │   ├── DBConnection.java        ← MySQL connection manager
│       │   ├── UserDAO.java             ← User DB operations
│       │   └── NoticeDAO.java           ← Notice DB operations
│       ├── model/
│       │   ├── User.java                ← User data model
│       │   └── Notice.java              ← Notice data model
│       ├── util/
│       │   └── UITheme.java             ← Colors, fonts, UI helpers
│       └── ui/
│           ├── LoginFrame.java          ← Login & Registration screen
│           ├── AdminDashboard.java      ← Admin panel
│           ├── StudentDashboard.java    ← Student panel
│           ├── NoticeDialog.java        ← Add/Edit notice dialog
│           └── NoticeViewDialog.java    ← View notice + download
├── lib/
│   └── mysql-connector-java.jar        ← (Download separately)
├── database_setup.sql                  ← Run this first in MySQL
├── run.bat                             ← Windows build & run
├── run.sh                              ← Linux/Mac build & run
└── README.md
```

---

## ⚙️ Prerequisites

- **Java JDK 8 or higher** — https://adoptium.net
- **MySQL Server 5.7 or 8.x** — https://dev.mysql.com/downloads/
- **MySQL Connector/J (JDBC Driver)** — https://dev.mysql.com/downloads/connector/j/

---

## 🚀 Setup Instructions

### Step 1 — Download MySQL JDBC Driver
1. Go to: https://dev.mysql.com/downloads/connector/j/
2. Download the `.jar` file (e.g., `mysql-connector-java-8.x.x.jar`)
3. Rename it to `mysql-connector-java.jar`
4. Place it in the `lib/` folder of this project

### Step 2 — Set Up the Database
1. Open MySQL Workbench or MySQL command line
2. Run the SQL file:
   ```sql
   source /path/to/DigitalNoticeBoard/database_setup.sql
   ```
   Or paste the contents of `database_setup.sql` directly.

### Step 3 — Configure Database Credentials
Open `src/com/noticeboard/db/DBConnection.java` and update:
```java
private static final String USERNAME = "root";     // Your MySQL username
private static final String PASSWORD = "";         // Your MySQL password
```

### Step 4 — Compile and Run

**Windows:**
```cmd
run.bat
```

**Linux / macOS:**
```bash
chmod +x run.sh
./run.sh
```

**Or using an IDE (IntelliJ IDEA / Eclipse / NetBeans):**
- Import the project
- Add `lib/mysql-connector-java.jar` to the classpath/module dependencies
- Run `com.noticeboard.Main`

---

## 🔑 Default Login Credentials

| Role    | Email                      | Password    |
|---------|----------------------------|-------------|
| Admin   | admin@noticeboard.com      | admin123    |
| Student | john@student.com           | student123  |

---

## ✨ Features

### 🔐 Login & Registration
- Combined login/register form with smooth toggle
- Role selector: **Admin** or **Student**
- If **Student** is selected → Student ID field appears
- Email uniqueness validation
- Password minimum length check

### 👨‍💼 Admin Dashboard
- **Add Notice** — Title, Content, Category, Priority, optional Attachment
- **Edit Notice** — Update any field or replace attachment
- **Delete Notice** — With confirmation dialog
- **View Notice** — Full notice view with download option
- **Search** — Real-time keyword search across title and content
- Sortable notice table with priority color coding
- Attachment indicator column

### 👨‍🎓 Student Dashboard
- **View Notices** — Card-based notice list with preview
- **Search** — Search by keyword (Enter key or Search button)
- **Filter by Category** — Dropdown or sidebar quick-filter buttons
- **Download Attachment** — Save attached files locally
- Student info card (name, email, student ID) in sidebar
- Clickable cards open full notice detail dialog

### 🗄️ Database (MySQL)
| Table    | Columns                                                     |
|----------|-------------------------------------------------------------|
| `users`  | id, name, email, password, role, student_id, created_at    |
| `notices`| id, title, content, category, priority, attachment_name, attachment_data, attachment_type, posted_by, created_at, updated_at |

---

## 🎨 UI Highlights
- Dark sidebar navigation
- Color-coded priority badges (Urgent=Red, High=Orange, Medium=Blue, Low=Green)
- Category color coding across the app
- Hover effects on notice cards
- Responsive table with alternating row colors
- Drop-shadow card style for modals

---

## 🛠 Troubleshooting

| Problem | Solution |
|---------|----------|
| `ClassNotFoundException: com.mysql.cj.jdbc.Driver` | Add `mysql-connector-java.jar` to `lib/` folder |
| Cannot connect to MySQL | Check username/password in `DBConnection.java`, ensure MySQL is running |
| `database_setup.sql` errors | Run it fresh on a new MySQL installation |
| Blank notice list | Run the sample INSERT statements in `database_setup.sql` |
| Attachment download fails | Ensure the notice was uploaded with attachment (check DB) |
