package com.laforesta.api.notification.event;

public record EmailVerificationRequestedEvent(

        String email,
        String fullName,
        String verificationToken

) {
}