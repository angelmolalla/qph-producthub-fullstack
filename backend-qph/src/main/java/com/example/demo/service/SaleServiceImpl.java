package com.example.demo.service;

import com.example.demo.dto.CreateSaleRequest;
import com.example.demo.dto.SaleResponse;
import com.example.demo.dto.SaleStatus;
import com.example.demo.entity.Product;
import com.example.demo.entity.Sale;
import com.example.demo.exception.InsufficientStockException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.SaleRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SaleServiceImpl
        implements SaleService {

    private final ProductRepository productRepository;
    private final SaleRepository saleRepository;


    @Override
    @Transactional
    public SaleResponse create(
            CreateSaleRequest request,
            String username) {
        Product product =
                productRepository
                        .findById(
                                request.getProductId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Producto no encontrado con id: "
                                                + request.getProductId()
                                )
                        );

        if (product.getStock()
                < request.getQuantity()) {

            throw new InsufficientStockException(
                    "Stock insuficiente para el producto "
                            + product.getNombre()
                            + ". Disponible: "
                            + product.getStock()
                            + ", solicitado: "
                            + request.getQuantity()
            );
        }

        int updatedRows =
                productRepository
                        .decrementStockIfAvailable(
                                product.getId(),
                                request.getQuantity()
                        );

        if (updatedRows == 0) {

            throw new InsufficientStockException(
                    "El stock cambió mientras se procesaba la venta. "
                            + "Por favor intenta nuevamente."
            );
        }

        BigDecimal total =
                product
                        .getPrecio()
                        .multiply(
                                BigDecimal.valueOf(
                                        request.getQuantity()
                                )
                        );

        Sale sale =
                Sale.builder()
                        .id(
                                UUID.randomUUID()
                        )
                        .productId(
                                product.getId()
                        )
                        .productName(
                                product.getNombre()
                        )
                        .quantity(
                                request.getQuantity()
                        )
                        .unitPrice(
                                product.getPrecio()
                        )
                        .total(total)
                        .username(username)
                        .status(
                                SaleStatus.COMPLETED
                        )
                        .createdAt(
                                Instant.now()
                        )
                        .build();

        Sale savedSale =
                saleRepository.save(sale);

        Integer remainingStock =
                productRepository
                        .findById(
                                product.getId()
                        )
                        .map(
                                Product::getStock
                        )
                        .orElse(0);

        return toResponse(
                savedSale,
                remainingStock
        );
    }


    @Override
    @Transactional(readOnly = true)
    public SaleResponse findById(
            UUID id,
            String username,
            boolean admin) {

        Sale sale =
                saleRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Venta no encontrada con id: "
                                                + id
                                )
                        );

        if (!admin
                && !sale.getUsername()
                .equals(username)) {

            throw new AccessDeniedException(
                    "No tienes permisos para consultar esta venta"
            );
        }

        return toResponse(
                sale,
                null
        );
    }


    @Override
    @Transactional(readOnly = true)
    public List<SaleResponse> findAll() {

        return saleRepository
                .findAllByOrderByCreatedAtDesc()
                .stream()
                .map(
                        sale ->
                                toResponse(
                                        sale,
                                        null
                                )
                )
                .toList();
    }


    @Override
    @Transactional(readOnly = true)
    public List<SaleResponse> findMySales(
            String username) {

        return saleRepository
                .findByUsernameOrderByCreatedAtDesc(
                        username
                )
                .stream()
                .map(
                        sale ->
                                toResponse(
                                        sale,
                                        null
                                )
                )
                .toList();
    }


    private SaleResponse toResponse(
            Sale sale,
            Integer remainingStock) {

        return SaleResponse.builder()
                .id(
                        sale.getId()
                )
                .productId(
                        sale.getProductId()
                )
                .productName(
                        sale.getProductName()
                )
                .quantity(
                        sale.getQuantity()
                )
                .unitPrice(
                        sale.getUnitPrice()
                )
                .total(
                        sale.getTotal()
                )
                .username(
                        sale.getUsername()
                )
                .status(
                        sale.getStatus()
                )
                .createdAt(
                        sale.getCreatedAt()
                )
                .remainingStock(
                        remainingStock
                )
                .build();
    }
}
