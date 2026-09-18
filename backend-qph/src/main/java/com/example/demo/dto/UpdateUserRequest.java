package com.example.demo.dto;

import com.example.demo.entity.Role;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateUserRequest {

    @NotBlank
    private String username;

    @NotBlank
    @Email
    private String email;

    private String password;

    @NotNull
    private Role role;

    @NotNull
    private Boolean enabled;

    @NotNull
    private Boolean twoFactorEnabled;
}