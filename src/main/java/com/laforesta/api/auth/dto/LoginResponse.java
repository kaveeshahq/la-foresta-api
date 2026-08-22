package com.laforesta.api.auth.dto;

import java.util.List;
import java.util.UUID;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        UUID userId,
        String fullName,
        String email,
        List<String> roles
) {
}