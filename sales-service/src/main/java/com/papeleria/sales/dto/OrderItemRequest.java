package com.papeleria.sales.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record OrderItemRequest(
        @NotBlank(message = "Debe indicar el producto")
        String productName,
        @NotNull(message = "Debe indicar la cantidad")
        @Min(value = 1, message = "La cantidad debe ser mayor a cero")
        Integer quantity,
        @NotNull(message = "Debe indicar el precio unitario")
        @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a cero")
        BigDecimal unitPrice
) {
}
