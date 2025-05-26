package com.example.paymentservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

// The 'name' attribute is used for service discovery (e.g., with Eureka or Consul)
// The 'url' attribute is used for direct URL calls, typically from properties.
@FeignClient(name = "order-service", url = "${clients.order-service.url}")
public interface OrderServiceClient {

    // This method signature must match the one in OrderResource.java in the order-service
    // Specifically, the JAX-RS annotations in OrderResource translate to Spring MVC annotations here.
    // @Path("/{orderId}/status") with @PUT -> @PutMapping("/{orderId}/status")
    // @PathParam("orderId") -> @PathVariable("orderId")
    // @QueryParam("status") -> @RequestParam("status")
    @PutMapping("/api/orders/{orderId}/status")
    void updateOrderStatus(@PathVariable("orderId") Long orderId, @RequestParam("status") String status);
}
