package com.papeleria.inventory.dto;

public record InventorySummaryResponse(
        long totalProducts,
        long totalUnitsInStock,
        long lowStockProducts
) {
}
