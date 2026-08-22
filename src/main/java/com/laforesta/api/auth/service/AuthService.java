package com.laforesta.api.auth.service;

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

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

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

        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getFullName(),
                savedUser.getEmail(),
                savedUser.isEmailVerified()
        );
    }
}