package com.example.demo.repository;

import com.example.demo.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SaleRepository
        extends JpaRepository<Sale, UUID> {

    List<Sale> findAllByOrderByCreatedAtDesc();

    List<Sale> findByUsernameOrderByCreatedAtDesc(
            String username
    );
}