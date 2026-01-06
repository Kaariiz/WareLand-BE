# Rangkuman Fitur Property Management - WareLand Backend

Dokumen ini menjelaskan **flow lengkap** dari fitur Property Management (CRUD oleh Seller) dengan penekanan pada **konsep OOP (Object-Oriented Programming)** yang diterapkan.

---

## 📁 Struktur File Terkait

```
src/main/java/com/wareland/
├── property/                          # Module Property (SELLER-ONLY)
│   ├── controller/
│   │   └── PropertyController.java    # API: /api/seller/properties/*
│   ├── model/
│   │   └── Property.java              # Entity dengan encapsulated behavior
│   ├── repository/
│   │   └── PropertyRepository.java    # Spring Data JPA interface
│   └── service/
│       └── PropertyService.java       # Business logic dengan ownership validation
│
└── user/
    └── model/
        └── Seller.java                # Owner dari Property (extends User)
```

---

## 🔷 Konsep OOP yang Diterapkan

### 1. **Encapsulation (Enkapsulasi)**

```java
// Property.java - Entity dengan private fields
@Entity
@Table(name = "properties")
public class Property {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer propertyId;      // Private, controlled access
    
    @NotBlank
    private String address;
    
    @Min(0)
    private double price;
    
    private String imageUrl;
    private String description;
    
    // Encapsulated behavior - update logic di dalam entity
    public void updateDetails(String newAddress, double newPrice, 
                              String newDescription, String newImageUrl) {
        if (newAddress != null && !newAddress.isBlank()) {
            this.address = newAddress;
        }
        if (newPrice >= 0) {
            this.price = newPrice;
        }
        if (newDescription != null) {
            this.description = newDescription;
        }
        if (newImageUrl != null && !newImageUrl.isBlank()) {
            this.imageUrl = newImageUrl;
        }
    }
    
    // Behavior: display method
    public String displayProperty() {
        return String.format("Property{id=%d, address='%s', price=%.2f, image='%s'}", 
                             propertyId, address, price, imageUrl);
    }
}
```

- **Private fields** dengan getter/setter terkontrol
- **Behavior encapsulation**: Logic update di dalam method `updateDetails()`
- **Validation** di dalam entity (null checks, range checks)

### 2. **Aggregation (Agregasi)**

```
┌─────────────────────────────────────────────────────────────┐
│                         Property                            │
├─────────────────────────────────────────────────────────────┤
│ - propertyId: Integer                                       │
│ - address: String                                           │
│ - price: double                                             │
│ - imageUrl: String                                          │
│ - description: String                                       │
│                                                             │
│ + seller ◄────────────── ManyToOne (Required)               │
│   ┌─────────────────┐                                       │
│   │     Seller      │ Property MILIK seorang Seller         │
│   │   (Owner)       │ (Aggregation - whole/part)            │
│   └─────────────────┘                                       │
│                                                             │
│ + reviews ◄─────────── OneToMany (Cascade)                  │
│   ┌─────────────────┐                                       │
│   │  List<Review>   │ Property MEMILIKI banyak Review       │
│   └─────────────────┘                                       │
└─────────────────────────────────────────────────────────────┘
```

```java
// Property.java - Aggregation relationships
@ManyToOne(optional = false, fetch = FetchType.LAZY)
@JoinColumn(name = "seller_id", nullable = false)
private Seller seller;  // TEPAT 1 owner

@OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true)
private List<Review> reviews = new ArrayList<>();
```

### 3. **Inheritance (Pewarisan)**

```
        ┌─────────────┐
        │    User     │  ← Abstract Class
        │  (abstract) │
        └──────┬──────┘
               │
      ┌────────┴────────┐
      │                 │
┌─────▼─────┐     ┌─────▼─────┐
│   Buyer   │     │  Seller   │
└───────────┘     └─────┬─────┘
                        │
                        │ 1
                        │
                        ▼ *
                  ┌───────────┐
                  │ Property  │
                  └───────────┘
```

- **Seller** extends **User** (Single Table Inheritance)
- Hanya **Seller** yang dapat memiliki dan mengelola Property

### 4. **Polymorphism via Downcasting**

```java
// PropertyController.java - Type checking dan downcasting
private Seller getCurrentSeller() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String username = (auth != null) ? auth.getName() : null;
    
    User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan"));
    
    // Polymorphic check
    if (user.getUserRole() != UserRole.SELLER) {
        throw new BusinessException("Hanya Seller yang boleh mengakses fitur ini");
    }
    
    // Downcasting setelah validasi
    return (Seller) user;
}
```

---

## ➕ Feature: CREATE PROPERTY

### Flow Diagram

```
┌─────────┐  POST /api/seller/properties  ┌──────────────────┐
│ Client  │ ─────────────────────────────▶│PropertyController│
└─────────┘  Header: Authorization        └────────┬─────────┘
             {address, price, description,         │
              imageUrl}                            ▼
             (Auth Required: SELLER)      ┌────────────────────┐
                                          │ getCurrentSeller() │
                                          │ Extract from JWT   │
                                          │ Validate SELLER    │
                                          └────────┬───────────┘
                                                   │
                                                   ▼
                                          ┌────────────────┐
                                          │PropertyService │
                                          │ createProperty │
                                          └────────┬───────┘
                                                   │
                                 ┌─────────────────┴─────────────────┐
                                 │                                   │
                                 ▼                                   ▼
                        ┌────────────────┐                  ┌────────────────┐
                        │ validateSeller │                  │ Set Ownership  │
                        │ (Role Check)   │                  │ data.setSeller │
                        │ UserRole.SELLER│                  │   (seller)     │
                        └────────────────┘                  └────────────────┘
                                                                    │
                                                                    ▼
                                                           ┌────────────────┐
                                                           │PropertyRepository│
                                                           │    save()      │
                                                           └────────────────┘
```

### OOP Highlight: Ownership Establishment

```java
// PropertyService.java - Establishing ownership
public Property createProperty(Seller seller, Property data) {
    validateSeller(seller);  // Role validation
    
    if (data == null) {
        throw new BadRequestException("Data property tidak boleh kosong");
    }
    
    // Establish ownership relationship (Aggregation)
    data.setSeller(seller);
    
    return propertyRepository.save(data);
}

private void validateSeller(Seller seller) {
    if (seller == null || seller.getUserRole() != UserRole.SELLER) {
        throw new BusinessException("Hanya Seller yang dapat mengelola Property");
    }
}
```

### File Terkait

| File | Deskripsi |
|------|-----------|
| `PropertyController.java` (L31-36) | Endpoint `POST /api/seller/properties` |
| `PropertyController.java` (L62-75) | Method `getCurrentSeller()` |
| `PropertyService.java` (L26-33) | Method `createProperty()` |
| `Property.java` (L35-39) | ManyToOne relationship to Seller |

---

## 📋 Feature: GET SELLER'S PROPERTIES (List Own)

### Flow Diagram

```
┌─────────┐  GET /api/seller/properties  ┌──────────────────┐
│ Client  │ ────────────────────────────▶│PropertyController│
└─────────┘  Header: Authorization       └────────┬─────────┘
             (Auth Required: SELLER)              │
                                                  ▼
                                         ┌────────────────────┐
                                         │ getCurrentSeller() │
                                         └────────┬───────────┘
                                                  │
                                                  ▼
                                         ┌────────────────┐
                                         │PropertyService │
                                         │getSellerProps  │
                                         └────────┬───────┘
                                                  │
                                                  ▼
                                         ┌────────────────────┐
                                         │ PropertyRepository │
                                         │  findBySeller()    │
                                         └────────┬───────────┘
                                                  │
                                                  ▼
                                         ┌────────────────┐
                                         │ List<Property> │
                                         └────────────────┘
```

### OOP Highlight: Query by Relationship

```java
// PropertyRepository.java - Spring Data JPA derived query
@Repository
public interface PropertyRepository extends JpaRepository<Property, Integer> {
    
    // Query berdasarkan relasi Aggregation
    List<Property> findBySeller(Seller seller);
    
    // Query untuk ownership check
    boolean existsByPropertyIdAndSeller(Integer propertyId, Seller seller);
    
    // Query untuk get specific property by owner
    Optional<Property> findByPropertyIdAndSeller(Integer propertyId, Seller seller);
}
```

### File Terkait

| File | Deskripsi |
|------|-----------|
| `PropertyController.java` (L38-43) | Endpoint `GET /api/seller/properties` |
| `PropertyService.java` (L61-65) | Method `getSellerProperties()` |
| `PropertyRepository.java` (L14) | Query method `findBySeller()` |

---

## ✏️ Feature: UPDATE PROPERTY

### Flow Diagram

```
┌─────────┐  PUT /api/seller/properties/{id}  ┌──────────────────┐
│ Client  │ ──────────────────────────────────▶│PropertyController│
└─────────┘  Header: Authorization             └────────┬─────────┘
             {address?, price?,                        │
              description?, imageUrl?}                 ▼
             (Auth Required: SELLER)          ┌────────────────────┐
                                              │ getCurrentSeller() │
                                              └────────┬───────────┘
                                                       │
                                                       ▼
                                              ┌────────────────┐
                                              │PropertyService │
                                              │ updateProperty │
                                              └────────┬───────┘
                                                       │
                                 ┌─────────────────────┴─────────────────────┐
                                 │                                           │
                                 ▼                                           ▼
                        ┌────────────────┐                          ┌─────────────────┐
                        │ validateSeller │                          │ Verify Ownership│
                        └────────────────┘                          │findByPropertyId │
                                                                    │   AndSeller     │
                                                                    └────────┬────────┘
                                                                             │
                                                              ┌──────────────┴──────────────┐
                                                              │                             │
                                                          Owned                        Not Owned
                                                              │                             │
                                                              ▼                             ▼
                                                     ┌────────────────┐           ┌─────────────────┐
                                                     │    Property    │           │ BusinessException│
                                                     │ updateDetails()│           │ "Tidak berhak"  │
                                                     │ [Encapsulated] │           └─────────────────┘
                                                     └────────┬───────┘
                                                              │
                                                              ▼
                                                     ┌────────────────┐
                                                     │PropertyRepository│
                                                     │    save()      │
                                                     └────────────────┘
```

### OOP Highlight: Encapsulated Update Behavior

```java
// Property.java - Entity bertanggung jawab atas validasi dan update sendiri
public void updateDetails(String newAddress, double newPrice, 
                          String newDescription, String newImageUrl) {
    // Validasi dan update di dalam entity (Encapsulation)
    if (newAddress != null && !newAddress.isBlank()) {
        this.address = newAddress;
    }
    if (newPrice >= 0) {
        this.price = newPrice;
    }
    if (newDescription != null) {
        this.description = newDescription;
    }
    if (newImageUrl != null && !newImageUrl.isBlank()) {
        this.imageUrl = newImageUrl;
    }
}

// PropertyService.java - Service hanya orchestrate
public void updateProperty(Seller seller, Property property) {
    validateSeller(seller);
    
    Property existing = propertyRepository
        .findByPropertyIdAndSeller(property.getPropertyId(), seller)
        .orElseThrow(() -> new BusinessException("Tidak berhak atau tidak ditemukan"));
    
    // Delegasi update ke entity (Tell, Don't Ask)
    existing.updateDetails(
        property.getAddress(), 
        property.getPrice(), 
        property.getDescription(), 
        property.getImageUrl()
    );
    
    propertyRepository.save(existing);
}
```

**OOP Principle: Tell, Don't Ask**
- Service tidak mengakses field langsung, tapi meminta entity untuk update dirinya sendiri

### File Terkait

| File | Deskripsi |
|------|-----------|
| `PropertyController.java` (L45-52) | Endpoint `PUT /api/seller/properties/{id}` |
| `PropertyService.java` (L35-46) | Method `updateProperty()` |
| `Property.java` (L46-59) | Method `updateDetails()` |
| `PropertyRepository.java` (L18) | Query `findByPropertyIdAndSeller()` |

---

## 🗑️ Feature: DELETE PROPERTY

### Flow Diagram

```
┌─────────┐  DELETE /api/seller/properties/{id}  ┌──────────────────┐
│ Client  │ ─────────────────────────────────────▶│PropertyController│
└─────────┘  Header: Authorization                └────────┬─────────┘
             (Auth Required: SELLER)                       │
                                                           ▼
                                                  ┌────────────────────┐
                                                  │ getCurrentSeller() │
                                                  └────────┬───────────┘
                                                           │
                                                           ▼
                                                  ┌────────────────┐
                                                  │PropertyService │
                                                  │ deleteProperty │
                                                  └────────┬───────┘
                                                           │
                                    ┌──────────────────────┴──────────────────────┐
                                    │                                             │
                                    ▼                                             ▼
                           ┌────────────────┐                            ┌─────────────────┐
                           │ validateSeller │                            │ Verify Ownership│
                           └────────────────┘                            │findByIdAndSeller│
                                                                         └────────┬────────┘
                                                                                   │
                                                                 ┌─────────────────┴─────────────────┐
                                                                 │                                   │
                                                             Owned                              Not Owned
                                                                 │                                   │
                                                                 ▼                                   ▼
                                                        ┌────────────────┐               ┌─────────────────┐
                                                        │PropertyRepository│             │ BusinessException│
                                                        │   delete()     │               └─────────────────┘
                                                        └────────┬───────┘
                                                                 │
                                                                 ▼
                                                        ┌─────────────────┐
                                                        │ CASCADE DELETE  │
                                                        │ All Reviews     │
                                                        └─────────────────┘
```

### OOP Highlight: Cascade Delete via Composition

```java
// Property.java - OneToMany dengan Cascade
@OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true)
private List<Review> reviews = new ArrayList<>();
```

**Cascade Behavior:**
- Ketika Property dihapus, semua Review terkait **otomatis terhapus**
- `orphanRemoval = true`: Review tanpa Property otomatis dihapus
- Ini adalah implementasi **Composition** (bagian tidak bisa exist tanpa whole)

### File Terkait

| File | Deskripsi |
|------|-----------|
| `PropertyController.java` (L54-59) | Endpoint `DELETE /api/seller/properties/{id}` |
| `PropertyService.java` (L48-53) | Method `deleteProperty()` |
| `Property.java` (L41-43) | Cascade configuration |

---

## 🔐 Feature: VERIFY OWNERSHIP (Internal)

### Purpose
Method internal untuk memverifikasi apakah Seller tertentu memiliki Property tertentu.

```java
// PropertyService.java
@Transactional(readOnly = true)
public boolean verifyOwnership(Seller seller, int propertyId) {
    validateSeller(seller);
    return propertyRepository.existsByPropertyIdAndSeller(propertyId, seller);
}
```

### File Terkait

| File | Deskripsi |
|------|-----------|
| `PropertyService.java` (L55-59) | Method `verifyOwnership()` |
| `PropertyRepository.java` (L16) | Query `existsByPropertyIdAndSeller()` |

---

## 📊 Summary API Endpoints

| Feature | Method | Endpoint | Auth | Role |
|---------|--------|----------|------|------|
| **Create Property** | POST | `/api/seller/properties` | ✅ | SELLER |
| **List Own Properties** | GET | `/api/seller/properties` | ✅ | SELLER |
| **Update Property** | PUT | `/api/seller/properties/{id}` | ✅ | SELLER |
| **Delete Property** | DELETE | `/api/seller/properties/{id}` | ✅ | SELLER |

---

## 🔗 Entity Relationship Diagram

```
┌─────────────────┐         ┌─────────────────┐         ┌─────────────────┐
│      User       │         │    Property     │         │     Review      │
│   (abstract)    │         │                 │         │                 │
├─────────────────┤         ├─────────────────┤         ├─────────────────┤
│ - userId        │         │ - propertyId    │         │ - id            │
│ - username      │         │ - address       │         │ - rating        │
│ - password      │         │ - price         │         │ - comment       │
│ - name          │         │ - description   │         │ - createdAt     │
│ - email         │         │ - imageUrl      │         │ - updatedAt     │
│ - phoneNumber   │         │                 │         │                 │
└────────┬────────┘         └────────┬────────┘         └────────┬────────┘
         │                           │                           │
         │                           │                           │
    ┌────┴────┐                      │                           │
    │         │                      │                           │
┌───▼───┐ ┌───▼───┐                  │                           │
│ Buyer │ │Seller │ 1 ◄──────────────┤ * (owner)                 │
└───────┘ └───────┘                  │                           │
    │                                │                           │
    │ 1                              │ 1                         │
    │                                │                           │
    └───────────────── * ◄───────────┴─────────────────── * ◄────┘
         (gives review)                    (has reviews)
```

---

## ✅ OOP Principles Recap

| Principle | Implementation |
|-----------|----------------|
| **Encapsulation** | Private fields, `updateDetails()` method, `displayProperty()` |
| **Aggregation** | Property belongs to Seller (ManyToOne, required) |
| **Composition** | Property has Reviews (OneToMany, cascade delete) |
| **Inheritance** | Seller extends User (Single Table Inheritance) |
| **Polymorphism** | Downcasting User → Seller via `instanceof` check |
| **Single Responsibility** | Separate Controller, Service, Repository layers |
| **Tell, Don't Ask** | Service calls `entity.updateDetails()` instead of getting/setting fields |
| **Dependency Injection** | Constructor injection di semua class |
| **Interface Abstraction** | `PropertyRepository extends JpaRepository<Property, Integer>` |

---

## 🔐 Security Model

```
┌──────────────────────────────────────────────────────────────────┐
│                    Property Management Security                   │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│  1. JWT Token Required                                           │
│     └─▶ JwtAuthenticationFilter validates token                  │
│                                                                  │
│  2. Role Validation                                              │
│     └─▶ getCurrentSeller() checks UserRole.SELLER               │
│                                                                  │
│  3. Ownership Verification                                       │
│     └─▶ findByPropertyIdAndSeller() ensures owner match         │
│                                                                  │
│  4. Business Rule Enforcement                                    │
│     └─▶ PropertyService.validateSeller() for every operation    │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
```

| Layer | Check | Error |
|-------|-------|-------|
| **Filter** | Valid JWT token | 401 Unauthorized |
| **Controller** | User is SELLER | BusinessException |
| **Service** | Seller != null, role == SELLER | BusinessException |
| **Repository** | Property owned by Seller | BusinessException |
