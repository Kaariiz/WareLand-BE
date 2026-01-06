# Rangkuman Fitur Review Management - WareLand Backend

Dokumen ini menjelaskan **flow lengkap** dari fitur Review dengan penekanan pada **konsep OOP (Object-Oriented Programming)** yang diterapkan.

---

## 📁 Struktur File Terkait

```
src/main/java/com/wareland/
├── review/                            # Module Review
│   ├── controller/
│   │   └── ReviewController.java      # API: /api/reviews/*
│   ├── dto/
│   │   ├── ReviewCreateRequest.java   # DTO untuk create review
│   │   ├── ReviewUpdateRequest.java   # DTO untuk update review
│   │   ├── ReviewResponse.java        # DTO response (untuk property view)
│   │   └── ReviewBuyerResponse.java   # DTO response (untuk buyer view)
│   ├── mapper/
│   │   └── ReviewMapper.java          # Mapper Entity → DTO
│   ├── model/
│   │   └── Review.java                # Entity dengan relasi ManyToOne
│   ├── repository/
│   │   └── ReviewRepository.java      # Spring Data JPA interface
│   └── service/
│       └── ReviewService.java         # Business logic dengan ownership validation
```

---

## 🔷 Konsep OOP yang Diterapkan

### 1. **Association (Asosiasi)**

```
┌─────────┐                    ┌──────────┐                    ┌──────────┐
│  Buyer  │                    │  Review  │                    │ Property │
│         │ 1 ◄──────────── * │          │ * ──────────────► 1 │          │
└─────────┘    (memberikan)    └──────────┘    (untuk)          └──────────┘
```

- **Buyer** dapat memberikan banyak **Review** (OneToMany)
- **Property** dapat memiliki banyak **Review** (OneToMany)
- **Review** adalah entitas penghubung (Association Class)

### 2. **Encapsulation (Enkapsulasi)**

```java
// Review.java - Private fields dengan controlled access
@Entity
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                // Private, no setter (ID dikelola JPA)
    
    @Min(1) @Max(5)
    private int rating;             // Validasi terintegrasi
    
    @NotBlank
    private String comment;
    
    private LocalDateTime createdAt;  // Private, no setter
    private LocalDateTime updatedAt;  // Private, no setter
    
    // Lifecycle hooks untuk timestamp
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
```

- Field `id`, `createdAt`, `updatedAt` tidak memiliki setter publik
- Timestamps dikelola otomatis via lifecycle hooks

### 3. **Unique Constraint (Business Rule in OOP)**

```java
// Review.java - Database-level constraint
@Entity
@Table(
    name = "reviews",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_review_buyer_property",
            columnNames = {"buyer_id", "property_id"}
        )
    }
)
public class Review { ... }
```

- **Business rule**: 1 Buyer hanya bisa memberikan 1 Review per Property
- Constraint diimplementasikan di level entity dan di-enforce di database

### 4. **Mapper Pattern (Single Responsibility)**

```java
// ReviewMapper.java - Mapper dengan multiple response types
@Component
public class ReviewMapper {
    
    // Mapper untuk tampilan di halaman Property
    public ReviewResponse toResponse(Review review) {
        String buyerName = review.getBuyer() != null 
            ? review.getBuyer().getName() : null;
        return new ReviewResponse(
            review.getId(),
            review.getRating(),
            review.getComment(),
            buyerName,
            review.getCreatedAt()
        );
    }
    
    // Mapper untuk tampilan di halaman Buyer (My Reviews)
    public ReviewBuyerResponse toBuyerResponse(Review review) {
        Long propertyId = review.getProperty() != null 
            ? review.getProperty().getPropertyId().longValue() : null;
        String propertyTitle = review.getProperty() != null 
            ? review.getProperty().getAddress() : null;
        return new ReviewBuyerResponse(
            review.getId(),
            propertyId,
            propertyTitle,
            review.getRating(),
            review.getComment(),
            review.getCreatedAt()
        );
    }
}
```

- Satu entity `Review` dapat di-map ke berbagai DTO tergantung konteks
- **Single Responsibility**: Mapper hanya bertugas transformasi data

### 5. **Polymorphism via Type Checking**

```java
// ReviewService.java - Type checking untuk validasi role
User user = userRepository.findById(request.getBuyerId()).orElseThrow(...);

// Polymorphic check: apakah user adalah Buyer?
if (user.getUserRole() != UserRole.BUYER || !(user instanceof Buyer)) {
    throw new BusinessException("Hanya buyer yang dapat membuat review");
}

// Downcasting setelah validasi
Buyer buyer = (Buyer) user;
```

- Menggunakan `instanceof` untuk type checking
- Downcasting dari `User` ke `Buyer` setelah validasi

---

## ➕ Feature: CREATE REVIEW

### Flow Diagram

```
┌─────────┐    POST /api/reviews    ┌──────────────────┐
│ Client  │ ───────────────────────▶│ ReviewController │
└─────────┘  {buyerId, propertyId,  └────────┬─────────┘
              rating, comment}               │
             (Auth Required)                 ▼
                                    ┌────────────────┐
                                    │ ReviewService  │
                                    │  createReview  │
                                    └────────┬───────┘
                                             │
       ┌─────────────────────────────────────┼─────────────────────────────────────┐
       │                                     │                                     │
       ▼                                     ▼                                     ▼
┌──────────────┐                    ┌────────────────┐                    ┌────────────────┐
│ Validate     │                    │ UserRepository │                    │PropertyRepository│
│ Rating 1-5   │                    │ findById()     │                    │ findById()     │
│ Comment      │                    └────────┬───────┘                    └────────────────┘
└──────────────┘                             │
                                             ▼
                                    ┌────────────────┐
                                    │ Type Check     │
                                    │ user instanceof│
                                    │ Buyer?         │
                                    └────────┬───────┘
                                             │
                                             ▼
                                    ┌────────────────────┐
                                    │ ReviewRepository   │
                                    │existsByBuyerAnd    │
                                    │   Property?        │
                                    └────────┬───────────┘
                                             │
                              ┌──────────────┴──────────────┐
                              │                             │
                          Not Exists                     Exists
                              │                             │
                              ▼                             ▼
                     ┌────────────────┐           ┌─────────────────┐
                     │ Create Review  │           │ BusinessException│
                     │ Set Buyer      │           │ "Sudah review"  │
                     │ Set Property   │           └─────────────────┘
                     └────────┬───────┘
                              │
                              ▼
                     ┌────────────────┐
                     │ ReviewRepository│
                     │    save()      │
                     └────────┬───────┘
                              │
                              ▼
                     ┌────────────────┐
                     │ ReviewMapper   │
                     │  toResponse()  │
                     └────────────────┘
```

### Class Interaction

| Layer | Class | Method | OOP Concept |
|-------|-------|--------|-------------|
| **Controller** | `ReviewController` | `create()` | Thin Controller |
| **Service** | `ReviewService` | `createReview()` | Business Logic + Validation |
| **Repository** | `ReviewRepository` | `existsByBuyerUserIdAndPropertyPropertyId()` | Uniqueness Check |
| **Mapper** | `ReviewMapper` | `toResponse()` | Single Responsibility |
| **DTO** | `ReviewCreateRequest` | - | Encapsulated request data |

### Validasi yang Dilakukan

| Validasi | Level | Message |
|----------|-------|---------|
| Rating 1-5 | Service | "Rating harus antara 1 hingga 5" |
| Comment not blank | Service | "Comment tidak boleh kosong" |
| Buyer exists | Service | "Buyer dengan ID X tidak ditemukan" |
| User is Buyer | Service | "Hanya buyer yang dapat membuat review" |
| Property exists | Service | "Property dengan ID X tidak ditemukan" |
| Review unique | Service | "Anda sudah memberikan review untuk properti ini" |

### File Terkait

| File | Deskripsi |
|------|-----------|
| `ReviewController.java` (L26-34) | Endpoint `POST /api/reviews` |
| `ReviewService.java` (L46-87) | Method `createReview()` dengan validasi lengkap |
| `ReviewCreateRequest.java` | DTO dengan validasi `@NotNull`, `@Min`, `@Max`, `@NotBlank` |
| `Review.java` (L15-20) | Unique constraint definition |

---

## 📋 Feature: GET REVIEWS BY PROPERTY

### Flow Diagram

```
┌─────────┐  GET /api/reviews/property/{id}  ┌──────────────────┐
│ Client  │ ─────────────────────────────────▶│ ReviewController │
└─────────┘  (Auth Required)                 └────────┬─────────┘
                                                      │
                                                      ▼
                                             ┌────────────────┐
                                             │ ReviewService  │
                                             │getReviewsBy    │
                                             │    Property    │
                                             └────────┬───────┘
                                                      │
                                                      ▼
                                             ┌────────────────────┐
                                             │   ReviewRepository │
                                             │findAllByPropertyId │
                                             │ OrderByCreatedAtDesc│
                                             └────────┬───────────┘
                                                      │
                                                      ▼
                                             ┌────────────────┐
                                             │ ReviewMapper   │
                                             │  toResponse()  │
                                             │   (stream)     │
                                             └────────────────┘
                                                      │
                                                      ▼
                                            ┌──────────────────┐
                                            │List<ReviewResponse>│
                                            │ - reviewId       │
                                            │ - rating         │
                                            │ - comment        │
                                            │ - buyerName      │
                                            │ - createdAt      │
                                            └──────────────────┘
```

### OOP Highlight: Stream Processing dengan Mapper

```java
// ReviewService.java - Functional style processing
public List<ReviewResponse> getReviewsByProperty(Long propertyId) {
    List<Review> reviews = reviewRepository
        .findAllByPropertyPropertyIdOrderByCreatedAtDesc(pid);
    
    return reviews.stream()
            .map(reviewMapper::toResponse)  // Method reference
            .collect(Collectors.toList());
}
```

### File Terkait

| File | Deskripsi |
|------|-----------|
| `ReviewController.java` (L37-43) | Endpoint `GET /api/reviews/property/{id}` |
| `ReviewService.java` (L90-99) | Method dengan Stream API |
| `ReviewRepository.java` (L14) | Query method dengan sorting |
| `ReviewResponse.java` | DTO untuk tampilan di halaman property |

---

## 👤 Feature: GET REVIEWS BY BUYER (My Reviews)

### Flow Diagram

```
┌─────────┐  GET /api/reviews/buyer/{id}  ┌──────────────────┐
│ Client  │ ──────────────────────────────▶│ ReviewController │
└─────────┘  (Auth Required)              └────────┬─────────┘
                                                   │
                                                   ▼
                                          ┌────────────────┐
                                          │ ReviewService  │
                                          │getReviewsBy    │
                                          │    Buyer       │
                                          └────────┬───────┘
                                                   │
                                 ┌─────────────────┴─────────────────┐
                                 │                                   │
                                 ▼                                   ▼
                        ┌────────────────┐                  ┌────────────────────┐
                        │ UserRepository │                  │   ReviewRepository │
                        │  findById()    │                  │ findAllByBuyerId   │
                        └────────┬───────┘                  │ OrderByCreatedAt   │
                                 │                          └────────────────────┘
                                 ▼
                        ┌────────────────┐
                        │ Validate User  │
                        │ is Buyer       │
                        └────────────────┘
                                 │
                                 ▼
                        ┌────────────────┐
                        │ ReviewMapper   │
                        │toBuyerResponse │
                        │   (stream)     │
                        └────────────────┘
                                 │
                                 ▼
                       ┌────────────────────┐
                       │List<ReviewBuyer    │
                       │       Response>    │
                       │ - reviewId         │
                       │ - propertyId       │
                       │ - propertyTitle    │
                       │ - rating           │
                       │ - comment          │
                       │ - createdAt        │
                       └────────────────────┘
```

### OOP Highlight: Different DTO for Different Context

```java
// ReviewMapper.java - Context-aware mapping
public ReviewBuyerResponse toBuyerResponse(Review review) {
    // Untuk Buyer view, fokus pada Property info (bukan Buyer info)
    Long propertyId = review.getProperty().getPropertyId().longValue();
    String propertyTitle = review.getProperty().getAddress();
    
    return new ReviewBuyerResponse(
        review.getId(),
        propertyId,            // Property context
        propertyTitle,         // Property context
        review.getRating(),
        review.getComment(),
        review.getCreatedAt()
    );
}
```

| DTO | Konteks Penggunaan | Fokus Informasi |
|-----|-------------------|-----------------|
| `ReviewResponse` | Halaman Property | Buyer name, rating, comment |
| `ReviewBuyerResponse` | Halaman My Reviews | Property info, rating, comment |

### File Terkait

| File | Deskripsi |
|------|-----------|
| `ReviewController.java` (L46-50) | Endpoint `GET /api/reviews/buyer/{id}` |
| `ReviewService.java` (L101-114) | Method dengan Buyer validation |
| `ReviewBuyerResponse.java` | DTO dengan property context |

---

## ✏️ Feature: UPDATE REVIEW

### Flow Diagram

```
┌─────────┐  PUT /api/reviews/{id}?buyerId=X  ┌──────────────────┐
│ Client  │ ──────────────────────────────────▶│ ReviewController │
└─────────┘  {rating, comment}                 └────────┬─────────┘
             (Auth Required)                            │
                                                        ▼
                                               ┌────────────────┐
                                               │ ReviewService  │
                                               │  updateReview  │
                                               └────────┬───────┘
                                                        │
                                                        ▼
                                               ┌────────────────┐
                                               │ReviewRepository│
                                               │   findById()   │
                                               └────────┬───────┘
                                                        │
                                                        ▼
                                               ┌────────────────────┐
                                               │ Ownership Check    │
                                               │review.getBuyer()   │
                                               │.getUserId() ==     │
                                               │     buyerId?       │
                                               └────────┬───────────┘
                                                        │
                                      ┌─────────────────┴─────────────────┐
                                      │                                   │
                                  Owner                              Not Owner
                                      │                                   │
                                      ▼                                   ▼
                             ┌────────────────┐               ┌─────────────────┐
                             │ Validate Input │               │ BusinessException│
                             │ Rating 1-5     │               │ "Tidak berhak"  │
                             │ Comment valid  │               └─────────────────┘
                             └────────┬───────┘
                                      │
                                      ▼
                             ┌────────────────┐
                             │ Update Entity  │
                             │ review.setRating│
                             │ review.setComment│
                             └────────┬───────┘
                                      │
                                      ▼
                             ┌────────────────┐
                             │ReviewRepository│
                             │    save()      │
                             └────────────────┘
```

### OOP Highlight: Ownership Verification

```java
// ReviewService.java - Ownership check
public ReviewResponse updateReview(Long reviewId, Long buyerId, ReviewUpdateRequest request) {
    Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ResourceNotFoundException("Review tidak ditemukan"));
    
    // OOP: Access buyer via encapsulated getter
    if (!review.getBuyer().getUserId().equals(buyerId)) {
        throw new BusinessException("Anda tidak berhak mengubah review ini");
    }
    
    // Direct setter access (controlled)
    review.setRating(request.getRating());
    review.setComment(request.getComment());
    // updatedAt dikelola otomatis via @PreUpdate
    
    return reviewMapper.toResponse(reviewRepository.save(review));
}
```

### File Terkait

| File | Deskripsi |
|------|-----------|
| `ReviewController.java` (L53-63) | Endpoint `PUT /api/reviews/{id}?buyerId=X` |
| `ReviewService.java` (L117-138) | Method dengan ownership validation |
| `ReviewUpdateRequest.java` | DTO dengan rating dan comment only |
| `Review.java` (L56-59) | `@PreUpdate` lifecycle hook |

---

## 🗑️ Feature: DELETE REVIEW

### Flow Diagram

```
┌─────────┐  DELETE /api/reviews/{id}?buyerId=X  ┌──────────────────┐
│ Client  │ ─────────────────────────────────────▶│ ReviewController │
└─────────┘  (Auth Required)                     └────────┬─────────┘
                                                          │
                                                          ▼
                                                 ┌────────────────┐
                                                 │ ReviewService  │
                                                 │  deleteReview  │
                                                 └────────┬───────┘
                                                          │
                                                          ▼
                                                 ┌────────────────┐
                                                 │ReviewRepository│
                                                 │   findById()   │
                                                 └────────┬───────┘
                                                          │
                                                          ▼
                                                 ┌────────────────────┐
                                                 │ Ownership Check    │
                                                 │review.getBuyer()   │
                                                 │.getUserId() ==     │
                                                 │     buyerId?       │
                                                 └────────┬───────────┘
                                                          │
                                        ┌─────────────────┴─────────────────┐
                                        │                                   │
                                    Owner                              Not Owner
                                        │                                   │
                                        ▼                                   ▼
                               ┌────────────────┐               ┌─────────────────┐
                               │ReviewRepository│               │ BusinessException│
                               │   delete()     │               │ "Tidak berhak"  │
                               └────────────────┘               └─────────────────┘
```

### File Terkait

| File | Deskripsi |
|------|-----------|
| `ReviewController.java` (L66-75) | Endpoint `DELETE /api/reviews/{id}?buyerId=X` |
| `ReviewService.java` (L141-151) | Method dengan ownership validation |

---

## 📊 Summary API Endpoints

| Feature | Method | Endpoint | Auth | Params |
|---------|--------|----------|------|--------|
| **Create Review** | POST | `/api/reviews` | ✅ Yes | Body: buyerId, propertyId, rating, comment |
| **Get by Property** | GET | `/api/reviews/property/{propertyId}` | ✅ Yes | - |
| **Get by Buyer** | GET | `/api/reviews/buyer/{buyerId}` | ✅ Yes | - |
| **Update Review** | PUT | `/api/reviews/{reviewId}` | ✅ Yes | Query: buyerId, Body: rating, comment |
| **Delete Review** | DELETE | `/api/reviews/{reviewId}` | ✅ Yes | Query: buyerId |

---

## 🔗 Relasi Antar Entity

```
┌─────────┐         ┌──────────┐         ┌──────────┐
│  Buyer  │ 1─────* │  Review  │ *─────1 │ Property │
└─────────┘         └──────────┘         └──────────┘
     │                   │                    │
     │                   │                    │
  Pemberi           Penghubung            Objek
  Review            (Association)         Review
     
CONSTRAINT: 1 Buyer hanya bisa memberikan 1 Review per Property
(UniqueConstraint pada kombinasi buyer_id + property_id)
```

---

## ✅ OOP Principles Recap

| Principle | Implementation |
|-----------|----------------|
| **Encapsulation** | Private fields, no public setter untuk `id`, `createdAt`, `updatedAt` |
| **Association** | Review menghubungkan Buyer (ManyToOne) dan Property (ManyToOne) |
| **Lifecycle Hooks** | `@PrePersist`, `@PreUpdate` untuk auto-manage timestamps |
| **Unique Constraint** | Business rule "1 review per buyer per property" via `@UniqueConstraint` |
| **Mapper Pattern** | `ReviewMapper` dengan multiple mapping methods untuk context berbeda |
| **Polymorphism** | `instanceof` check untuk validasi User → Buyer |
| **Single Responsibility** | Separate Controller, Service, Repository, Mapper layers |
| **Dependency Injection** | Constructor injection di semua class |
| **Functional Programming** | Stream API dengan method reference untuk mapping collections |

---

## 🔐 Security Considerations

| Aspek | Implementasi |
|-------|--------------|
| **Ownership Verification** | Update/Delete hanya oleh Buyer yang membuat review |
| **Role Validation** | Hanya user dengan role BUYER yang dapat membuat review |
| **Duplicate Prevention** | Database constraint mencegah duplicate review |
| **Input Validation** | Rating (1-5), Comment (not blank) divalidasi di service layer |
