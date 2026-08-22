package com.laforesta.api.auth.service;

import com.laforesta.api.auth.entity.RefreshToken;
import com.laforesta.api.auth.repository.RefreshTokenRepository;
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

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final int REFRESH_TOKEN_DAYS = 30;

    private final RefreshTokenRepository refreshTokenRepository;

    public String createRefreshToken(User user) {

        String rawToken = generateSecureToken();

        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setUser(user);
        refreshToken.setTokenHash(hashToken(rawToken));
        refreshToken.setExpiresAt(
                OffsetDateTime.now().plusDays(REFRESH_TOKEN_DAYS)
        );

        refreshTokenRepository.save(refreshToken);

        return rawToken;
    }

    @Transactional
    public User validateAndRotate(String rawToken) {

        String tokenHash = hashToken(rawToken);

        RefreshToken token = refreshTokenRepository
                .findByTokenHash(tokenHash)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid refresh token"
                ));

        if (token.getRevokedAt() != null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Refresh token has been revoked"
            );
        }

        if (token.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Refresh token has expired"
            );
        }

        token.setRevokedAt(OffsetDateTime.now());

        return token.getUser();
    }

    @Transactional
    public void revoke(String rawToken) {

        String tokenHash = hashToken(rawToken);

        refreshTokenRepository
                .findByTokenHash(tokenHash)
                .ifPresent(token -> {
                    if (token.getRevokedAt() == null) {
                        token.setRevokedAt(OffsetDateTime.now());
                    }
                });
    }

    private String generateSecureToken() {

        byte[] bytes = new byte[64];

        new SecureRandom().nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    private String hashToken(String token) {

        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Unable to hash refresh token",
                    e
            );
        }
    }
}