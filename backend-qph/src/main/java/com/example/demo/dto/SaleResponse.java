package com.example.demo.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleResponse {

    private UUID id;

    private Long productId;

    private String productName;

    private Integer quantity;

    private BigDecimal unitPrice;

    private BigDecimal total;

    private String username;

    private SaleStatus status;

    private Instant createdAt;

    private Integer remainingStock;
}