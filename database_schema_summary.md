# Database Schema Summary and Setup Instructions

This document outlines the database schemas for the microservices in the Artisan Marketplace application and provides instructions for setting them up.

## 1. MySQL Schema Summary

The following services use MySQL databases. The `spring.jpa.hibernate.ddl-auto=update` property in `application.properties` will instruct Hibernate to create/update tables based on entity definitions. The `?createDatabaseIfNotExist=true` in the JDBC URL will attempt to create the databases if the MySQL user has sufficient privileges.

### a. User Service (`user_db`)

*   **`users` table:** Stores user information.
    *   `id` (BIGINT, Primary Key, Auto Increment)
    *   `email` (VARCHAR(255), Not Null, Unique)
    *   `password` (VARCHAR(255), Not Null)
    *   `first_name` (VARCHAR(255), Not Null)
    *   `last_name` (VARCHAR(255), Not Null)
    *   `role` (VARCHAR(255), Not Null) - *e.g., "CUSTOMER", "ARTISAN"*

### b. Order Service (`order_db`)

*   **`orders` table:** Stores order information.
    *   `id` (BIGINT, Primary Key, Auto Increment)
    *   `user_id` (VARCHAR(255), Not Null) - *Corresponds to `User.id` from `user_db` if users are identified by a string ID there, or `User.email`. The current `User.id` is Long, so this might need alignment, or `userId` in `Order` should be `Long` if User IDs are numeric.*
    *   `order_date` (DATETIME, Not Null) - *Automatically set on creation.*
    *   `status` (VARCHAR(255), Not Null) - *e.g., "PENDING_PAYMENT", "PAID", "SHIPPED", "DELIVERED"*
    *   `total_amount` (DOUBLE, Not Null)
    *   `shipping_address` (VARCHAR(255), Not Null)

*   **`order_items` table:** Stores items within an order.
    *   `id` (BIGINT, Primary Key, Auto Increment)
    *   `product_id` (VARCHAR(255), Not Null) - *Corresponds to `Product.id` (String) from `product_db` (MongoDB)*
    *   `quantity` (INT, Not Null)
    *   `price_per_item` (DOUBLE, Not Null)
    *   `product_name` (VARCHAR(255), Not Null) - *Denormalized for convenience.*
    *   `order_id` (BIGINT, Not Null, Foreign Key references `orders(id)`)

### c. Payment Service (`payment_db`)

*   **`payments` table:** Stores payment information.
    *   `id` (BIGINT, Primary Key, Auto Increment)
    *   `order_id` (BIGINT, Not Null, Unique) - *Corresponds to `Order.id` from `order_db`.*
    *   `payment_date` (DATETIME, Not Null) - *Automatically set on creation.*
    *   `amount` (DOUBLE, Not Null)
    *   `payment_method` (VARCHAR(255), Not Null) - *e.g., "MOCK_CREDIT_CARD"*
    *   `status` (VARCHAR(255), Not Null) - *e.g., "PENDING", "SUCCESSFUL", "FAILED"*

## 2. MongoDB Schema Summary

The following services use MongoDB. Databases and collections are typically created automatically by Spring Data MongoDB when data is first written.

### a. Product Service (`product_db`)

*   **`products` collection:** Stores product details.
    *   `_id` (String, Primary Key, typically MongoDB ObjectId represented as String in Java)
    *   `artisanId` (String) - *Corresponds to `User.id` (if string) or `User.email`.*
    *   `name` (String)
    *   `description` (String)
    *   `price` (Double)
    *   `category` (String)
    *   `images` (List of Strings) - *URLs to product images.*
    *   `stockQuantity` (Integer)
    *   `details` (Map of String to String) - *e.g., material, dimensions.*
    *   `_class` (String) - *Added by Spring Data MongoDB for type mapping if not disabled.*

### b. Cart Service (`cart_db`)

*   **`carts` collection:** Stores shopping cart details.
    *   `_id` (String, Primary Key) - *Represents the `userId` whose cart this is.*
    *   `items` (List of embedded documents):
        *   `productId` (String) - *Corresponds to `Product.id` from `product_db`.*
        *   `quantity` (Integer)
        *   `priceAtAddition` (Double) - *Price of the product when it was added to the cart.*
        *   `productName` (String) - *Denormalized for convenience.*
        *   `productImage` (String) - *Optional URL to a product image.*
    *   `lastModified` (Date) - *Timestamp of the last modification.*
    *   `_class` (String) - *Added by Spring Data MongoDB.*

### c. Product Search Service (uses `product_db`)

*   This service reads from the `products` collection in the `product_db` managed by the Product Service. It does not own its own collections for product data.

## 3. Database Setup Instructions

### a. MySQL Setup

1.  **Automated Database Creation (Recommended for Dev):**
    The `application.properties` for each MySQL-based service includes `?createDatabaseIfNotExist=true` in the `spring.datasource.url`. If the MySQL user specified in the properties (e.g., `your_mysql_username`) has `CREATE DATABASE` privileges, the databases (`user_db`, `order_db`, `payment_db`) will be created automatically upon service startup if they don't exist.

2.  **Manual Database Creation (If Needed):**
    If automatic creation is not desired or not possible due to user privileges, create the databases manually using a MySQL client:
    ```sql
    CREATE DATABASE IF NOT EXISTS user_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
    CREATE DATABASE IF NOT EXISTS order_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
    CREATE DATABASE IF NOT EXISTS payment_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
    ```
    You may also need to create a dedicated application user and grant privileges:
    ```sql
    -- Replace 'appuser' and 'password' with your desired username and password
    -- CREATE USER 'appuser'@'localhost' IDENTIFIED BY 'secure_password'; 
    -- GRANT ALL PRIVILEGES ON user_db.* TO 'appuser'@'localhost';
    -- GRANT ALL PRIVILEGES ON order_db.* TO 'appuser'@'localhost';
    -- GRANT ALL PRIVILEGES ON payment_db.* TO 'appuser'@'localhost';
    -- FLUSH PRIVILEGES;
    ```
    Remember to update the `spring.datasource.username` and `spring.datasource.password` in the respective `application.properties` files.

3.  **Table Creation:**
    Hibernate (`spring.jpa.hibernate.ddl-auto=update`) will automatically create or update tables according to the JPA entity definitions when each service starts.

### b. MongoDB Setup

1.  **Automated Database and Collection Creation:**
    Spring Data MongoDB will automatically create the databases (`product_db`, `cart_db`) and their respective collections (`products`, `carts`) when the services first attempt to write data, provided the MongoDB server is running and accessible with the configured URI (e.g., `mongodb://localhost:27017/product_db`).

2.  **Text Index for Product Search (CRUCIAL):**
    For the Product Search Service to perform text-based searches efficiently on product names, descriptions, and categories, a text index **must** be created on the `products` collection in the `product_db`.
    Connect to your MongoDB instance using `mongosh` or your preferred MongoDB client and run the following command:
    ```javascript
    use product_db; // Switch to the product_db database

    db.products.createIndex(
       {
         name: "text",
         description: "text",
         category: "text"
       },
       {
         name: "ProductTextIndex" // Optional: specify a name for the index
       }
    );
    ```
    This step is essential for the functionality of the search endpoint in `product-search-service`.

## Notes:

*   **User ID Consistency:** The `Order.userId` is a `String`. The `User.id` in `user-service` is a `Long`. This needs to be reconciled. Either `Order.userId` should become `Long` (if feasible and `User.id` is the intended foreign key), or user identification across services should consistently use a string identifier (like email or a UUID if `User.id` were a string). For now, the schema reflects the current code.
*   **Foreign Key Enforcement:** True database-level foreign key constraints between different databases (e.g., `payment_db.payments.order_id` to `order_db.orders.id`) are not typically enforced. This relationship is managed at the application level.
*   **Configuration:** Ensure that the database connection details (host, port, username, password) in each service's `application.properties` file are correctly configured for your environment.
