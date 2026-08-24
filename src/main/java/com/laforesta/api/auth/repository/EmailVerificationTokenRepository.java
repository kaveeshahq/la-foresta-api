package com.laforesta.api.auth.repository;

import com.laforesta.api.auth.entity.EmailVerificationToken;
import com.laforesta.api.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmailVerificationTokenRepository
        extends JpaRepository<EmailVerificationToken, UUID> {

    Optional<EmailVerificationToken> findByTokenHash(String tokenHash);

    List<EmailVerificationToken> findAllByUserAndUsedAtIsNull(User user);
}