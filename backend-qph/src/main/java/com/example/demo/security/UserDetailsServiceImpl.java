package com.example.demo.security;

import com.example.demo.entity.UserEntity;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        UserEntity userEntity = userRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Usuario no encontrado: " + username
                        )
                );

        return User.builder()
                .username(userEntity.getUsername())
                .password(userEntity.getPassword())
                .roles(userEntity.getRole().name())
                .disabled(!userEntity.getEnabled())
                .build();
    }
}