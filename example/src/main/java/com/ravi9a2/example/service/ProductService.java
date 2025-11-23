package com.ravi9a2.example.service;

import com.ravi9a2.example.model.User;
import com.ravi9a2.nea.annotations.ExternalService;
import com.ravi9a2.nea.annotations.GetCall;
import com.ravi9a2.nea.annotations.PathParam;
import com.ravi9a2.nea.annotations.PostCall;
import com.ravi9a2.nea.annotations.Payload;

/**
 * Non-reactive (blocking) HTTP service example.
 * 
 * This interface demonstrates blocking HTTP calls using Apache HttpClient.
 * Methods return regular types (not Mono/Flux), making them suitable for
 * traditional blocking code.
 * 
 * The annotation processor will generate ProductServiceImpl with:
 * - Blocking HTTP client calls
 * - Circuit breaker protection
 * - Bulkhead isolation
 * - Automatic instrumentation
 */
@ExternalService
public interface ProductService {

    /**
     * Get product by ID (blocking call).
     * This method blocks until the response is received.
     */
    @GetCall(path = "/api/products/{productId}", service = "product-service", cbEnabled = true, circuitBreaker = "product-service-cb", bhEnabled = true, bulkhead = "product-service-bh")
    User getProduct(@PathParam("productId") String productId);

    /**
     * Create a new product (blocking call).
     */
    @PostCall(path = "/api/products", service = "product-service", cbEnabled = true, circuitBreaker = "product-service-cb")
    User createProduct(@Payload User product);

    /**
     * Get all products (blocking call).
     */
    @GetCall(path = "/api/products", service = "product-service", cbEnabled = true, circuitBreaker = "product-service-cb")
    java.util.List<User> getAllProducts();
}
