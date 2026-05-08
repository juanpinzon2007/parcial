package com.papeleria.sales.controller;

import com.papeleria.sales.dto.OrderRequest;
import com.papeleria.sales.dto.OrderResponse;
import com.papeleria.sales.security.AuthenticatedUser;
import com.papeleria.sales.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of(
                "service", "sales-service",
                "status", "UP",
                "timestamp", Instant.now()
        );
    }

    @GetMapping("/orders")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public List<OrderResponse> findAll(@AuthenticationPrincipal AuthenticatedUser currentUser) {
        return orderService.findAll(currentUser);
    }

    @GetMapping("/orders/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public OrderResponse findById(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser currentUser) {
        return orderService.findById(id, currentUser);
    }

    @PostMapping("/orders")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public OrderResponse create(
            @Valid @RequestBody OrderRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        return orderService.create(request, currentUser);
    }

    @PutMapping("/orders/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public OrderResponse update(
            @PathVariable Long id,
            @Valid @RequestBody OrderRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        return orderService.update(id, request, currentUser);
    }

    @DeleteMapping("/orders/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public void delete(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser currentUser) {
        orderService.delete(id, currentUser);
    }
}
