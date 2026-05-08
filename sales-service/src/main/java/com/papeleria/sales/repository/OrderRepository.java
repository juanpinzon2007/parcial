package com.papeleria.sales.repository;

import com.papeleria.sales.model.OrderRecord;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<OrderRecord, Long> {

    @EntityGraph(attributePaths = "items")
    List<OrderRecord> findAllByCustomerEmailOrderByCreatedAtDesc(String customerEmail);

    @Override
    @EntityGraph(attributePaths = "items")
    List<OrderRecord> findAll();

    @Override
    @EntityGraph(attributePaths = "items")
    Optional<OrderRecord> findById(Long id);
}
