package com.example.demo.controller;

import com.example.demo.dto.ProductDTO;
import com.example.demo.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController controller;

    @Test
    void shouldReturnAllProducts() {
        List<ProductDTO> products = List.of(product(1L, "Laptop"));
        when(productService.findAll()).thenReturn(products);

        ResponseEntity<List<ProductDTO>> response = controller.getAll();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(products);
    }

    @Test
    void shouldReturnProductById() {
        ProductDTO product = product(1L, "Laptop");
        when(productService.findById(1L)).thenReturn(product);

        ResponseEntity<ProductDTO> response = controller.getById(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(product);
    }

    @Test
    void shouldCreateProduct() {
        ProductDTO request = product(null, "Laptop");
        ProductDTO created = product(1L, "Laptop");
        when(productService.create(request)).thenReturn(created);

        ResponseEntity<ProductDTO> response = controller.create(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(created);
    }

    @Test
    void shouldUpdateProduct() {
        ProductDTO request = product(null, "Laptop Pro");
        ProductDTO updated = product(1L, "Laptop Pro");
        when(productService.update(1L, request)).thenReturn(updated);

        ResponseEntity<ProductDTO> response = controller.update(1L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(updated);
    }

    @Test
    void shouldDeleteProduct() {
        ResponseEntity<Void> response = controller.delete(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(productService).delete(1L);
    }

    private ProductDTO product(Long id, String name) {
        return ProductDTO.builder()
                .id(id)
                .nombre(name)
                .precio(new BigDecimal("100.00"))
                .stock(10)
                .build();
    }
}
