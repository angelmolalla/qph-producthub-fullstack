package com.example.demo.entity;

import com.example.demo.dto.SaleStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "sales")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Sale {

    @Id
    private UUID id;

    @Column(
            name = "product_id",
            nullable = false
    )
    private Long productId;

    @Column(
            name = "product_name",
            nullable = false
    )
    private String productName;

    @Column(nullable = false)
    private Integer quantity;

    @Column(
            name = "unit_price",
            nullable = false,
            precision = 38,
            scale = 2
    )
    private BigDecimal unitPrice;

    @Column(
            nullable = false,
            precision = 38,
            scale = 2
    )
    private BigDecimal total;

    @Column(
            nullable = false
    )
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SaleStatus status;

    @Column(
            name = "created_at",
            nullable = false
    )
    private Instant createdAt;
}