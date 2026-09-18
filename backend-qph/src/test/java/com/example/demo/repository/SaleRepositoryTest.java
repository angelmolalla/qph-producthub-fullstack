package com.example.demo.repository;

import com.example.demo.dto.SaleStatus;
import com.example.demo.entity.Sale;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
class SaleRepositoryTest {

    @Autowired
    private SaleRepository repository;

    @Test
    void shouldReturnSalesOrderedByCreatedAtDescending() {
        Sale oldSale = sale("angelo", Instant.parse("2026-09-17T10:00:00Z"));
        Sale newSale = sale("admin", Instant.parse("2026-09-17T12:00:00Z"));
        repository.saveAllAndFlush(List.of(oldSale, newSale));

        List<Sale> result = repository.findAllByOrderByCreatedAtDesc();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCreatedAt()).isAfter(result.get(1).getCreatedAt());
    }

    @Test
    void shouldReturnOnlyUsersSalesOrderedDescending() {
        repository.saveAllAndFlush(List.of(
                sale("angelo", Instant.parse("2026-09-17T10:00:00Z")),
                sale("admin", Instant.parse("2026-09-17T11:00:00Z")),
                sale("angelo", Instant.parse("2026-09-17T12:00:00Z"))
        ));

        List<Sale> result = repository.findByUsernameOrderByCreatedAtDesc("angelo");

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(s -> s.getUsername().equals("angelo"));
        assertThat(result.get(0).getCreatedAt()).isAfter(result.get(1).getCreatedAt());
    }

    private Sale sale(String username, Instant createdAt) {
        return Sale.builder()
                .id(UUID.randomUUID())
                .productId(1L)
                .productName("Laptop")
                .quantity(1)
                .unitPrice(new BigDecimal("100.00"))
                .total(new BigDecimal("100.00"))
                .username(username)
                .status(SaleStatus.COMPLETED)
                .createdAt(createdAt)
                .build();
    }
}
