package com.laforesta.api.auth.repository;

import com.laforesta.api.auth.entity.AuthProviderAccount;
import com.laforesta.api.auth.model.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AuthProviderAccountRepository
        extends JpaRepository<AuthProviderAccount, UUID> {

    Optional<AuthProviderAccount>
    findByProviderAndProviderUserId(
            AuthProvider provider,
            String providerUserId
    );
}