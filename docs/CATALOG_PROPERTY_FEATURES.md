# Rangkuman Fitur Catalog & Property Management - WareLand Backend

Dokumen ini menjelaskan **flow lengkap** dari fitur Catalog (publik) dan Property Management (seller) dengan penekanan pada **konsep OOP (Object-Oriented Programming)** yang diterapkan.

---

## 📁 Struktur File Terkait

```
src/main/java/com/wareland/
├── catalog/                           # Module Catalog (PUBLIC API)
│   ├── controller/
│   │   └── CatalogController.java     # API: /api/catalog/*
│   ├── dto/
│   │   ├── CatalogPropertyResponse.java  # DTO response dengan nested SellerInfo
│   │   └── CatalogSearchRequest.java     # DTO untuk parameter pencarian
│   ├── mapper/
│   │   └── CatalogMapper.java         # Mapper Entity → DTO (Single Responsibility)
│   ├── repository/
│   │   └── CatalogRepository.java     # Adapter Pattern untuk read-only access
│   └── service/
│       └── CatalogService.java        # Business logic catalog
│
├── property/                          # Module Property (SELLER-ONLY API)
│   ├── controller/
│   │   └── PropertyController.java    # API: /api/seller/properties/*
│   ├── model/
│   │   └── Property.java              # Entity dengan encapsulated behavior
│   ├── repository/
│   │   └── PropertyRepository.java    # Spring Data JPA interface
│   └── service/
│       └── PropertyService.java       # Business logic dengan ownership validation
```

---

## 🔷 Konsep OOP yang Diterapkan

### 1. **Encapsulation (Enkapsulasi)**

```java
// Property.java - Behavior encapsulated dalam entity
public void updateDetails(String newAddress, double newPrice, 
                          String newDescription, String newImageUrl) {
    // Validasi dan update dilakukan di dalam entity sendiri
    if (newAddress != null && !newAddress.isBlank()) {
        this.address = newAddress;
    }
    if (newPrice >= 0) {
        this.price = newPrice;
    }
    // ...
}
```

- Field `address`, `price`, `description`, `imageUrl` bersifat **private**
- Akses melalui getter/setter yang terkontrol
- Logic update di-encapsulate dalam method `updateDetails()`

### 2. **Composition (Komposisi)**

```
┌─────────────────────────────────────────────────────────┐
│                CatalogPropertyResponse                   │
├─────────────────────────────────────────────────────────┤
│ - propertyId: int                                       │
│ - address: String                                       │
│ - price: double                                         │
│ - description: String                                   │
│ - imageUrl: String                                      │
│ - seller: SellerInfo  ◄────────── Nested Object         │
│   ┌─────────────────────────────────────────────────┐   │
│   │              SellerInfo (Inner Class)           │   │
│   ├─────────────────────────────────────────────────┤   │
│   │ - userId: Long                                  │   │
│   │ - username: String                              │   │
│   │ - name: String                                  │   │
│   │ - email: String                                 │   │
│   │ - phoneNumber: String                           │   │
│   │ - userRole: String                              │   │
│   └─────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
```

- `CatalogPropertyResponse` memiliki **inner class** `SellerInfo`
- Menunjukkan relasi **HAS-A** antara Response dan SellerInfo

### 3. **Aggregation (Agregasi)**

```java
// Property.java - Relasi ManyToOne dengan Seller
@ManyToOne(optional = false, fetch = FetchType.LAZY)
@JoinColumn(name = "seller_id", nullable = false)
private Seller seller;

// Reviews sebagai OneToMany
@OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true)
private List<Review> reviews = new ArrayList<>();
```

- Property **memiliki referensi** ke Seller (ownership)
- Property **memiliki koleksi** Review

### 4. **Adapter Pattern**

```java
// CatalogRepository.java - Adapter untuk PropertyRepository
@Repository
public class CatalogRepository {
    private final PropertyRepository propertyRepository;  // Adaptee
    
    public List<Property> findAll() {
        return propertyRepository.findAll();  // Delegate ke PropertyRepository
    }
    
    // Custom method dengan EntityManager
    public List<Property> filterByCriteria(String keyword, Double minPrice, Double maxPrice) {
        // JPQL dinamis untuk filtering
    }
}
```

- `CatalogRepository` bertindak sebagai **Adapter**
- Menyediakan interface berbeda untuk akses data Property

### 5. **Mapper Pattern (Single Responsibility)**

```java
// CatalogMapper.java - Dedicated mapper class
@Component
public class CatalogMapper {
    public CatalogPropertyResponse toResponse(Property property) {
        // Transform Entity → DTO
        SellerInfo sellerInfo = null;
        Seller seller = property.getSeller();
        if (seller != null) {
            sellerInfo = new SellerInfo(
                seller.getUserId(),
                seller.getUsername(),
                // ...
            );
        }
        return new CatalogPropertyResponse(...);
    }
}
```

- Class khusus untuk mapping Entity ke DTO
- Memisahkan tanggung jawab transformasi data

---

## 🔎 Feature: GET ALL PROPERTIES (Catalog)

### Flow Diagram

```
┌─────────┐  GET /api/catalog/properties  ┌──────────────────┐
│ Client  │ ─────────────────────────────▶│ CatalogController│
└─────────┘  (No Auth Required)           └────────┬─────────┘
                                                   │
                                                   ▼
                                          ┌────────────────┐
                                          │ CatalogService │
                                          │showAllProperties│
                                          └────────┬───────┘
                                                   │
                                                   ▼
                                          ┌────────────────┐
                                          │CatalogRepository│
                                          │   findAll()    │
                                          └────────┬───────┘
                                                   │
                                                   ▼
                                          ┌────────────────┐
                                          │PropertyRepository│
                                          │   findAll()    │ (Delegate)
                                          └────────┬───────┘
                                                   │
                                                   ▼
                                          ┌────────────────┐
                                          │ CatalogMapper  │
                                          │  toResponse()  │
                                          └────────┬───────┘
                                                   │
                                                   ▼
                                  ┌─────────────────────────────┐
                                  │List<CatalogPropertyResponse>│
                                  └─────────────────────────────┘
```

### Class Interaction

| Layer | Class | Method | OOP Concept |
|-------|-------|--------|-------------|
| **Controller** | `CatalogController` | `getAllProperties()` | Thin Controller |
| **Service** | `CatalogService` | `showAllProperties()` | Business Logic Layer |
| **Repository** | `CatalogRepository` | `findAll()` | **Adapter Pattern** |
| **Repository** | `PropertyRepository` | `findAll()` | Interface Abstraction |
| **Mapper** | `CatalogMapper` | `toResponse()` | **Single Responsibility** |
| **DTO** | `CatalogPropertyResponse` | - | **Composition** (nested SellerInfo) |

### File Terkait

| File | Deskripsi |
|------|-----------|
| `CatalogController.java` (L23-30) | Endpoint `GET /api/catalog/properties` |
| `CatalogService.java` (L25-28) | Method `showAllProperties()` |
| `CatalogRepository.java` (L30-32) | Adapter delegating ke PropertyRepository |
| `CatalogMapper.java` | Mapping Property → CatalogPropertyResponse |

---

## 🔍 Feature: SEARCH PROPERTIES (Catalog)

### Flow Diagram

```
┌─────────┐  GET /api/catalog/properties/search  ┌──────────────────┐
│ Client  │ ────────────────────────────────────▶│ CatalogController│
└─────────┘  ?keyword=X&minPrice=Y&maxPrice=Z   └────────┬─────────┘
             (No Auth Required)                          │
                                                         ▼
                                                ┌────────────────────┐
                                                │  Build DTO from    │
                                                │  Request Params    │
                                                │ CatalogSearchRequest│
                                                └────────┬───────────┘
                                                         │
                                                         ▼
                                                ┌────────────────┐
                                                │ CatalogService │
                                                │searchProperties │
                                                └────────┬───────┘
                                                         │
                                                         ▼
                                                ┌────────────────┐
                                                │CatalogRepository│
                                                │filterByCriteria │
                                                └────────┬───────┘
                                                         │
                                                         ▼
                                               ┌──────────────────┐
                                               │ Dynamic JPQL     │
                                               │ Query Building   │
                                               │ EntityManager    │
                                               └────────┬─────────┘
                                                        │
                                                        ▼
                                                ┌────────────────┐
                                                │ CatalogMapper  │
                                                │  toResponse()  │
                                                └────────────────┘
```

### OOP Highlight: Dynamic Query Building

```java
// CatalogRepository.java - Dynamic JPQL construction
public List<Property> filterByCriteria(String keyword, Double minPrice, Double maxPrice) {
    StringBuilder jpql = new StringBuilder("SELECT p FROM Property p WHERE 1=1");
    
    // Conditional query building
    if (keyword != null && !keyword.isBlank()) {
        jpql.append(" AND (LOWER(p.address) LIKE :kw OR LOWER(p.description) LIKE :kw)");
    }
    if (minPrice != null) {
        jpql.append(" AND p.price >= :minPrice");
    }
    if (maxPrice != null) {
        jpql.append(" AND p.price <= :maxPrice");
    }
    
    TypedQuery<Property> q = em.createQuery(jpql.toString(), Property.class);
    // Set parameters conditionally...
    return q.getResultList();
}
```

### File Terkait

| File | Deskripsi |
|------|-----------|
| `CatalogController.java` (L33-50) | Endpoint dengan query params binding |
| `CatalogSearchRequest.java` | DTO dengan semua field opsional |
| `CatalogService.java` (L30-38) | Delegasi ke repository |
| `CatalogRepository.java` (L51-75) | Dynamic JPQL query builder |

---

## 📄 Feature: GET PROPERTY DETAIL (Catalog)

### Flow Diagram

```
┌─────────┐  GET /api/catalog/properties/{id}  ┌──────────────────┐
│ Client  │ ──────────────────────────────────▶│ CatalogController│
└─────────┘  (No Auth Required)                └────────┬─────────┘
                                                        │
                                                        ▼
                                               ┌────────────────┐
                                               │ CatalogService │
                                               │getPropertyDetail│
                                               └────────┬───────┘
                                                        │
                                                        ▼
                                               ┌────────────────┐
                                               │CatalogRepository│
                                               │   findById()   │
                                               └────────┬───────┘
                                                        │
                                                        ▼
                                               ┌────────────────┐
                                               │PropertyRepository│
                                               │   findById()   │
                                               └────────┬───────┘
                                                        │
                                           ┌────────────┴────────────┐
                                           │                         │
                                      Found                      Not Found
                                           │                         │
                                           ▼                         ▼
                                   ┌────────────────┐        ┌───────────┐
                                   │ CatalogMapper  │        │   null    │
                                   │  toResponse()  │        └───────────┘
                                   └────────────────┘
```

### File Terkait

| File | Deskripsi |
|------|-----------|
| `CatalogController.java` (L53-60) | Endpoint `GET /api/catalog/properties/{id}` |
| `CatalogService.java` (L40-44) | Return null jika tidak ditemukan (publik) |
| `CatalogRepository.java` (L34-36) | Delegate ke PropertyRepository.findById() |

---

## ➕ Feature: CREATE PROPERTY (Seller)

### Flow Diagram

```
┌─────────┐  POST /api/seller/properties  ┌──────────────────┐
│ Client  │ ─────────────────────────────▶│PropertyController│
└─────────┘  Header: Authorization        └────────┬─────────┘
             {address, price, description}         │
             (Auth Required: SELLER)               ▼
                                          ┌────────────────────┐
                                          │ getCurrentSeller() │
                                          │ (From JWT Context) │
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
                        └────────────────┘                  └────────────────┘
                                                                    │
                                                                    ▼
                                                           ┌────────────────┐
                                                           │PropertyRepository│
                                                           │    save()      │
                                                           └────────────────┘
```

### OOP Highlight: Ownership Assignment

```java
// PropertyService.java - Encapsulated ownership logic
public Property createProperty(Seller seller, Property data) {
    validateSeller(seller);              // Validate role
    if (data == null) {
        throw new BadRequestException("Data property tidak boleh kosong");
    }
    data.setSeller(seller);              // Establish ownership (Aggregation)
    return propertyRepository.save(data);
}
```

### File Terkait

| File | Deskripsi |
|------|-----------|
| `PropertyController.java` (L31-36) | Endpoint `POST /api/seller/properties` |
| `PropertyController.java` (L62-75) | `getCurrentSeller()` - Extract from JWT |
| `PropertyService.java` (L26-33) | Validasi dan set ownership |

---

## ✏️ Feature: UPDATE PROPERTY (Seller)

### Flow Diagram

```
┌─────────┐  PUT /api/seller/properties/{id}  ┌──────────────────┐
│ Client  │ ──────────────────────────────────▶│PropertyController│
└─────────┘  Header: Authorization             └────────┬─────────┘
             {address?, price?, description?}          │
             (Auth Required: SELLER)                   ▼
                                              ┌────────────────────┐
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
                        ┌────────────────┐                          ┌────────────────────┐
                        │ validateSeller │                          │ Verify Ownership   │
                        └────────────────┘                          │findByPropertyIdAnd │
                                                                    │       Seller       │
                                                                    └────────┬───────────┘
                                                                             │
                                                              ┌──────────────┴──────────────┐
                                                              │                             │
                                                          Owned                        Not Owned
                                                              │                             │
                                                              ▼                             ▼
                                                     ┌────────────────┐           ┌─────────────────┐
                                                     │    Property    │           │BusinessException│
                                                     │ updateDetails()│           └─────────────────┘
                                                     └────────┬───────┘
                                                              │
                                                              ▼
                                                     ┌────────────────┐
                                                     │PropertyRepository│
                                                     │    save()      │
                                                     └────────────────┘
```

### OOP Highlight: Encapsulated Update Logic

```java
// Property.java - Entity bertanggung jawab atas validasi update sendiri
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
```

### File Terkait

| File | Deskripsi |
|------|-----------|
| `PropertyController.java` (L45-52) | Endpoint `PUT /api/seller/properties/{id}` |
| `PropertyService.java` (L35-46) | Ownership verification + delegasi update |
| `Property.java` (L46-59) | **Encapsulated** `updateDetails()` method |

---

## 🗑️ Feature: DELETE PROPERTY (Seller)

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
                                                        │PropertyRepository│             │BusinessException│
                                                        │   delete()     │               └─────────────────┘
                                                        └────────┬───────┘
                                                                 │
                                                                 ▼
                                                        ┌─────────────────┐
                                                        │ CASCADE DELETE  │
                                                        │ All Reviews     │
                                                        └─────────────────┘
```

### OOP Highlight: Cascade Deletion

```java
// Property.java - Cascade relationship untuk Reviews
@OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true)
private List<Review> reviews = new ArrayList<>();
```

Ketika property dihapus, semua **Review** terkait juga akan terhapus otomatis.

### File Terkait

| File | Deskripsi |
|------|-----------|
| `PropertyController.java` (L54-59) | Endpoint `DELETE /api/seller/properties/{id}` |
| `PropertyService.java` (L48-53) | Ownership check + delete |
| `Property.java` (L41-43) | Cascade configuration |

---

## 📋 Feature: GET SELLER'S OWN PROPERTIES

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
                                         ┌────────────────┐
                                         │PropertyRepository│
                                         │ findBySeller() │
                                         └────────────────┘
```

### File Terkait

| File | Deskripsi |
|------|-----------|
| `PropertyController.java` (L38-43) | Endpoint `GET /api/seller/properties` |
| `PropertyService.java` (L61-65) | Query berdasarkan seller |
| `PropertyRepository.java` (L14) | Query method `findBySeller()` |

---

## 📊 Summary API Endpoints

### Catalog API (PUBLIC - No Auth Required)

| Feature | Method | Endpoint | Auth |
|---------|--------|----------|------|
| **Get All Properties** | GET | `/api/catalog/properties` | ❌ No |
| **Search Properties** | GET | `/api/catalog/properties/search?keyword=&minPrice=&maxPrice=` | ❌ No |
| **Get Property Detail** | GET | `/api/catalog/properties/{id}` | ❌ No |

### Property Management API (SELLER-ONLY)

| Feature | Method | Endpoint | Auth |
|---------|--------|----------|------|
| **Create Property** | POST | `/api/seller/properties` | ✅ SELLER |
| **Get Own Properties** | GET | `/api/seller/properties` | ✅ SELLER |
| **Update Property** | PUT | `/api/seller/properties/{id}` | ✅ SELLER |
| **Delete Property** | DELETE | `/api/seller/properties/{id}` | ✅ SELLER |

---

## ✅ OOP Principles Recap

| Principle | Implementation |
|-----------|----------------|
| **Encapsulation** | `Property.updateDetails()` method, private fields dengan getter/setter |
| **Composition** | `CatalogPropertyResponse` dengan inner class `SellerInfo` |
| **Aggregation** | `Property` → `Seller` (ManyToOne), `Property` → `List<Review>` (OneToMany) |
| **Adapter Pattern** | `CatalogRepository` sebagai adapter untuk `PropertyRepository` |
| **Mapper Pattern** | `CatalogMapper` dedicated class untuk Entity→DTO transformation |
| **Single Responsibility** | Separate Controller, Service, Repository, Mapper layers |
| **Dependency Injection** | Constructor injection di semua class |
| **Interface Abstraction** | `PropertyRepository extends JpaRepository<Property, Integer>` |

---

## 🔗 Relasi Antar Entity

```
┌─────────┐        ┌──────────┐        ┌────────┐
│  Seller │ 1────* │ Property │ 1────* │ Review │
└─────────┘        └──────────┘        └────────┘
     │                  │                  │
     │                  │                  │
  Pemilik           Dikelola           Diberikan
  Property          Seller             oleh Buyer
```

- **Seller** memiliki banyak **Property** (OneToMany)
- **Property** memiliki banyak **Review** (OneToMany, Cascade)
- Semua operasi modifikasi Property memerlukan **ownership verification**
