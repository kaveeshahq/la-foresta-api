package com.laforesta.api.promo.repository;

import com.laforesta.api.promo.entity.PromoCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PromoCodeRepository
        extends JpaRepository<PromoCode, UUID> {

    Optional<PromoCode> findByCodeIgnoreCase(
            String code
    );

    boolean existsByCodeIgnoreCase(
            String code
    );
}