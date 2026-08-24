package com.laforesta.api.auth.service;

import com.laforesta.api.auth.entity.EmailVerificationToken;
import com.laforesta.api.auth.repository.EmailVerificationTokenRepository;
import com.laforesta.api.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private static final int TOKEN_EXPIRY_HOURS = 24;

    private final EmailVerificationTokenRepository tokenRepository;

    @Transactional
    public String createVerificationToken(User user) {

        String rawToken = generateSecureToken();

        EmailVerificationToken token =
                new EmailVerificationToken();

        token.setUser(user);
        token.setTokenHash(hashToken(rawToken));
        token.setExpiresAt(
                OffsetDateTime.now().plusHours(TOKEN_EXPIRY_HOURS)
        );

        tokenRepository.save(token);

        return rawToken;
    }

    @Transactional
    public String resendVerificationToken(User user) {

        if (user.isEmailVerified()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Email is already verified"
            );
        }

        List<EmailVerificationToken> existingTokens =
                tokenRepository.findAllByUserAndUsedAtIsNull(user);

        OffsetDateTime now = OffsetDateTime.now();

        existingTokens.forEach(token ->
                token.setUsedAt(now)
        );

        return createVerificationToken(user);
    }

    @Transactional
    public void verify(String rawToken) {

        String tokenHash = hashToken(rawToken);

        EmailVerificationToken token =
                tokenRepository
                        .findByTokenHash(tokenHash)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Invalid verification token"
                                )
                        );

        if (token.getUsedAt() != null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Verification token has already been used"
            );
        }

        if (token.getExpiresAt()
                .isBefore(OffsetDateTime.now())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Verification token has expired"
            );
        }

        User user = token.getUser();

        user.setEmailVerified(true);

        token.setUsedAt(OffsetDateTime.now());
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
                    "Unable to hash verification token",
                    e
            );
        }
    }
}