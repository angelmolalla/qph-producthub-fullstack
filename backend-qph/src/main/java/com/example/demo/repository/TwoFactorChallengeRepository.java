package com.example.demo.repository;

import com.example.demo.entity.TwoFactorChallenge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TwoFactorChallengeRepository
        extends JpaRepository<TwoFactorChallenge, UUID> {

    void deleteByUser_Id(Long userId);
}