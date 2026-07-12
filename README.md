# 🔐 SecureVault - Password Manager

SecureVault is a premium, full-stack, zero-knowledge password manager web application. It combines enterprise-grade security with an ultra-modern user interface inspired by the **Apple iOS Human Interface Guidelines** (featuring SF Pro typography, frosted glass panels, native widgets, and a dynamic Light/Dark theme toggle).

---

## 🚀 Quick Start

### Prerequisites
* **Java 17+** (JDK)
* **Maven 3.6+**

### Run Locally (H2 In-Memory Database)
To run the application locally using the embedded H2 database (ideal for development and testing):
```bash
mvn spring-boot:run "-Dspring-boot.run.profiles=h2"
```
Once started, navigate to: **[http://localhost:8089/](http://localhost:8089/)**

> [!NOTE]
> Since the H2 database runs in-memory, all data is wiped on application restart. When launching the application for the first time, proceed to **Create Account** (`/auth/register`) to register your local credentials.

### Run in Production (Oracle Database)
Ensure you have an Oracle Database XE instance running locally on port `1521` (or configure your database parameters in `src/main/resources/application.properties`), then run:
```bash
mvn spring-boot:run
```

---

## 🛡️ Security Architecture

SecureVault is built on a **Zero-Knowledge Architecture**, ensuring your sensitive credentials never leave your device unencrypted.

| Feature | Implementation Detail |
| :--- | :--- |
| **Local-First Key Derivation** | Master passwords derive cryptographic keys locally on your device via **PBKDF2 SHA-256**. |
| **Vault Encryption** | Credentials are encrypted and decrypted locally using **AES-256-GCM** authenticated encryption. |
| **Session Control** | Secure HTTP session state managed through **Spring Security** (30-minute idle timeout). |
| **Multi-Factor Auth (2FA)** | Time-based One-Time Passwords (**TOTP** / Google Authenticator compatible) simulation. |
| **Account Recovery** | Dual-layered recovery using **BCrypt-hashed** security question answers and single-use validation codes. |
| **CSRF Protection** | Strict CSRF token validation on all state-changing endpoints. |

---

## 📋 Features

* **Frosted-Glass iOS UI**: Clean typography, fluid animations, dynamic dark/light mode toggle, and table lists matching iOS Grouped layouts.
* **Vault CRUD Operations**: Easily add, edit, category-filter, search, and delete password records.
* **Password Generator**: Generate highly customizable, random, and strong passwords (length, numbers, symbols, readablility).
* **Master Password Reveal**: Requires re-entering your master password for confirmation before displaying stored secrets.
* **Security Diagnostic Audits**: Real-time diagnostic scanning to detect weak, reused, or potentially leaked passwords.
* **Import/Export Backups**: Securely export and import encrypted JSON backups of your vault.
* **Account Recovery Flow**: Reset password options via secure OTP validation and security questions.

---

## 🏗️ Project Structure

```
password-manager/
├── src/main/java/
│   └── com/passmanager/
│       ├── config/          # Spring Security, JPA & database configurations
│       ├── controller/      # MVC controllers (Auth, Dashboard, Vault, Security, Profile)
│       ├── dto/             # Data Transfer Objects
│       ├── entity/          # JPA entities (User, PasswordEntry, SecurityQuestion, VerificationCode)
│       ├── repository/      # Spring Data JPA repositories
│       ├── security/        # Custom authentication providers & TOTP utils
│       ├── service/         # Service layers (UserService, VaultService, EmailService)
│       └── util/            # Cryptographic & Password strength helpers
├── src/main/resources/
│   ├── static/              # CSS rules (iOS themes) & graphic assets
│   └── templates/           # Thymeleaf layouts & fragment files
└── pom.xml                  # Maven dependencies & build rules
```

---

## 🧪 Testing

To run unit and integration tests (isolated in-memory using H2):
```bash
mvn test
```
