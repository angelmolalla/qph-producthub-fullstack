package com.example.demo.repository;

import com.example.demo.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository
        extends JpaRepository<Product, Long> {

    @Modifying(
            clearAutomatically = true,
            flushAutomatically = true
    )
    @Query("""
        UPDATE Product p
           SET p.stock = p.stock - :quantity
         WHERE p.id = :productId
           AND p.stock >= :quantity
        """)
    int decrementStockIfAvailable(
            @Param("productId")
            Long productId,

            @Param("quantity")
            Integer quantity
    );
}