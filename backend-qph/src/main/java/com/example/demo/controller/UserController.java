package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserResponse>>
    findAll() {
        return ResponseEntity.ok(
                userService.findAll()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse>
    findById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                userService.findById(id)
        );
    }

    @PostMapping
    public ResponseEntity<UserResponse>
    create(
            @Valid
            @RequestBody
            CreateUserRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        userService.create(request)
                );
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse>
    update(
            @PathVariable Long id,

            @Valid
            @RequestBody
            UpdateUserRequest request) {

        return ResponseEntity.ok(
                userService.update(
                        id,
                        request
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void>
    delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent()
                .build();
    }
}