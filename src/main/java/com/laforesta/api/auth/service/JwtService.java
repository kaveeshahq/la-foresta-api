package com.laforesta.api.auth.service;

import com.laforesta.api.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtEncoder jwtEncoder;

    public String generateAccessToken(User user) {

        Instant now = Instant.now();

        List<String> roles = user.getRoles()
                .stream()
                .map(role -> role.getName().name())
                .toList();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("laforesta-api")
                .subject(user.getId().toString())
                .issuedAt(now)
                .expiresAt(now.plus(15, ChronoUnit.MINUTES))
                .claim("email", user.getEmail())
                .claim("roles", roles)
                .claim("tokenType", "access")
                .build();

        JwsHeader header = JwsHeader
                .with(SignatureAlgorithm.RS256)
                .build();

        return jwtEncoder
                .encode(
                        JwtEncoderParameters.from(header, claims)
                )
                .getTokenValue();
    }
}