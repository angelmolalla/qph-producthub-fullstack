package com.example.demo.dto;

import com.example.demo.entity.Role;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;

    private String username;

    private String email;

    private Role role;

    private Boolean enabled;

    private Boolean twoFactorEnabled;
}