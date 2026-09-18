package com.example.demo.controller;

import com.example.demo.dto.CreateSaleRequest;
import com.example.demo.dto.SaleResponse;
import com.example.demo.service.SaleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
public class SaleController {

    private final SaleService saleService;


    @PostMapping
    public ResponseEntity<SaleResponse> create(
            @Valid
            @RequestBody
            CreateSaleRequest request,

            Authentication authentication) {
        String username =
                authentication.getName();

        SaleResponse sale =
                saleService.create(
                        request,
                        username
                );

        return ResponseEntity
                .status(
                        HttpStatus.CREATED
                )
                .body(sale);
    }

    @GetMapping("/me")
    public ResponseEntity<List<SaleResponse>>
    findMySales(
            Authentication authentication) {

        return ResponseEntity.ok(
                saleService.findMySales(
                        authentication.getName()
                )
        );
    }


    @GetMapping("/{id}")
    public ResponseEntity<SaleResponse>
    findById(
            @PathVariable
            UUID id,

            Authentication authentication) {

        boolean admin =
                authentication
                        .getAuthorities()
                        .stream()
                        .map(
                                GrantedAuthority::getAuthority
                        )
                        .anyMatch(
                                "ROLE_ADMIN"::equals
                        );

        return ResponseEntity.ok(
                saleService.findById(
                        id,
                        authentication.getName(),
                        admin
                )
        );
    }

    @GetMapping
    public ResponseEntity<List<SaleResponse>>
    findAll() {
        return ResponseEntity.ok(
                saleService.findAll()
        );
    }
}