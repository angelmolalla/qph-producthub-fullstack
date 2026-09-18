package com.example.demo.controller;

import com.example.demo.dto.CreateSaleRequest;
import com.example.demo.dto.SaleResponse;
import com.example.demo.dto.SaleStatus;
import com.example.demo.service.SaleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SaleControllerTest {

    @Mock
    private SaleService saleService;

    @InjectMocks
    private SaleController controller;

    @Test
    void shouldCreateSaleForAuthenticatedUser() {
        CreateSaleRequest request = new CreateSaleRequest();
        request.setProductId(1L);
        request.setQuantity(2);
        SaleResponse sale = saleResponse();
        when(saleService.create(request, "angelo")).thenReturn(sale);

        ResponseEntity<SaleResponse> result = controller.create(request, authentication("angelo", "ROLE_USER"));

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isEqualTo(sale);
        verify(saleService).create(request, "angelo");
    }

    @Test
    void shouldReturnAuthenticatedUsersSales() {
        List<SaleResponse> sales = List.of(saleResponse());
        when(saleService.findMySales("angelo")).thenReturn(sales);

        ResponseEntity<List<SaleResponse>> result = controller.findMySales(authentication("angelo", "ROLE_USER"));

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(sales);
    }

    @Test
    void shouldDetectAdminWhenFindingSaleById() {
        UUID id = UUID.randomUUID();
        SaleResponse sale = saleResponse();
        when(saleService.findById(id, "admin", true)).thenReturn(sale);

        ResponseEntity<SaleResponse> result = controller.findById(id, authentication("admin", "ROLE_ADMIN"));

        assertThat(result.getBody()).isEqualTo(sale);
        verify(saleService).findById(id, "admin", true);
    }

    @Test
    void shouldDetectRegularUserWhenFindingSaleById() {
        UUID id = UUID.randomUUID();
        SaleResponse sale = saleResponse();
        when(saleService.findById(id, "angelo", false)).thenReturn(sale);

        ResponseEntity<SaleResponse> result = controller.findById(id, authentication("angelo", "ROLE_USER"));

        assertThat(result.getBody()).isEqualTo(sale);
        verify(saleService).findById(id, "angelo", false);
    }

    @Test
    void shouldReturnAllSales() {
        List<SaleResponse> sales = List.of(saleResponse());
        when(saleService.findAll()).thenReturn(sales);

        ResponseEntity<List<SaleResponse>> result = controller.findAll();

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(sales);
    }

    private Authentication authentication(String username, String authority) {
        return new UsernamePasswordAuthenticationToken(
                username,
                null,
                List.of(new SimpleGrantedAuthority(authority))
        );
    }

    private SaleResponse saleResponse() {
        return SaleResponse.builder()
                .id(UUID.randomUUID())
                .productId(1L)
                .productName("Laptop")
                .quantity(2)
                .unitPrice(new BigDecimal("100.00"))
                .total(new BigDecimal("200.00"))
                .username("angelo")
                .status(SaleStatus.COMPLETED)
                .createdAt(Instant.now())
                .remainingStock(8)
                .build();
    }
}
