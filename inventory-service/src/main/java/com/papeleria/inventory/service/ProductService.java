package com.papeleria.inventory.service;

import com.papeleria.inventory.dto.InventorySummaryResponse;
import com.papeleria.inventory.dto.ProductRequest;
import com.papeleria.inventory.dto.ProductResponse;
import com.papeleria.inventory.exception.ResourceNotFoundException;
import com.papeleria.inventory.model.Product;
import com.papeleria.inventory.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<ProductResponse> findAll() {
        return productRepository.findAll().stream()
                .map(this::mapResponse)
                .toList();
    }

    public ProductResponse findById(Long id) {
        return mapResponse(getEntity(id));
    }

    public ProductResponse create(ProductRequest request) {
        Product product = new Product();
        applyChanges(product, request);
        return mapResponse(productRepository.save(product));
    }

    public ProductResponse update(Long id, ProductRequest request) {
        Product product = getEntity(id);
        applyChanges(product, request);
        return mapResponse(productRepository.save(product));
    }

    public void delete(Long id) {
        productRepository.delete(getEntity(id));
    }

    public InventorySummaryResponse getSummary() {
        List<Product> products = productRepository.findAll();
        long totalUnitsInStock = products.stream()
                .mapToLong(Product::getStock)
                .sum();
        return new InventorySummaryResponse(
                products.size(),
                totalUnitsInStock,
                productRepository.countByStockLessThanEqual(5)
        );
    }

    private Product getEntity(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
    }

    private void applyChanges(Product product, ProductRequest request) {
        product.setName(request.name().trim());
        product.setBrand(request.brand().trim());
        product.setCategory(request.category().trim());
        product.setDescription(request.description() == null ? null : request.description().trim());
        product.setPrice(request.price());
        product.setStock(request.stock());
        product.setActive(request.active());
    }

    private ProductResponse mapResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getBrand(),
                product.getCategory(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getActive(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
