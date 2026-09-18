package com.example.demo.dto;

import com.example.demo.entity.Role;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateUserRequest {

    @NotBlank
    private String username;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 8)
    private String password;

    @NotNull
    private Role role;

    @NotNull
    private Boolean twoFactorEnabled;
}