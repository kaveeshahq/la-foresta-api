package com.laforesta.api.auth.service;

import com.laforesta.api.auth.dto.AuthTokensResponse;
import com.laforesta.api.auth.dto.GoogleIdentity;
import com.laforesta.api.auth.entity.AuthProviderAccount;
import com.laforesta.api.auth.model.AuthProvider;
import com.laforesta.api.auth.repository.AuthProviderAccountRepository;
import com.laforesta.api.common.security.GoogleTokenVerifier;
import com.laforesta.api.user.entity.Role;
import com.laforesta.api.user.entity.User;
import com.laforesta.api.user.model.AccountStatus;
import com.laforesta.api.user.model.RoleName;
import com.laforesta.api.user.repository.RoleRepository;
import com.laforesta.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class GoogleAuthService {

    private final GoogleTokenVerifier googleTokenVerifier;
    private final AuthProviderAccountRepository authProviderAccountRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public AuthTokensResponse loginWithGoogle(String credential) {

        GoogleIdentity identity =
                googleTokenVerifier.verify(credential);

        if (!identity.emailVerified()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Google email is not verified"
            );
        }

        User user = authProviderAccountRepository
                .findByProviderAndProviderUserId(
                        AuthProvider.GOOGLE,
                        identity.subject()
                )
                .map(AuthProviderAccount::getUser)
                .orElseGet(() -> createGoogleUser(identity));

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

    private User createGoogleUser(GoogleIdentity identity) {

        String email = identity.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "An account with this email already exists. Sign in with your existing account first."
            );
        }

        Role customerRole = roleRepository
                .findByName(RoleName.CUSTOMER)
                .orElseThrow(() -> new IllegalStateException(
                        "CUSTOMER role is not configured"
                ));

        User user = new User();

        user.setEmail(email);

        String name = identity.name();

        user.setFullName(
                name == null || name.isBlank()
                        ? email
                        : name.trim()
        );

        user.setPasswordHash(null);
        user.setEmailVerified(true);
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.getRoles().add(customerRole);

        User savedUser =
                userRepository.save(user);

        AuthProviderAccount providerAccount =
                new AuthProviderAccount();

        providerAccount.setUser(savedUser);
        providerAccount.setProvider(AuthProvider.GOOGLE);
        providerAccount.setProviderUserId(identity.subject());

        authProviderAccountRepository.save(providerAccount);

        return savedUser;
    }
}