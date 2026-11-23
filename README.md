# CampusRent – Equipment Rental Management System

## Project Overview

CampusRent is a Java-based equipment rental management system developed for the CTU Advanced Java Programming (JD522) assignment.

The application allows students and staff to browse available campus equipment, create rental bookings and manage equipment returns. Administrators can manage users, equipment, bookings, reports and system settings.

The system uses a Java Swing graphical user interface connected to a MySQL database through JDBC.

## Technologies Used

- **Programming Language:** Java 21
- **GUI Framework:** Java Swing
- **Database:** MySQL
- **Database Connectivity:** JDBC
- **IDE:** IntelliJ IDEA
- **Database Management:** MySQL Workbench
- **Build Tool:** Maven
- **Version Control:** Git and GitHub

## Main Features

### 1. User Authentication
- Secure user login.
- User registration.
- Password hashing using PBKDF2WithHmacSHA256.
- Role-based access control.
- Logout functionality.

### 2. Equipment Management
- View equipment catalogue.
- Search equipment.
- Filter equipment by category.
- Add, edit and manage equipment.
- Import equipment from CSV files.
- Export equipment data to CSV.
- Track equipment availability.

### 3. Booking Management
- Create equipment bookings.
- View booking records.
- Approve pending bookings.
- Activate approved bookings.
- Cancel bookings with a required reason.
- Complete equipment returns.
- Track booking statuses.

### 4. Returns Management
- View active and overdue bookings.
- Identify overdue equipment.
- Calculate estimated late fees.
- Process equipment returns.
- Update equipment availability after returns.

### 5. User Management
- View registered users.
- Add new users.
- Edit user information.
- Change user roles.
- Activate and deactivate accounts.

### 6. Reports Dashboard
- View equipment statistics.
- View booking statistics.
- Monitor equipment availability.
- Display the top rented equipment.
- Analyse revenue by category and campus.
- Monitor overdue bookings.
- Export reports to CSV.

### 7. System Settings
- English, Afrikaans and isiZulu language support.
- Light and dark themes.
- Configurable export and backup directories.
- Save application preferences.
- Create database-data backups.

### 8. Advanced Java Features
- Object-oriented programming.
- Interfaces and inheritance.
- Java Collections Framework.
- Generic DAO implementation.
- Java Streams API.
- Lambda expressions.
- Inner and nested classes.
- Exception handling.
- File handling and serialization.
- Multithreading and concurrency.
- SwingWorker for background database operations.

## User Roles

| Role | Permissions |
|---|---|
| Student | Browse equipment, create bookings and manage eligible bookings. |
| Staff | Manage equipment, bookings and returns, and access reports. |
| Administrator | Access user management, equipment management, bookings, returns, reports and settings. |

## Project Structure

```text
CampusRent/
│
├── src/
│   └── main/
│       ├── java/
│       │   └── za/ac/ctu/campusrent/
│       │       ├── app/
│       │       ├── concurrency/
│       │       ├── dao/
│       │       ├── gui/
│       │       ├── model/
│       │       ├── service/
│       │       └── util/
│       │
│       └── resources/
│           └── za/ac/ctu/campusrent/i18n/
│               ├── Messages_en_ZA.properties
│               ├── Messages_af_ZA.properties
│               └── Messages_zu_ZA.properties
|            |
|            |____db.properties
|            |____schema.sql
│
├── pom.xml
├── .gitignore
└── README.md
```

## Database Setup

CampusRent uses a MySQL database named:

```sql
campusrent_db
```

The database contains five main tables:

1. `campus` – Stores campus information.
2. `app_user` – Stores user accounts and roles.
3. `equipment` – Stores equipment details and availability.
4. `booking` – Stores equipment rental bookings.
5. `audit_log` – Records important system actions.

### Database Configuration

The application uses environment variables for database configuration.

Configure the following variables in your IntelliJ IDEA Run Configuration:

```text
CAMPUSRENT_DB_URL=jdbc:mysql://localhost:3306/campusrent_db
CAMPUSRENT_DB_USER=your_mysql_username
CAMPUSRENT_DB_PASSWORD=your_mysql_password
```

Replace the example username and password with your local MySQL credentials.

**Important:** Never commit database passwords or other sensitive credentials to GitHub.

## Installation and Setup

### Prerequisites

Make sure the following software is installed:

- Java Development Kit (JDK) 21.
- IntelliJ IDEA.
- MySQL Server.
- MySQL Workbench.
- Git.
- Maven (or use IntelliJ's bundled Maven support).

### 1. Clone the Repository

Open a terminal and execute:

```bash
git clone YOUR_GITHUB_REPOSITORY_URL
```

Navigate into the project:

```bash
cd CampusRent
```

### 2. Open the Project

1. Open IntelliJ IDEA.
2. Select **Open**.
3. Select the CampusRent project folder.
4. Allow IntelliJ to load and synchronise the Maven dependencies.

### 3. Configure the Database

1. Start MySQL Server.
2. Open MySQL Workbench.
3. Create the `campusrent_db` database.
4. Run the project's database schema and sample-data SQL scripts.
5. Configure the database environment variables in IntelliJ.

### 4. Build the Application

Open the IntelliJ terminal and execute:

```bash
mvn clean compile
```

### 5. Run CampusRent

Run the main application class:

```text
za.ac.ctu.campusrent.app.Main
```

Alternatively, run `Main.java` directly from IntelliJ IDEA.

## Testing

The project includes backend and authentication tests.

| Test Suite | Tests Executed | Result |
|---|---:|---|
| Backend Tests | 29 | 29 Passed |
| Authentication Tests | 10 | 10 Passed |
| **Total** | **39** | **39 Passed** |

### Additional Testing

The application was also tested for:

- User registration and login.
- Role-based access permissions.
- Equipment management.
- Booking creation and status changes.
- Booking cancellation and audit logging.
- Equipment returns.
- CSV import and export.
- Reports and Stream API calculations.
- Language and theme preferences.
- Application logout.

### Stream Performance Test

A performance comparison was conducted using 100,000 synthetic booking records.

| Measurement | Recorded Result |
|---|---:|
| Synthetic bookings | 100,000 |
| Sequential execution | 25.2899 ms |
| Parallel execution | 25.7845 ms |
| Results match | True |

The test demonstrated that both approaches produced matching results. Parallel execution was not faster in this particular test.

## Screenshots

Add screenshots of the completed application to a folder named `screenshots` in the project root.

Suggested screenshots:

- Login screen.
- Dashboard.
- Equipment catalogue.
- Booking management.
- Returns management.
- User management.
- Reports dashboard.
- Settings and language selection.

You can display screenshots in this section using Markdown:

```markdown
![CampusRent Dashboard](screenshots/dashboard.png)
```

## Security

The application includes:

- Password hashing using PBKDF2WithHmacSHA256.
- PreparedStatement queries to help prevent SQL injection.
- Role-based access restrictions.
- Database transactions for booking operations.
- Audit logging for important actions.
- Sensitive configuration kept outside the source repository.

## Future Improvements

Possible improvements include:

- Additional reporting and analytics.
- Automated scheduled backups.
- More advanced equipment search and filtering.
- Improved dashboard visualisations.
- Additional validation and automated test coverage.

## Developer

**Project:** CampusRent – Equipment Rental Management System  
**Module:** Advanced Java Programming (JD522)  
**Institution:** CTU Training Solutions  
**Qualification:** Software Development Programme

## License

This project was developed for educational and assessment purposes as part of the CTU Advanced Java Programming module.
