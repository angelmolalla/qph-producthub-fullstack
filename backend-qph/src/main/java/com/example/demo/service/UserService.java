package com.example.demo.service;
import com.example.demo.dto.*;
import java.util.List;

public interface UserService {

    List<UserResponse> findAll();

    UserResponse findById(Long id);

    UserResponse create(
            CreateUserRequest request
    );

    UserResponse update(
            Long id,
            UpdateUserRequest request
    );

    void delete(Long id);
}