package com.example.demo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateSaleRequest {

    @NotNull
    private Long productId;

    @NotNull
    @Min(
            value = 1,
            message = "La cantidad debe ser mayor a cero"
    )
    private Integer quantity;
}
