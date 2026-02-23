# Database Entity Design Document
## Spring Boot + JPA/Hibernate

**Project:** E-Commerce Platform  
**Version:** 1.0.0  
**Last Updated:** 2026-02-23

---

## Table of Contents

1. [Overview](#overview)
2. [Tech Stack & Dependencies](#tech-stack--dependencies)
3. [Entity Relationship Summary](#entity-relationship-summary)
4. [Base Entity](#base-entity)
5. [User Management](#user-management)
    - [Users](#users)
    - [Roles](#roles)
    - [Addresses](#addresses)
6. [Product Catalog](#product-catalog)
    - [Products](#products)
    - [Categories](#categories)
    - [ProductImages](#productimages)
7. [Shopping Cart](#shopping-cart)
    - [Cart](#cart)
    - [CartItems](#cartitems)
8. [Orders](#orders)
    - [Orders](#orders-1)
    - [OrderItems](#orderitems)
9. [Reviews & Payments](#reviews--payments)
    - [Reviews](#reviews)
    - [Payments](#payments)
10. [Enum Definitions](#enum-definitions)
11. [Database Configuration](#database-configuration)
12. [Naming Conventions](#naming-conventions)
13. [Indexing Strategy](#indexing-strategy)

---

## Overview

This document defines all JPA entity classes for the e-commerce platform. Each entity maps to a relational database table managed via Hibernate. All entities follow a consistent structure using a shared `BaseEntity` for audit fields.

**Design Principles:**
- All entities extend `BaseEntity` for consistent audit trails (`createdAt`, `updatedAt`)
- Soft deletes are used where data retention is critical (Orders, Users)
- Fetch type defaults to `LAZY` unless otherwise noted
- Cascade types are explicitly defined per relationship
- All enums are stored as `STRING` in the DB for readability

---

## Tech Stack & Dependencies

```xml
<!-- pom.xml -->
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
    <dependency>
        <groupId>org.hibernate.validator</groupId>
        <artifactId>hibernate-validator</artifactId>
    </dependency>
</dependencies>
```

---

## Entity Relationship Summary

```
Users ──< Addresses
Users >──< Roles  (many-to-many via user_roles)
Users ──< Reviews
Users ──< Orders
Users ──── Cart

Products >── Categories
Products ──< ProductImages
Products ──< CartItems
Products ──< OrderItems
Products ──< Reviews

Cart ──< CartItems
Orders ──< OrderItems
Orders ──── Payments
```

---

## Base Entity

All entities inherit from `BaseEntity`. It provides common audit fields automatically managed by JPA.

```java
// package: com.ecommerce.entity.common

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

**Enable JPA Auditing in main class:**

```java
@SpringBootApplication
@EnableJpaAuditing
public class EcommerceApplication { ... }
```

---

## User Management

### Users

**Table:** `users`

Stores authenticated user account data. Soft delete is enabled via `isActive` flag.

```java
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Column(name = "first_name", nullable = false, length = 100)
    @NotBlank
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    @NotBlank
    private String lastName;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    @Email
    @NotBlank
    private String email;

    @Column(name = "password_hash", nullable = false)
    @NotBlank
    private String passwordHash;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "is_email_verified", nullable = false)
    @Builder.Default
    private Boolean isEmailVerified = false;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    // Relationships
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Address> addresses = new ArrayList<>();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Cart cart;

    @OneToMany(mappedBy = "user", cascade = CascadeType.PERSIST)
    private List<Order> orders = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.PERSIST)
    private List<Review> reviews = new ArrayList<>();
}
```

**Field Reference:**

| Field | Type | Constraints | Notes |
|---|---|---|---|
| `id` | `Long` | PK, auto-generated | Inherited from BaseEntity |
| `firstName` | `String` | NOT NULL, max 100 | — |
| `lastName` | `String` | NOT NULL, max 100 | — |
| `email` | `String` | NOT NULL, UNIQUE, max 150 | Used as login identifier |
| `passwordHash` | `String` | NOT NULL | BCrypt encoded |
| `phoneNumber` | `String` | nullable, max 20 | — |
| `status` | `UserStatus` | NOT NULL, default ACTIVE | Enum as STRING |
| `isEmailVerified` | `Boolean` | NOT NULL, default false | — |
| `lastLoginAt` | `LocalDateTime` | nullable | Updated on successful login |
| `createdAt` | `LocalDateTime` | NOT NULL, auto | Inherited |
| `updatedAt` | `LocalDateTime` | nullable, auto | Inherited |

---

### Roles

**Table:** `roles`

Defines system-level roles for authorization (e.g., ROLE_USER, ROLE_ADMIN).

```java
@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false, unique = true, length = 50)
    private RoleName name;

    @Column(name = "description", length = 255)
    private String description;

    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<User> users = new HashSet<>();
}
```

**Field Reference:**

| Field | Type | Constraints | Notes |
|---|---|---|---|
| `id` | `Long` | PK | Inherited |
| `name` | `RoleName` | NOT NULL, UNIQUE | Enum: ROLE_USER, ROLE_ADMIN, ROLE_SELLER |
| `description` | `String` | nullable, max 255 | Human-readable description |

**Join Table:** `user_roles`

| Column | Type | Constraints |
|---|---|---|
| `user_id` | `Long` | FK → users.id |
| `role_id` | `Long` | FK → roles.id |

---

### Addresses

**Table:** `addresses`

Stores shipping/billing addresses linked to a user. A user can have multiple addresses.

```java
@Entity
@Table(name = "addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Column(name = "address_line1", nullable = false, length = 255)
    @NotBlank
    private String addressLine1;

    @Column(name = "address_line2", length = 255)
    private String addressLine2;

    @Column(name = "city", nullable = false, length = 100)
    @NotBlank
    private String city;

    @Column(name = "state", nullable = false, length = 100)
    @NotBlank
    private String state;

    @Column(name = "country", nullable = false, length = 100)
    @NotBlank
    private String country;

    @Column(name = "postal_code", nullable = false, length = 20)
    @NotBlank
    private String postalCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "address_type", nullable = false, length = 20)
    private AddressType addressType;

    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private Boolean isDefault = false;
}
```

**Field Reference:**

| Field | Type | Constraints | Notes |
|---|---|---|---|
| `id` | `Long` | PK | Inherited |
| `user` | `User` | FK, NOT NULL | Many-to-one |
| `addressLine1` | `String` | NOT NULL, max 255 | Street address |
| `addressLine2` | `String` | nullable, max 255 | Apt, Suite, etc. |
| `city` | `String` | NOT NULL, max 100 | — |
| `state` | `String` | NOT NULL, max 100 | — |
| `country` | `String` | NOT NULL, max 100 | — |
| `postalCode` | `String` | NOT NULL, max 20 | — |
| `addressType` | `AddressType` | NOT NULL | Enum: SHIPPING, BILLING, BOTH |
| `isDefault` | `Boolean` | NOT NULL, default false | User's primary address |

---

## Product Catalog

### Products

**Table:** `products`

Core product entity. Each product belongs to a category and can have multiple images.

```java
@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product extends BaseEntity {

    @Column(name = "name", nullable = false, length = 255)
    @NotBlank
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "sku", nullable = false, unique = true, length = 100)
    @NotBlank
    private String sku;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    @DecimalMin("0.00")
    private BigDecimal price;

    @Column(name = "discount_price", precision = 10, scale = 2)
    private BigDecimal discountPrice;

    @Column(name = "stock_quantity", nullable = false)
    @Min(0)
    private Integer stockQuantity;

    @Column(name = "weight_kg", precision = 6, scale = 3)
    private BigDecimal weightKg;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ProductStatus status = ProductStatus.ACTIVE;

    @Column(name = "average_rating", precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Column(name = "review_count", nullable = false)
    @Builder.Default
    private Integer reviewCount = 0;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.PERSIST)
    private List<Review> reviews = new ArrayList<>();
}
```

**Field Reference:**

| Field | Type | Constraints | Notes |
|---|---|---|---|
| `id` | `Long` | PK | Inherited |
| `name` | `String` | NOT NULL, max 255 | — |
| `description` | `String` | nullable, TEXT | Long-form product description |
| `sku` | `String` | NOT NULL, UNIQUE, max 100 | Stock Keeping Unit |
| `price` | `BigDecimal` | NOT NULL, ≥ 0.00 | Base price |
| `discountPrice` | `BigDecimal` | nullable | Sale/discounted price |
| `stockQuantity` | `Integer` | NOT NULL, ≥ 0 | Inventory count |
| `weightKg` | `BigDecimal` | nullable | For shipping calculations |
| `status` | `ProductStatus` | NOT NULL, default ACTIVE | Enum: ACTIVE, INACTIVE, ARCHIVED |
| `averageRating` | `BigDecimal` | default 0.00 | Computed from reviews |
| `reviewCount` | `Integer` | NOT NULL, default 0 | Denormalized count |
| `category` | `Category` | FK, NOT NULL | Many-to-one |

---

### Categories

**Table:** `categories`

Supports a self-referential parent-child hierarchy for nested categories.

```java
@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category extends BaseEntity {

    @Column(name = "name", nullable = false, unique = true, length = 100)
    @NotBlank
    private String name;

    @Column(name = "slug", nullable = false, unique = true, length = 120)
    @NotBlank
    private String slug;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    // Self-referencing for nested categories
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @JsonIgnore
    private Category parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.PERSIST)
    private List<Category> children = new ArrayList<>();

    @OneToMany(mappedBy = "category", cascade = CascadeType.PERSIST)
    private List<Product> products = new ArrayList<>();
}
```

**Field Reference:**

| Field | Type | Constraints | Notes |
|---|---|---|---|
| `id` | `Long` | PK | Inherited |
| `name` | `String` | NOT NULL, UNIQUE, max 100 | Display name |
| `slug` | `String` | NOT NULL, UNIQUE, max 120 | URL-friendly identifier |
| `description` | `String` | nullable, max 500 | — |
| `imageUrl` | `String` | nullable, max 500 | Category banner/icon |
| `isActive` | `Boolean` | NOT NULL, default true | — |
| `parent` | `Category` | FK, nullable | Self-reference for hierarchy |

---

### ProductImages

**Table:** `product_images`

Stores image URLs associated with a product. One product can have multiple images; one is flagged as primary.

```java
@Entity
@Table(name = "product_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnore
    private Product product;

    @Column(name = "image_url", nullable = false, length = 1000)
    @NotBlank
    private String imageUrl;

    @Column(name = "alt_text", length = 255)
    private String altText;

    @Column(name = "is_primary", nullable = false)
    @Builder.Default
    private Boolean isPrimary = false;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;
}
```

**Field Reference:**

| Field | Type | Constraints | Notes |
|---|---|---|---|
| `id` | `Long` | PK | Inherited |
| `product` | `Product` | FK, NOT NULL | Parent product |
| `imageUrl` | `String` | NOT NULL, max 1000 | CDN/S3 URL |
| `altText` | `String` | nullable, max 255 | Accessibility text |
| `isPrimary` | `Boolean` | NOT NULL, default false | Thumbnail image |
| `sortOrder` | `Integer` | NOT NULL, default 0 | Display order |

---

## Shopping Cart

### Cart

**Table:** `carts`

One cart per user. Cart is created automatically on user registration.

```java
@Entity
@Table(name = "carts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @JsonIgnore
    private User user;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();

    // Computed field — not stored in DB
    @Transient
    public BigDecimal getTotalAmount() {
        return items.stream()
            .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
```

**Field Reference:**

| Field | Type | Constraints | Notes |
|---|---|---|---|
| `id` | `Long` | PK | Inherited |
| `user` | `User` | FK, NOT NULL, UNIQUE | One-to-one |
| `totalAmount` | `BigDecimal` | Transient | Computed at runtime |

---

### CartItems

**Table:** `cart_items`

Each row represents a product added to a cart with quantity and price snapshot.

```java
@Entity
@Table(name = "cart_items",
    uniqueConstraints = @UniqueConstraint(columnNames = {"cart_id", "product_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cart_id", nullable = false)
    @JsonIgnore
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity", nullable = false)
    @Min(1)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;
}
```

**Field Reference:**

| Field | Type | Constraints | Notes |
|---|---|---|---|
| `id` | `Long` | PK | Inherited |
| `cart` | `Cart` | FK, NOT NULL | Parent cart |
| `product` | `Product` | FK, NOT NULL | — |
| `quantity` | `Integer` | NOT NULL, ≥ 1 | — |
| `unitPrice` | `BigDecimal` | NOT NULL | Snapshot of price at add time |

> **Note:** `(cart_id, product_id)` has a composite unique constraint — same product can't be added twice. Increment quantity instead.

---

## Orders

### Orders

**Table:** `orders`

Represents a placed order. Immutable after creation except for status transitions.

```java
@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Column(name = "order_number", nullable = false, unique = true, length = 50)
    @NotBlank
    private String orderNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "discount_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "shipping_amount", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal shippingAmount = BigDecimal.ZERO;

    @Column(name = "tax_amount", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "shipping_address", nullable = false, columnDefinition = "TEXT")
    private String shippingAddress;  // JSON snapshot of address at order time

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "placed_at", nullable = false)
    private LocalDateTime placedAt;

    // Relationships
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
    private Payment payment;
}
```

**Field Reference:**

| Field | Type | Constraints | Notes |
|---|---|---|---|
| `id` | `Long` | PK | Inherited |
| `user` | `User` | FK, NOT NULL | — |
| `orderNumber` | `String` | NOT NULL, UNIQUE | e.g. `ORD-20260223-0001` |
| `status` | `OrderStatus` | NOT NULL | Enum: PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED, REFUNDED |
| `subtotal` | `BigDecimal` | NOT NULL | Before tax/shipping/discount |
| `discountAmount` | `BigDecimal` | NOT NULL, default 0 | — |
| `shippingAmount` | `BigDecimal` | NOT NULL, default 0 | — |
| `taxAmount` | `BigDecimal` | NOT NULL, default 0 | — |
| `totalAmount` | `BigDecimal` | NOT NULL | Final charged amount |
| `shippingAddress` | `String` | NOT NULL, TEXT | JSON snapshot |
| `notes` | `String` | nullable, max 500 | Customer notes |
| `placedAt` | `LocalDateTime` | NOT NULL | When order was submitted |

---

### OrderItems

**Table:** `order_items`

Snapshot of each product in the order at time of purchase. Prices are fixed at order time.

```java
@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnore
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "product_name_snapshot", nullable = false, length = 255)
    private String productNameSnapshot;

    @Column(name = "product_sku_snapshot", nullable = false, length = 100)
    private String productSkuSnapshot;

    @Column(name = "quantity", nullable = false)
    @Min(1)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;
}
```

**Field Reference:**

| Field | Type | Constraints | Notes |
|---|---|---|---|
| `id` | `Long` | PK | Inherited |
| `order` | `Order` | FK, NOT NULL | Parent order |
| `product` | `Product` | FK, NOT NULL | Product reference (soft FK) |
| `productNameSnapshot` | `String` | NOT NULL, max 255 | Name at purchase time |
| `productSkuSnapshot` | `String` | NOT NULL, max 100 | SKU at purchase time |
| `quantity` | `Integer` | NOT NULL, ≥ 1 | — |
| `unitPrice` | `BigDecimal` | NOT NULL | Price at purchase time |
| `totalPrice` | `BigDecimal` | NOT NULL | `unitPrice × quantity` |

> **Note:** Product name and SKU are snapshotted to preserve historical accuracy even if the product is modified later.

---

## Reviews & Payments

### Reviews

**Table:** `reviews`

Stores product reviews submitted by verified buyers. One review per user per product enforced via unique constraint.

```java
@Entity
@Table(name = "reviews",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "product_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "rating", nullable = false)
    @Min(1) @Max(5)
    private Integer rating;

    @Column(name = "title", length = 150)
    private String title;

    @Column(name = "body", columnDefinition = "TEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ReviewStatus status = ReviewStatus.PENDING;

    @Column(name = "is_verified_purchase", nullable = false)
    @Builder.Default
    private Boolean isVerifiedPurchase = false;

    @Column(name = "helpful_votes", nullable = false)
    @Builder.Default
    private Integer helpfulVotes = 0;
}
```

**Field Reference:**

| Field | Type | Constraints | Notes |
|---|---|---|---|
| `id` | `Long` | PK | Inherited |
| `user` | `User` | FK, NOT NULL | Reviewer |
| `product` | `Product` | FK, NOT NULL | — |
| `rating` | `Integer` | NOT NULL, 1–5 | Star rating |
| `title` | `String` | nullable, max 150 | Review headline |
| `body` | `String` | nullable, TEXT | Full review text |
| `status` | `ReviewStatus` | NOT NULL, default PENDING | Enum: PENDING, APPROVED, REJECTED |
| `isVerifiedPurchase` | `Boolean` | NOT NULL, default false | Set true if user actually ordered this product |
| `helpfulVotes` | `Integer` | NOT NULL, default 0 | Community upvotes |

---

### Payments

**Table:** `payments`

Records payment details tied to a single order. One payment per order.

```java
@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    @JsonIgnore
    private Order order;

    @Column(name = "transaction_id", nullable = false, unique = true, length = 255)
    @NotBlank
    private String transactionId;

    @Column(name = "gateway", nullable = false, length = 50)
    @NotBlank
    private String gateway;  // e.g. "STRIPE", "RAZORPAY", "PAYPAL"

    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false, length = 30)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.INITIATED;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 10)
    @Builder.Default
    private String currency = "INR";

    @Column(name = "gateway_response", columnDefinition = "TEXT")
    private String gatewayResponse;  // Raw JSON response from gateway

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    @Column(name = "refund_amount", precision = 10, scale = 2)
    private BigDecimal refundAmount;
}
```

**Field Reference:**

| Field | Type | Constraints | Notes |
|---|---|---|---|
| `id` | `Long` | PK | Inherited |
| `order` | `Order` | FK, NOT NULL, UNIQUE | One-to-one |
| `transactionId` | `String` | NOT NULL, UNIQUE | Gateway transaction ID |
| `gateway` | `String` | NOT NULL, max 50 | Payment processor name |
| `method` | `PaymentMethod` | NOT NULL | Enum: CREDIT_CARD, DEBIT_CARD, UPI, NET_BANKING, WALLET, COD |
| `status` | `PaymentStatus` | NOT NULL | Enum: INITIATED, SUCCESS, FAILED, REFUNDED, PARTIALLY_REFUNDED |
| `amount` | `BigDecimal` | NOT NULL | Must match order totalAmount |
| `currency` | `String` | NOT NULL, default "INR" | ISO 4217 currency code |
| `gatewayResponse` | `String` | nullable, TEXT | Raw JSON from gateway |
| `paidAt` | `LocalDateTime` | nullable | Populated on success |
| `refundedAt` | `LocalDateTime` | nullable | Populated on refund |
| `refundAmount` | `BigDecimal` | nullable | Partial or full refund |

---

## Enum Definitions

```java
// UserStatus.java
public enum UserStatus { ACTIVE, INACTIVE, BANNED, DELETED }

// RoleName.java
public enum RoleName { ROLE_USER, ROLE_ADMIN, ROLE_SELLER }

// AddressType.java
public enum AddressType { SHIPPING, BILLING, BOTH }

// ProductStatus.java
public enum ProductStatus { ACTIVE, INACTIVE, ARCHIVED }

// OrderStatus.java
public enum OrderStatus {
    PENDING, CONFIRMED, PROCESSING,
    SHIPPED, OUT_FOR_DELIVERY, DELIVERED,
    CANCELLED, RETURN_REQUESTED, RETURNED, REFUNDED
}

// ReviewStatus.java
public enum ReviewStatus { PENDING, APPROVED, REJECTED }

// PaymentMethod.java
public enum PaymentMethod {
    CREDIT_CARD, DEBIT_CARD, UPI, NET_BANKING, WALLET, COD
}

// PaymentStatus.java
public enum PaymentStatus {
    INITIATED, PENDING, SUCCESS, FAILED,
    REFUNDED, PARTIALLY_REFUNDED, CANCELLED
}
```

---

## Database Configuration

```yaml
# application.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/ecommerce_db
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    driver-class-name: org.postgresql.Driver

  jpa:
    hibernate:
      ddl-auto: validate          # Use 'create-drop' for dev, 'validate' for prod
    show-sql: false               # Set true for debugging only
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
        default_batch_fetch_size: 20
        order_inserts: true
        order_updates: true
        jdbc:
          batch_size: 25
```

> **DDL Strategy:** Use **Flyway** or **Liquibase** for production migrations. Never rely on `ddl-auto: create` in production.

---

## Naming Conventions

| Concern | Convention | Example |
|---|---|---|
| Entity class | `PascalCase` | `ProductImage` |
| Table name | `snake_case`, plural | `product_images` |
| Column name | `snake_case` | `created_at`, `stock_quantity` |
| FK column | `{entity}_id` | `user_id`, `product_id` |
| Join table | `{table1}_{table2}` | `user_roles` |
| Enum column | stored as `STRING` | `status = 'ACTIVE'` |
| Boolean columns | prefixed with `is_` | `is_active`, `is_default` |
| Package | `com.ecommerce.entity` | — |

---

## Indexing Strategy

Beyond the default PK indexes, the following additional indexes should be created via migration scripts:

| Table | Column(s) | Type | Reason |
|---|---|---|---|
| `users` | `email` | UNIQUE | Login lookup |
| `users` | `status` | Standard | Filter active users |
| `products` | `sku` | UNIQUE | Inventory lookup |
| `products` | `category_id` | Standard | Category-filtered queries |
| `products` | `status` | Standard | Active product filter |
| `orders` | `user_id` | Standard | User order history |
| `orders` | `order_number` | UNIQUE | Order lookup |
| `orders` | `status` | Standard | Status-based filtering |
| `cart_items` | `(cart_id, product_id)` | UNIQUE | Duplicate prevention |
| `reviews` | `(user_id, product_id)` | UNIQUE | One review per user per product |
| `payments` | `transaction_id` | UNIQUE | Gateway dedup |
| `payments` | `order_id` | UNIQUE | One payment per order |

---

*End of Document*