package com.laforesta.api.notification.service;

public interface EmailService {

    void sendEmailVerification(
            String to,
            String fullName,
            String verificationToken
    );

    void sendPasswordReset(
            String to,
            String fullName,
            String resetToken
    );

}