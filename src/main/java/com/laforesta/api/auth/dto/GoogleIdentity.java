package com.laforesta.api.auth.dto;

public record GoogleIdentity(
        String subject,
        String email,
        boolean emailVerified,
        String name
) {
}