package com.laforesta.api.notification.event;

public record PasswordResetRequestedEvent(

        String email,
        String fullName,
        String resetToken

) {
}