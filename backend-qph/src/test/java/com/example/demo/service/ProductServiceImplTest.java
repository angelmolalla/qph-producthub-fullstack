package com.example.demo.service;

import com.example.demo.dto.ProductDTO;
import com.example.demo.entity.Product;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository repository;

    @InjectMocks
    private ProductServiceImpl service;

    @Test
    void shouldReturnAllProducts() {

        when(repository.findAll())
                .thenReturn(
                        List.of(
                                product(
                                        1L,
                                        "Laptop",
                                        "1200.00",
                                        5
                                )
                        )
                );

        List<ProductDTO> result =
                service.findAll();

        assertThat(result)
                .hasSize(1);

        assertThat(
                result.getFirst()
                        .getNombre()
        ).isEqualTo("Laptop");
    }

    @Test
    void shouldCreateProduct() {

        ProductDTO request =
                ProductDTO.builder()
                        .nombre("Mouse")
                        .precio(
                                new BigDecimal(
                                        "25.50"
                                )
                        )
                        .stock(10)
                        .build();

        when(repository.save(any(Product.class)))
                .thenAnswer(invocation -> {
                    Product saved =
                            invocation.getArgument(0);

                    saved.setId(1L);

                    return saved;
                });

        ProductDTO result =
                service.create(request);

        assertThat(result.getId())
                .isEqualTo(1L);

        assertThat(result.getNombre())
                .isEqualTo("Mouse");

        verify(repository)
                .save(any(Product.class));
    }

    @Test
    void shouldThrowWhenProductDoesNotExist() {

        when(repository.findById(99L))
                .thenReturn(
                        Optional.empty()
                );

        assertThatThrownBy(
                () ->
                        service.findById(
                                99L
                        )
        )
                .isInstanceOf(
                        ResourceNotFoundException.class
                )
                .hasMessageContaining(
                        "99"
                );
    }

    @Test
    void shouldDeleteExistingProduct() {

        when(repository.existsById(1L))
                .thenReturn(true);

        service.delete(1L);

        verify(repository)
                .deleteById(1L);
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
