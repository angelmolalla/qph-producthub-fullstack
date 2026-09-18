package com.example.demo.service;

import com.example.demo.dto.CreateSaleRequest;
import com.example.demo.dto.SaleResponse;

import java.util.List;
import java.util.UUID;

public interface SaleService {

    SaleResponse create(
            CreateSaleRequest request,
            String username
    );

    SaleResponse findById(
            UUID id,
            String username,
            boolean admin
    );

    List<SaleResponse> findAll();

    List<SaleResponse> findMySales(
            String username
    );
}