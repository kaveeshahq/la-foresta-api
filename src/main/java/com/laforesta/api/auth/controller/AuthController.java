package com.laforesta.api.auth.controller;

import com.laforesta.api.auth.dto.AuthTokensResponse;
import com.laforesta.api.auth.dto.LoginRequest;
import com.laforesta.api.auth.dto.GoogleLoginRequest;
import com.laforesta.api.auth.service.GoogleAuthService;
import com.laforesta.api.auth.dto.RefreshTokenRequest;
import com.laforesta.api.auth.dto.RegisterRequest;
import com.laforesta.api.auth.dto.RegisterResponse;
import com.laforesta.api.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.laforesta.api.auth.dto.VerifyEmailRequest;
import com.laforesta.api.auth.service.EmailVerificationService;
import com.laforesta.api.auth.dto.ResendVerificationRequest;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final GoogleAuthService googleAuthService;
    private final EmailVerificationService emailVerificationService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {

        RegisterResponse response =
                authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthTokensResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {

        AuthTokensResponse response =
                authService.login(request);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthTokensResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request
    ) {

        return ResponseEntity.ok(
                authService.refresh(request)
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @Valid @RequestBody RefreshTokenRequest request
    ) {

        authService.logout(request);

        return ResponseEntity
                .noContent()
                .build();
    }

    @PostMapping("/google")
    public ResponseEntity<AuthTokensResponse> googleLogin(
            @Valid @RequestBody GoogleLoginRequest request
    ) {

        AuthTokensResponse response =
                googleAuthService.loginWithGoogle(
                        request.credential()
                );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-email")
    public ResponseEntity<Void> verifyEmail(
            @Valid @RequestBody VerifyEmailRequest request
    ) {

        emailVerificationService.verify(
                request.token()
        );

        return ResponseEntity
                .noContent()
                .build();
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<Void> resendVerification(
            @Valid @RequestBody ResendVerificationRequest request
    ) {

        authService.resendVerification(request);

        return ResponseEntity.noContent().build();
    }
}