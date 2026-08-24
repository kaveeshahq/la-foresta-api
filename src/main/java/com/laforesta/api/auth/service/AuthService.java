package com.laforesta.api.auth.service;

import com.laforesta.api.auth.dto.AuthTokensResponse;
import com.laforesta.api.auth.dto.LoginRequest;
import com.laforesta.api.auth.dto.RefreshTokenRequest;
import com.laforesta.api.auth.dto.RegisterRequest;
import com.laforesta.api.auth.dto.RegisterResponse;
import com.laforesta.api.user.entity.Role;
import com.laforesta.api.user.entity.User;
import com.laforesta.api.user.model.AccountStatus;
import com.laforesta.api.user.model.RoleName;
import com.laforesta.api.user.repository.RoleRepository;
import com.laforesta.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.laforesta.api.auth.dto.ResendVerificationRequest;
import java.util.Locale;
import com.laforesta.api.notification.service.EmailService;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailVerificationService emailVerificationService;
    private final EmailService emailService;



    @Transactional
    public RegisterResponse register(RegisterRequest request) {

        String email = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        String fullName = request.fullName().trim();

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "An account with this email already exists"
            );
        }

        Role customerRole = roleRepository
                .findByName(RoleName.CUSTOMER)
                .orElseThrow(() -> new IllegalStateException(
                        "CUSTOMER role is not configured"
                ));

        User user = new User();

        user.setEmail(email);
        user.setFullName(fullName);
        user.setPasswordHash(
                passwordEncoder.encode(request.password())
        );
        user.setEmailVerified(false);
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.getRoles().add(customerRole);

        User savedUser = userRepository.save(user);
        String verificationToken =
                emailVerificationService
                        .createVerificationToken(savedUser);

        emailService.sendEmailVerification(
                savedUser.getEmail(),
                savedUser.getFullName(),
                verificationToken
        );

        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getFullName(),
                savedUser.getEmail(),
                savedUser.isEmailVerified()
        );
    }

    @Transactional
    public AuthTokensResponse login(LoginRequest request) {

        String email = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        User user = userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid email or password"
                ));

        if (user.getPasswordHash() == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid email or password"
            );
        }

        if (!passwordEncoder.matches(
                request.password(),
                user.getPasswordHash()
        )) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid email or password"
            );
        }

        if (user.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "This account is not active"
            );
        }

        String accessToken =
                jwtService.generateAccessToken(user);

        String refreshToken =
                refreshTokenService.createRefreshToken(user);

        return new AuthTokensResponse(
                accessToken,
                refreshToken,
                "Bearer",
                900
        );
    }

    @Transactional
    public void resendVerification(
            ResendVerificationRequest request
    ) {

        String email = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        User user = userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Account not found"
                ));

        String verificationToken =
                emailVerificationService
                        .resendVerificationToken(user);

        emailService.sendEmailVerification(
                user.getEmail(),
                user.getFullName(),
                verificationToken
        );
    }

    @Transactional
    public AuthTokensResponse refresh(
            RefreshTokenRequest request
    ) {

        User user = refreshTokenService
                .validateAndRotate(request.refreshToken());

        String newAccessToken =
                jwtService.generateAccessToken(user);

        String newRefreshToken =
                refreshTokenService.createRefreshToken(user);

        return new AuthTokensResponse(
                newAccessToken,
                newRefreshToken,
                "Bearer",
                900
        );
    }

    @Transactional
    public void logout(
            RefreshTokenRequest request
    ) {

        refreshTokenService.revoke(
                request.refreshToken()
        );
    }
}