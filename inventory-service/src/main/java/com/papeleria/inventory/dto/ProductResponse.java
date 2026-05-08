package com.papeleria.inventory.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductResponse(
        Long id,
        String name,
        String brand,
        String category,
        String description,
        BigDecimal price,
        Integer stock,
        Boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
