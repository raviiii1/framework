package com.ravi9a2.example.service;

import com.ravi9a2.example.model.User;
import com.ravi9a2.nea.annotations.ExternalService;
import com.ravi9a2.nea.annotations.GetCall;
import com.ravi9a2.nea.annotations.PathParam;
import com.ravi9a2.nea.annotations.PostCall;
import com.ravi9a2.nea.annotations.Payload;
import reactor.core.publisher.Mono;

/**
 * Example external service interface demonstrating code generation.
 * The annotation processor will generate an implementation of this interface.
 * 
 * @author raviprakash
 */
@ExternalService
public interface UserService {

    /**
     * Get user by ID.
     * This method will be automatically instrumented and have resilience patterns
     * applied.
     */
    @GetCall(path = "/api/users/{userId}", service = "user-service", cbEnabled = true, circuitBreaker = "user-service-cb", bhEnabled = true, bulkhead = "user-service-bh")
    Mono<User> getUser(@PathParam("userId") String userId);

    /**
     * Create a new user.
     */
    @PostCall(path = "/api/users", service = "user-service", cbEnabled = true, circuitBreaker = "user-service-cb")
    Mono<User> createUser(@Payload User user);

    /**
     * Get all users.
     */
    @GetCall(path = "/api/users", service = "user-service", cbEnabled = true, circuitBreaker = "user-service-cb")
    Mono<java.util.List<User>> getAllUsers();
}
