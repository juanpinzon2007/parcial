package com.papeleria.sales.dto;

import com.papeleria.sales.model.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
        Long id,
        String customerEmail,
        String customerName,
        OrderStatus status,
        String notes,
        BigDecimal total,
        Instant createdAt,
        Instant updatedAt,
        List<OrderItemResponse> items
) {
}
