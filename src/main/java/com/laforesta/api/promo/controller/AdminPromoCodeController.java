package com.laforesta.api.promo.controller;

import com.laforesta.api.promo.dto.AdminPromoCodeResponse;
import com.laforesta.api.promo.dto.PromoCodeStatusRequest;
import com.laforesta.api.promo.dto.PromoCodeUpsertRequest;
import com.laforesta.api.promo.service.AdminPromoCodeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/promo-codes")
@RequiredArgsConstructor
public class AdminPromoCodeController {

    private final AdminPromoCodeService promoCodeService;

    @PreAuthorize(
            "hasAnyRole('EVENT_MANAGER', 'ADMIN', 'SUPER_ADMIN')"
    )
    @GetMapping
    public ResponseEntity<List<AdminPromoCodeResponse>>
    getPromoCodes() {

        return ResponseEntity.ok(
                promoCodeService.getPromoCodes()
        );
    }

    @PreAuthorize(
            "hasAnyRole('EVENT_MANAGER', 'ADMIN', 'SUPER_ADMIN')"
    )
    @GetMapping("/{promoCodeId}")
    public ResponseEntity<AdminPromoCodeResponse>
    getPromoCode(
            @PathVariable UUID promoCodeId
    ) {

        return ResponseEntity.ok(
                promoCodeService.getPromoCode(
                        promoCodeId
                )
        );
    }

    @PreAuthorize(
            "hasAnyRole('EVENT_MANAGER', 'ADMIN', 'SUPER_ADMIN')"
    )
    @PostMapping
    public ResponseEntity<AdminPromoCodeResponse>
    createPromoCode(
            @Valid
            @RequestBody
            PromoCodeUpsertRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        promoCodeService
                                .createPromoCode(request)
                );
    }

    @PreAuthorize(
            "hasAnyRole('EVENT_MANAGER', 'ADMIN', 'SUPER_ADMIN')"
    )
    @PutMapping("/{promoCodeId}")
    public ResponseEntity<AdminPromoCodeResponse>
    updatePromoCode(
            @PathVariable UUID promoCodeId,
            @Valid
            @RequestBody
            PromoCodeUpsertRequest request
    ) {

        return ResponseEntity.ok(
                promoCodeService.updatePromoCode(
                        promoCodeId,
                        request
                )
        );
    }

    @PreAuthorize(
            "hasAnyRole('EVENT_MANAGER', 'ADMIN', 'SUPER_ADMIN')"
    )
    @PatchMapping("/{promoCodeId}/status")
    public ResponseEntity<AdminPromoCodeResponse>
    updatePromoCodeStatus(
            @PathVariable UUID promoCodeId,
            @Valid
            @RequestBody
            PromoCodeStatusRequest request
    ) {

        return ResponseEntity.ok(
                promoCodeService.updateStatus(
                        promoCodeId,
                        request
                )
        );
    }
}