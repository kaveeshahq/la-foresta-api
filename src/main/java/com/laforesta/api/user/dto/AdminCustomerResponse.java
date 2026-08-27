package com.laforesta.api.user.dto;

import com.laforesta.api.user.model.AccountStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AdminCustomerResponse(

        UUID userId,
        String email,
        String fullName,
        boolean emailVerified,
        AccountStatus accountStatus,
        OffsetDateTime createdAt

) {
}