package com.papeleria.sales.dto;

import com.papeleria.sales.model.OrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record OrderRequest(
        @NotEmpty(message = "Debe agregar al menos un item")
        List<@Valid OrderItemRequest> items,
        String notes,
        OrderStatus status
) {
}
