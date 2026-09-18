package com.example.demo.repository;

import com.example.demo.entity.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
class ProductRepositoryTest {

    @Autowired
    private ProductRepository repository;

    @Test
    void shouldDecrementStockWhenEnoughStockExists() {
        Product product = repository.saveAndFlush(Product.builder()
                .nombre("Laptop")
                .precio(new BigDecimal("100.00"))
                .stock(10)
                .build());

        int updated = repository.decrementStockIfAvailable(product.getId(), 3);
        Product refreshed = repository.findById(product.getId()).orElseThrow();

        assertThat(updated).isEqualTo(1);
        assertThat(refreshed.getStock()).isEqualTo(7);
    }

    @Test
    void shouldNotDecrementStockWhenQuantityIsGreaterThanAvailable() {
        Product product = repository.saveAndFlush(Product.builder()
                .nombre("Laptop")
                .precio(new BigDecimal("100.00"))
                .stock(2)
                .build());

        int updated = repository.decrementStockIfAvailable(product.getId(), 3);
        Product refreshed = repository.findById(product.getId()).orElseThrow();

        assertThat(updated).isZero();
        assertThat(refreshed.getStock()).isEqualTo(2);
    }
}
