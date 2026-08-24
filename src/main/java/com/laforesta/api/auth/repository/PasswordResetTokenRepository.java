package com.laforesta.api.auth.repository;

import com.laforesta.api.auth.entity.PasswordResetToken;
import com.laforesta.api.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository
        extends JpaRepository<PasswordResetToken, UUID> {

    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    List<PasswordResetToken> findAllByUserAndUsedAtIsNull(User user);
}