package com.example.demo.service;

import com.example.demo.dto.CreateSaleRequest;
import com.example.demo.dto.SaleResponse;
import com.example.demo.dto.SaleStatus;
import com.example.demo.entity.Product;
import com.example.demo.entity.Sale;
import com.example.demo.exception.InsufficientStockException;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.SaleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SaleServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private SaleRepository saleRepository;

    @InjectMocks
    private SaleServiceImpl service;

    @Test
    void shouldCreateSaleAndDecreaseStock() {

        Product original =
                product(
                        1L,
                        "Laptop",
                        "1000.00",
                        10
                );

        Product updated =
                product(
                        1L,
                        "Laptop",
                        "1000.00",
                        7
                );

        when(
                productRepository.findById(
                        1L
                )
        )
                .thenReturn(
                        Optional.of(original),
                        Optional.of(updated)
                );

        when(
                productRepository
                        .decrementStockIfAvailable(
                                1L,
                                3
                        )
        )
                .thenReturn(1);

        when(
                saleRepository.save(
                        any(Sale.class)
                )
        )
                .thenAnswer(
                        invocation ->
                                invocation.getArgument(0)
                );

        CreateSaleRequest request =
                request(
                        1L,
                        3
                );

        SaleResponse response =
                service.create(
                        request,
                        "angelo"
                );

        assertThat(
                response.getProductName()
        ).isEqualTo("Laptop");

        assertThat(
                response.getTotal()
        ).isEqualByComparingTo(
                "3000.00"
        );

        assertThat(
                response.getRemainingStock()
        ).isEqualTo(7);

        assertThat(
                response.getStatus()
        ).isEqualTo(
                SaleStatus.COMPLETED
        );

        verify(productRepository)
                .decrementStockIfAvailable(
                        1L,
                        3
                );

        verify(saleRepository)
                .save(any(Sale.class));
    }

    @Test
    void shouldRejectSaleWhenStockIsInsufficient() {

        Product product =
                product(
                        1L,
                        "Mouse",
                        "25.00",
                        2
                );

        when(
                productRepository.findById(
                        1L
                )
        )
                .thenReturn(
                        Optional.of(product)
                );

        CreateSaleRequest request =
                request(
                        1L,
                        5
                );

        assertThatThrownBy(
                () ->
                        service.create(
                                request,
                                "angelo"
                        )
        )
                .isInstanceOf(
                        InsufficientStockException.class
                )
                .hasMessageContaining(
                        "Disponible: 2"
                );

        verify(
                productRepository,
                never()
        )
                .decrementStockIfAvailable(
                        anyLong(),
                        anyInt()
                );

        verify(
                saleRepository,
                never()
        )
                .save(any());
    }

    @Test
    void shouldRejectSaleWhenStockChangesConcurrently() {

        Product product =
                product(
                        1L,
                        "Keyboard",
                        "80.00",
                        5
                );

        when(
                productRepository.findById(
                        1L
                )
        )
                .thenReturn(
                        Optional.of(product)
                );

        when(
                productRepository
                        .decrementStockIfAvailable(
                                1L,
                                5
                        )
        )
                .thenReturn(0);

        assertThatThrownBy(
                () ->
                        service.create(
                                request(
                                        1L,
                                        5
                                ),
                                "angelo"
                        )
        )
                .isInstanceOf(
                        InsufficientStockException.class
                )
                .hasMessageContaining(
                        "stock cambió"
                );

        verify(
                saleRepository,
                never()
        )
                .save(any());
    }

    @Test
    void shouldRejectAccessToAnotherUsersSale() {

        UUID id =
                UUID.randomUUID();

        Sale sale =
                Sale.builder()
                        .id(id)
                        .username("otro")
                        .productId(1L)
                        .productName("Laptop")
                        .quantity(1)
                        .unitPrice(
                                new BigDecimal(
                                        "1000.00"
                                )
                        )
                        .total(
                                new BigDecimal(
                                        "1000.00"
                                )
                        )
                        .status(
                                SaleStatus.COMPLETED
                        )
                        .build();

        when(
                saleRepository.findById(id)
        )
                .thenReturn(
                        Optional.of(sale)
                );

        assertThatThrownBy(
                () ->
                        service.findById(
                                id,
                                "angelo",
                                false
                        )
        )
                .isInstanceOf(
                        AccessDeniedException.class
                );
    }

    private CreateSaleRequest request(
            Long productId,
            Integer quantity) {

        CreateSaleRequest request =
                new CreateSaleRequest();

        request.setProductId(
                productId
        );

        request.setQuantity(
                quantity
        );

        return request;
    }

    private Product product(
            Long id,
            String name,
            String price,
            Integer stock) {

        return Product.builder()
                .id(id)
                .nombre(name)
                .precio(
                        new BigDecimal(
                                price
                        )
                )
                .stock(stock)
                .build();
    }
}
