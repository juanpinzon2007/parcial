package com.papeleria.inventory.config;

import com.papeleria.inventory.model.Product;
import com.papeleria.inventory.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

@Configuration
public class ProductSeeder {

    @Bean
    public CommandLineRunner seedProducts(ProductRepository productRepository) {
        return args -> {
            if (productRepository.count() > 0) {
                return;
            }

            productRepository.saveAll(List.of(
                    buildProduct("Cuaderno cuadriculado", "Norma", "Escolar", "Cuaderno de 100 hojas", new BigDecimal("8500"), 25, true),
                    buildProduct("Lapicero azul", "Bic", "Escritura", "Boligrafo tinta azul", new BigDecimal("2500"), 120, true),
                    buildProduct("Resma carta", "Reprograf", "Oficina", "Papel blanco de 500 hojas", new BigDecimal("19500"), 14, true),
                    buildProduct("Marcador borrable", "Sharpie", "Pizarra", "Marcador punta fina color negro", new BigDecimal("6500"), 4, true)
            ));
        };
    }

    private Product buildProduct(
            String name,
            String brand,
            String category,
            String description,
            BigDecimal price,
            Integer stock,
            Boolean active
    ) {
        Product product = new Product();
        product.setName(name);
        product.setBrand(brand);
        product.setCategory(category);
        product.setDescription(description);
        product.setPrice(price);
        product.setStock(stock);
        product.setActive(active);
        return product;
    }
}
