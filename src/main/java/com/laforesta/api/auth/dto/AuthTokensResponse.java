package com.laforesta.api.auth.dto;

public record AuthTokensResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long accessTokenExpiresIn
) {
}