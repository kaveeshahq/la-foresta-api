package com.laforesta.api.auth.service;

import com.laforesta.api.auth.entity.PasswordResetToken;
import com.laforesta.api.auth.repository.PasswordResetTokenRepository;
import com.laforesta.api.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final int TOKEN_EXPIRY_MINUTES = 30;

    private final PasswordResetTokenRepository tokenRepository;

    @Transactional
    public String createResetToken(User user) {

        List<PasswordResetToken> existingTokens =
                tokenRepository.findAllByUserAndUsedAtIsNull(user);

        OffsetDateTime now = OffsetDateTime.now();

        existingTokens.forEach(token ->
                token.setUsedAt(now)
        );

        String rawToken = generateSecureToken();

        PasswordResetToken token =
                new PasswordResetToken();

        token.setUser(user);
        token.setTokenHash(hashToken(rawToken));
        token.setExpiresAt(
                now.plusMinutes(TOKEN_EXPIRY_MINUTES)
        );

        tokenRepository.save(token);

        return rawToken;
    }

    @Transactional
    public User validateAndConsume(String rawToken) {

        String tokenHash = hashToken(rawToken);

        PasswordResetToken token =
                tokenRepository
                        .findByTokenHash(tokenHash)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Invalid reset token"
                                )
                        );

        if (token.getUsedAt() != null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Reset token has already been used"
            );
        }

        if (token.getExpiresAt()
                .isBefore(OffsetDateTime.now())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Reset token has expired"
            );
        }

        token.setUsedAt(OffsetDateTime.now());

        return token.getUser();
    }

    private String generateSecureToken() {

        byte[] bytes = new byte[32];

        new SecureRandom().nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    private String hashToken(String token) {

        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash =
                    digest.digest(
                            token.getBytes(StandardCharsets.UTF_8)
                    );

            return HexFormat.of().formatHex(hash);

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Unable to hash reset token",
                    e
            );
        }
    }
}