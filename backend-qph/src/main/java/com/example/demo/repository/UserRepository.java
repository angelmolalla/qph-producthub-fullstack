package com.example.demo.repository;

import com.example.demo.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository
        extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByUsername(
            String username
    );

    boolean existsByUsername(
            String username
    );

    boolean existsByEmail(
            String email
    );

    boolean existsByUsernameAndIdNot(
            String username,
            Long id
    );

    boolean existsByEmailAndIdNot(
            String email,
            Long id
    );
}