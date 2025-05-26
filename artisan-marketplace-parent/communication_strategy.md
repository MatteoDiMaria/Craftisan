# Microservice Communication Strategy

This document outlines the strategy for inter-service communication within the Artisan Marketplace application.

## Overall Strategy

The primary mode of synchronous inter-service communication will be REST APIs, facilitated by Spring Cloud OpenFeign clients. Feign provides a declarative way to define HTTP clients, making it easier to consume RESTful services.

Asynchronous communication via message queues (e.g., Kafka, RabbitMQ) can be considered for future enhancements where decoupling and resilience are critical. Examples include:
*   Order creation triggering an event to clear the user's cart.
*   Product updates propagating to a search index.

## Implemented Communication Points

### 1. `payment-service` -> `order-service`

*   **Purpose:** To update the status of an order (e.g., to "PAID" or "PAYMENT_FAILED") after a payment attempt.
*   **Mechanism:** Synchronous REST call using a Spring Cloud OpenFeign client.
*   **Details:**
    *   An `OrderServiceClient` interface is defined in `payment-service`.
    *   This Feign client targets the `PUT /api/orders/{orderId}/status` endpoint exposed by `order-service`.
    *   The URL for the `order-service` is configured in `payment-service`'s `application.properties` via `clients.order-service.url`.

## Future Considerations / To Be Implemented

The following communication points are anticipated and will likely use Feign clients as well, unless an asynchronous pattern is more suitable:

### 1. `order-service` -> `cart-service`

*   **Purpose:** To clear the user's shopping cart after an order has been successfully placed and payment is confirmed (or initiated).
*   **Proposed Mechanism:** Synchronous REST call using a Feign client (`CartServiceClient`) to be created in `order-service`.
*   **Target Endpoint (Example):** `DELETE /api/carts/{cartId}` in `cart-service`.
*   **Alternative:** An asynchronous event could be published by `order-service`, and `cart-service` could subscribe to it. This would decouple the services more effectively.

### 2. `cart-service` -> `product-service`

*   **Purpose:** To fetch live product details (e.g., current price, stock quantity, product name) when an item is added to the cart. This ensures that the cart reflects the most up-to-date product information.
*   **Proposed Mechanism:** Synchronous REST call using a Feign client (`ProductServiceClient`) to be created in `cart-service`.
*   **Target Endpoint (Example):** `GET /api/products/{productId}` in `product-service`.
*   **Note:** This would replace the current approach where some product details (like name and price) are passed in the `AddItemRequest` to `cart-service`. Fetching live data improves consistency but introduces a runtime dependency.

## Service Discovery

Currently, service URLs are hardcoded in application properties (e.g., `clients.order-service.url`). For dynamic environments, such as deployments using Kubernetes or when running multiple instances of services, hardcoding URLs is not scalable or resilient.

**Future Enhancement:** Integrate a service discovery mechanism (e.g., Netflix Eureka, HashiCorp Consul, or Kubernetes-native service discovery) with Spring Cloud OpenFeign. This would allow Feign clients to look up service locations dynamically using their registered service names (e.g., "order-service") rather than static URLs.

## Error Handling and Resilience

For inter-service calls, especially synchronous ones, robust error handling and resilience patterns are crucial.
*   **Timeouts:** Configure appropriate connect and read timeouts for Feign clients.
*   **Retries:** Implement retry mechanisms for transient failures. Spring Retry can be used in conjunction with Feign.
*   **Circuit Breakers:** For services that might be temporarily unavailable or slow, a circuit breaker pattern (e.g., using Resilience4j) should be implemented to prevent cascading failures. Feign has integrations with Resilience4j.
*   **Fallbacks:** Define fallback responses or actions if a service call fails repeatedly or the circuit is open.

These patterns will be incorporated as the system matures and specific needs arise.
