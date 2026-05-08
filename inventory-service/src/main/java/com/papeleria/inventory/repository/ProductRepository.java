package com.papeleria.inventory.repository;

import com.papeleria.inventory.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    long countByStockLessThanEqual(Integer stock);
}
