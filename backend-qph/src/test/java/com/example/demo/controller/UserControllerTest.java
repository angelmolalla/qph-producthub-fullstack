package com.example.demo.controller;

import com.example.demo.dto.CreateUserRequest;
import com.example.demo.dto.UpdateUserRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.entity.Role;
import com.example.demo.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController controller;

    @Test
    void shouldReturnAllUsers() {
        List<UserResponse> users = List.of(response());
        when(userService.findAll()).thenReturn(users);

        ResponseEntity<List<UserResponse>> result = controller.findAll();

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(users);
    }

    @Test
    void shouldReturnUserById() {
        UserResponse user = response();
        when(userService.findById(1L)).thenReturn(user);

        ResponseEntity<UserResponse> result = controller.findById(1L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(user);
    }

    @Test
    void shouldCreateUser() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("angelo");
        request.setEmail("angelo@example.com");
        request.setPassword("Mono96mh");
        request.setRole(Role.USER);
        request.setTwoFactorEnabled(false);
        when(userService.create(request)).thenReturn(response());

        ResponseEntity<UserResponse> result = controller.create(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isEqualTo(response());
    }

    @Test
    void shouldUpdateUser() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setUsername("angelo");
        request.setEmail("angelo@example.com");
        request.setRole(Role.USER);
        request.setEnabled(true);
        request.setTwoFactorEnabled(true);
        when(userService.update(1L, request)).thenReturn(response());

        ResponseEntity<UserResponse> result = controller.update(1L, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(response());
    }

    @Test
    void shouldDeleteUser() {
        ResponseEntity<Void> result = controller.delete(1L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(userService).delete(1L);
    }

    private UserResponse response() {
        return UserResponse.builder()
                .id(1L)
                .username("angelo")
                .email("angelo@example.com")
                .role(Role.USER)
                .enabled(true)
                .twoFactorEnabled(false)
                .build();
    }
}
