package com.laforesta.api.common.security;

import com.laforesta.api.auth.dto.GoogleIdentity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Component
public class GoogleTokenVerifier {

    private static final String GOOGLE_ISSUER =
            "https://accounts.google.com";

    private final JwtDecoder jwtDecoder;

    public GoogleTokenVerifier(
            @Value("${app.google.client-id}") String clientId
    ) {

        NimbusJwtDecoder decoder =
                JwtDecoders.fromIssuerLocation(GOOGLE_ISSUER);

        OAuth2TokenValidator<Jwt> issuerValidator =
                JwtValidators.createDefaultWithIssuer(
                        GOOGLE_ISSUER
                );

        OAuth2TokenValidator<Jwt> audienceValidator =
                new JwtClaimValidator<List<String>>(
                        "aud",
                        audience ->
                                audience != null &&
                                        audience.contains(clientId)
                );

        decoder.setJwtValidator(
                new DelegatingOAuth2TokenValidator<>(
                        issuerValidator,
                        audienceValidator
                )
        );

        this.jwtDecoder = decoder;
    }

    public GoogleIdentity verify(String idToken) {

        try {

            Jwt jwt = jwtDecoder.decode(idToken);

            String subject = jwt.getSubject();
            String email =
                    jwt.getClaimAsString("email");

            Boolean emailVerified =
                    jwt.getClaim("email_verified");

            String name =
                    jwt.getClaimAsString("name");

            if (subject == null || subject.isBlank()) {
                throw invalidToken();
            }

            if (email == null || email.isBlank()) {
                throw invalidToken();
            }

            return new GoogleIdentity(
                    subject,
                    email,
                    Boolean.TRUE.equals(emailVerified),
                    name
            );

        } catch (JwtException exception) {

            throw invalidToken();
        }
    }

    private ResponseStatusException invalidToken() {

        return new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Invalid Google credential"
        );
    }
}