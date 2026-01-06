# Rangkuman Fitur Authentication & User Management - WareLand Backend

Dokumen ini menjelaskan **flow lengkap** dari setiap fitur authentication dan user management dengan penekanan pada **konsep OOP (Object-Oriented Programming)** yang diterapkan.

---

## 📁 Struktur File Terkait

```
src/main/java/com/wareland/
├── user/
│   ├── model/
│   │   ├── User.java              # Abstract Entity (Base Class)
│   │   ├── Buyer.java             # Concrete Class (extends User)
│   │   ├── Seller.java            # Concrete Class (extends User)
│   │   ├── UserRole.java          # Enum untuk role
│   │   └── RevokedToken.java      # Entity untuk token logout
│   ├── dto/
│   │   ├── LoginRequest.java      # DTO request login
│   │   ├── LoginResponse.java     # DTO response login
│   │   ├── UserRegisterRequest.java  # DTO request register
│   │   ├── UpdateProfileRequest.java # DTO request update
│   │   └── UserProfileResponse.java  # DTO response profil
│   ├── repository/
│   │   ├── UserRepository.java
│   │   └── RevokedTokenRepository.java
│   ├── service/
│   │   ├── UserService.java       # Business logic utama
│   │   └── AuthService.java       # Logic logout & token revocation
│   └── controller/
│       ├── AuthController.java    # API: /api/auth/*
│       └── UserController.java    # API: /api/users/*
├── common/
│   ├── security/
│   │   ├── JwtTokenProvider.java  # Pembuatan & validasi JWT
│   │   └── JwtAuthenticationFilter.java # Filter validasi request
│   ├── config/
│   │   └── SecurityConfig.java    # Konfigurasi Spring Security
│   └── response/
│       └── ApiResponse.java       # Wrapper response generik
```

---

## 🔷 Konsep OOP yang Diterapkan

### 1. **Inheritance (Pewarisan)**

```
        ┌─────────────────┐
        │   User          │  ← Abstract Class
        │   (abstract)    │
        ├─────────────────┤
        │ - userId        │
        │ - username      │
        │ - password      │
        │ - name          │
        │ - email         │
        │ - phoneNumber   │
        │ - imageUrl      │
        │ - createdAt     │
        │ - updatedAt     │
        ├─────────────────┤
        │ + register()*   │  ← Abstract method
        │ + login()       │
        │ + updateProfile │
        │ + getUserRole() │
        └────────┬────────┘
                 │
        ┌────────┴────────┐
        │                 │
   ┌────▼────┐       ┌────▼────┐
   │  Buyer  │       │ Seller  │
   ├─────────┤       ├─────────┤
   │-reviews │       │-proper- │
   │         │       │ ties    │
   ├─────────┤       ├─────────┤
   │+register│       │+register│
   └─────────┘       └─────────┘
```

**Penerapan:**
- `User` adalah **abstract class** yang menjadi base class untuk `Buyer` dan `Seller`
- Method `register()` dideklarasikan sebagai **abstract** di `User`, harus diimplementasikan oleh subclass
- Strategy **Single Table Inheritance** dengan discriminator column `role`

### 2. **Abstraction (Abstraksi)**

- Class `User` menyediakan **kontrak** (abstract method) tanpa detail implementasi
- Service layer (`UserService`, `AuthService`) meng-abstract logic bisnis dari controller
- DTO classes meng-abstract representasi data untuk transfer

### 3. **Encapsulation (Enkapsulasi)**

```java
// User.java - Field bersifat private
private Long userId;
private String username;
private String password;  // Password tidak dapat diakses langsung

// Akses via getter/setter
public String getUsername() { return username; }
public void setPassword(String password) { this.password = password; }
```

- Password di-hash sebelum disimpan menggunakan `PasswordEncoder`
- Logic update profil dienkapsulasi dalam method `updateBasicProfile()`

### 4. **Polymorphism (Polimorfisme)**

```java
// UserService.java
User user;
if (request.getRole() == UserRole.BUYER) {
    Buyer buyer = new Buyer();
    buyer.register();  // Polymorphic call
    user = buyer;
} else if (request.getRole() == UserRole.SELLER) {
    Seller seller = new Seller();
    seller.register();  // Polymorphic call
    user = seller;
}
```

- Method `register()` dipanggil secara polymorphic berdasarkan tipe konkret
- Method `getUserRole()` menentukan role berdasarkan `instanceof` check

---

## 🔐 Feature: LOGIN

### Flow Diagram

```
┌─────────┐    POST /api/auth/login     ┌────────────────┐
│ Client  │ ─────────────────────────▶  │ AuthController │
└─────────┘  {username, password}       └───────┬────────┘
                                                │
                                                ▼
                                        ┌───────────────┐
                                        │  UserService  │
                                        │   login()     │
                                        └───────┬───────┘
                                                │
                        ┌───────────────────────┼───────────────────────┐
                        │                       │                       │
                        ▼                       ▼                       ▼
               ┌────────────────┐      ┌───────────────┐      ┌─────────────────┐
               │ UserRepository │      │PasswordEncoder│      │ JwtTokenProvider│
               │ findByUsername │      │   matches()   │      │ generateToken() │
               └────────────────┘      └───────────────┘      └─────────────────┘
                                                                       │
                                                                       ▼
                                                              ┌─────────────────┐
                                                              │ LoginResponse   │
                                                              │ {token, profile}│
                                                              └─────────────────┘
```

### Class Interaction

| Layer | Class | Method | OOP Concept |
|-------|-------|--------|-------------|
| **Controller** | `AuthController` | `login()` | Composition (aggregates services) |
| **Service** | `UserService` | `login()` | Encapsulation (business logic) |
| **Repository** | `UserRepository` | `findByUsername()`, `findByEmail()` | Abstraction (interface) |
| **Security** | `JwtTokenProvider` | `generateToken()` | Single Responsibility |
| **DTO** | `LoginRequest`, `LoginResponse` | - | Data Transfer Object pattern |

### File Terkait

| File | Deskripsi |
|------|-----------|
| `AuthController.java` (L62-73) | Endpoint `POST /api/auth/login` |
| `UserService.java` (L90-119) | Method `login()` dengan validasi |
| `JwtTokenProvider.java` (L36-46) | Method `generateToken()` |
| `LoginRequest.java` | DTO dengan validasi `@NotBlank` |
| `LoginResponse.java` | Komposisi: token + UserProfileResponse |

---

## 📝 Feature: REGISTER

### Flow Diagram

```
┌─────────┐   POST /api/auth/register   ┌────────────────┐
│ Client  │ ─────────────────────────▶  │ AuthController │
└─────────┘  {username, password,       └───────┬────────┘
              name, email, phone, role}         │
                                                ▼
                                        ┌───────────────┐
                                        │  UserService  │
                                        │  register()   │
                                        └───────┬───────┘
                                                │
         ┌──────────────────────────────────────┼─────────────────────────────────┐
         │                                      │                                 │
         ▼                                      ▼                                 ▼
┌────────────────┐                     ┌───────────────────┐            ┌───────────────┐
│ UserRepository │                     │ Buyer/Seller      │            │PasswordEncoder│
│ existsByUser   │                     │ register()        │            │ encode()      │
│ existsByEmail  │                     │ [Polymorphic]     │            └───────────────┘
└────────────────┘                     └───────────────────┘
         │
         ▼
┌────────────────┐
│ UserRepository │
│   save(user)   │
└────────────────┘
         │
         ▼
┌───────────────────┐
│UserProfileResponse│
└───────────────────┘
```

### OOP Highlight: Polymorphic Object Creation

```java
// UserService.java - Lines 64-75
User user;
if (request.getRole() == UserRole.BUYER) {
    Buyer buyer = new Buyer();    // Concrete class instantiation
    buyer.register();              // Polymorphic method call
    user = buyer;                  // Upcasting to parent type
} else if (request.getRole() == UserRole.SELLER) {
    Seller seller = new Seller();
    seller.register();
    user = seller;
}
```

### File Terkait

| File | Deskripsi |
|------|-----------|
| `AuthController.java` (L49-57) | Endpoint `POST /api/auth/register` |
| `UserService.java` (L56-85) | Method `register()` dengan factory logic |
| `User.java` (L70) | Abstract method `register()` |
| `Buyer.java` (L25-29) | Override `register()` |
| `Seller.java` (L27-31) | Override `register()` |
| `UserRegisterRequest.java` | DTO dengan custom `@StrongPassword` validation |

---

## 🚪 Feature: LOGOUT

### Flow Diagram

```
┌─────────┐   POST /api/auth/logout    ┌────────────────┐
│ Client  │ ───────────────────────▶   │ AuthController │
└─────────┘  Header: Authorization     └───────┬────────┘
             Bearer <token>                    │
                                               ▼
                                       ┌───────────────┐
                                       │  AuthService  │
                                       │   logout()    │
                                       └───────┬───────┘
                                               │
                                               ▼
                                    ┌─────────────────────┐
                                    │RevokedTokenRepository│
                                    │ save(RevokedToken)  │
                                    └─────────────────────┘
```

### OOP Highlight: Encapsulation dalam RevokedToken

```java
// RevokedToken.java - Constructor automatically sets timestamp
public RevokedToken(String token) {
    this.token = token;
    this.revokedAt = Instant.now();  // Internal state management
}
```

### File Terkait

| File | Deskripsi |
|------|-----------|
| `AuthController.java` (L96-105) | Endpoint `POST /api/auth/logout` |
| `AuthService.java` (L25-41) | Method `logout()` dengan token extraction |
| `RevokedToken.java` | Entity dengan encapsulated state |
| `JwtAuthenticationFilter.java` (L50-51) | Validasi token tidak di-revoke |

---

## ✏️ Feature: UPDATE USER

### Flow Diagram

```
┌─────────┐    PUT /api/users/{id}     ┌────────────────┐
│ Client  │ ────────────────────────▶  │ UserController │
└─────────┘  {name?, email?,           └───────┬────────┘
              phoneNumber?, imageUrl?,         │
              oldPassword?, newPassword?}      ▼
                                       ┌───────────────┐
                                       │  UserService  │
                                       │ updateProfile │
                                       └───────┬───────┘
                                               │
              ┌────────────────────────────────┼────────────────────────────────┐
              │                                │                                │
              ▼                                ▼                                ▼
     ┌────────────────┐              ┌─────────────────┐              ┌───────────────┐
     │ UserRepository │              │      User       │              │PasswordEncoder│
     │  findById(id)  │              │updateBasicProfile│             │ matches/encode│
     └────────────────┘              └─────────────────┘              └───────────────┘
                                     [Encapsulated]
```

### OOP Highlight: Encapsulated Update dalam Entity

```java
// User.java - Lines 83-101
public void updateBasicProfile(
        String name,
        String email,
        String phoneNumber,
        String imageUrl
) {
    // Null-safe partial update - encapsulates validation
    if (name != null) this.name = name;
    if (email != null) this.email = email;
    if (phoneNumber != null) this.phoneNumber = phoneNumber;
    if (imageUrl != null) this.imageUrl = imageUrl;
}
```

### File Terkait

| File | Deskripsi |
|------|-----------|
| `UserController.java` (L58-69) | Endpoint `PUT /api/users/{id}` |
| `UserService.java` (L161-202) | Method `updateProfile()` |
| `User.java` (L83-101) | Method `updateBasicProfile()` |
| `UpdateProfileRequest.java` | DTO dengan semua field opsional |

---

## 🗑️ Feature: DELETE USER

### Flow Diagram

```
┌─────────┐  DELETE /api/users/{id}    ┌────────────────┐
│ Client  │ ────────────────────────▶  │ UserController │
└─────────┘                            └───────┬────────┘
                                               │
                                               ▼
                                       ┌───────────────┐
                                       │  UserService  │
                                       │ deleteAccount │
                                       └───────┬───────┘
                                               │
                                               ▼
                                      ┌────────────────┐
                                      │ UserRepository │
                                      │  delete(user)  │
                                      └───────┬────────┘
                                              │
                                              ▼
                                    ┌──────────────────┐
                                    │ CASCADE DELETE   │
                                    │ - Reviews (Buyer)│
                                    │ - Properties     │
                                    │   (Seller)       │
                                    └──────────────────┘
```

### OOP Highlight: Cascade Delete via Composition

```java
// Buyer.java - Cascade relationship
@OneToMany(mappedBy = "buyer", cascade = CascadeType.ALL, orphanRemoval = true)
private List<Review> reviews = new ArrayList<>();

// Seller.java - Cascade relationship
@OneToMany(mappedBy = "seller", cascade = CascadeType.ALL, orphanRemoval = true)
private List<Property> properties = new ArrayList<>();
```

Ketika user dihapus, semua **child entities** (Review untuk Buyer, Property untuk Seller) otomatis terhapus berkat `CascadeType.ALL` dan `orphanRemoval = true`.

### File Terkait

| File | Deskripsi |
|------|-----------|
| `UserController.java` (L74-82) | Endpoint `DELETE /api/users/{id}` |
| `UserService.java` (L207-209) | Method `deleteAccount()` |
| `Buyer.java` (L19-20) | Cascade delete reviews |
| `Seller.java` (L21-22) | Cascade delete properties |

---

## 🔒 Security Layer (Cross-cutting)

### JWT Authentication Filter Flow

```
┌──────────────────┐
│ Incoming Request │
└────────┬─────────┘
         │
         ▼
┌────────────────────────────┐
│ Has Authorization Header?  │
└────────┬───────────────────┘
         │
    ┌────┴────┐
    │         │
   Yes        No ──────────────────────────────────┐
    │                                              │
    ▼                                              │
┌────────────────────┐                             │
│ Starts with Bearer?│                             │
└────────┬───────────┘                             │
         │                                         │
    ┌────┴────┐                                    │
    │         │                                    │
   Yes        No ──────────────────────────────────┤
    │                                              │
    ▼                                              │
┌─────────────────────────┐                        │
│ Token in RevokedTokens? │                        │
└────────┬────────────────┘                        │
         │                                         │
    ┌────┴────┐                                    │
    │         │                                    │
   Yes        No                                   │
    │         │                                    │
    │         ▼                                    │
    │   ┌─────────────┐                            │
    │   │ Token Valid?│                            │
    │   └──────┬──────┘                            │
    │          │                                   │
    │     ┌────┴────┐                              │
    │     │         │                              │
    │    Yes        No ────────────────────────────┤
    │     │                                        │
    │     ▼                                        │
    │   ┌────────────────────┐                     │
    │   │ Set SecurityContext│                     │
    │   │ (Authenticated)    │                     │
    │   └─────────┬──────────┘                     │
    │             │                                │
    │             ▼                                │
    │   ┌────────────────────┐                     │
    └──▶│ Continue to        │◀────────────────────┘
        │ Controller         │
        └────────────────────┘
```

### File Terkait Security

| File | Deskripsi |
|------|-----------|
| `SecurityConfig.java` | Konfigurasi endpoint publik/protected |
| `JwtAuthenticationFilter.java` | Filter request dengan JWT |
| `JwtTokenProvider.java` | Generate & validate token |

---

## 📊 Summary API Endpoints

| Feature | Method | Endpoint | Auth Required |
|---------|--------|----------|---------------|
| **Register** | POST | `/api/auth/register` | ❌ No |
| **Login** | POST | `/api/auth/login` | ❌ No |
| **Logout** | POST | `/api/auth/logout` | ✅ Yes |
| **Get Profile (self)** | GET | `/api/auth/me` | ✅ Yes |
| **Get All Users** | GET | `/api/users` | ✅ Yes |
| **Get User by ID** | GET | `/api/users/{id}` | ✅ Yes |
| **Update Profile** | PUT | `/api/users/{id}` | ✅ Yes |
| **Delete Account** | DELETE | `/api/users/{id}` | ✅ Yes |
| **Get Users by Role** | GET | `/api/users/role/{role}` | ✅ Yes |

---

## ✅ OOP Principles Recap

| Principle | Implementation |
|-----------|----------------|
| **Inheritance** | `User` → `Buyer`, `Seller` (Single Table Inheritance) |
| **Abstraction** | Abstract class `User`, Service interfaces, DTO pattern |
| **Encapsulation** | Private fields, `updateBasicProfile()`, password hashing |
| **Polymorphism** | `register()` method override, `getUserRole()` via instanceof |
| **Composition** | `LoginResponse` contains `UserProfileResponse`, cascade relationships |
| **Single Responsibility** | Separate Controller, Service, Repository layers |
| **Dependency Injection** | Constructor injection throughout all classes |
