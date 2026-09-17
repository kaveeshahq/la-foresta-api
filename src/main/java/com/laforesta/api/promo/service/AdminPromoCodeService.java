package com.laforesta.api.promo.service;

import com.laforesta.api.event.entity.Event;
import com.laforesta.api.event.repository.EventRepository;
import com.laforesta.api.order.model.OrderStatus;
import com.laforesta.api.order.repository.OrderRepository;
import com.laforesta.api.promo.dto.AdminPromoCodeResponse;
import com.laforesta.api.promo.dto.PromoCodeStatusRequest;
import com.laforesta.api.promo.dto.PromoCodeUpsertRequest;
import com.laforesta.api.promo.entity.PromoCode;
import com.laforesta.api.promo.model.DiscountType;
import com.laforesta.api.promo.repository.PromoCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminPromoCodeService {

    private static final BigDecimal ONE_HUNDRED =
            new BigDecimal("100.00");

    private final PromoCodeRepository promoCodeRepository;
    private final EventRepository eventRepository;
    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public List<AdminPromoCodeResponse> getPromoCodes() {

        return promoCodeRepository
                .findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminPromoCodeResponse getPromoCode(
            UUID promoCodeId
    ) {

        return toResponse(
                findPromoCode(promoCodeId)
        );
    }

    @Transactional
    public AdminPromoCodeResponse createPromoCode(
            PromoCodeUpsertRequest request
    ) {

        String normalizedCode =
                normalizeCode(request.code());

        validateCodeAvailable(
                normalizedCode,
                null
        );

        validateConfiguration(request);

        PromoCode promoCode = new PromoCode();

        applyRequest(
                promoCode,
                request,
                normalizedCode
        );

        PromoCode savedPromoCode =
                promoCodeRepository.save(promoCode);

        return toResponse(savedPromoCode);
    }

    @Transactional
    public AdminPromoCodeResponse updatePromoCode(
            UUID promoCodeId,
            PromoCodeUpsertRequest request
    ) {

        PromoCode promoCode =
                findPromoCode(promoCodeId);

        String normalizedCode =
                normalizeCode(request.code());

        validateCodeAvailable(
                normalizedCode,
                promoCodeId
        );

        validateConfiguration(request);

        applyRequest(
                promoCode,
                request,
                normalizedCode
        );

        return toResponse(promoCode);
    }

    @Transactional
    public AdminPromoCodeResponse updateStatus(
            UUID promoCodeId,
            PromoCodeStatusRequest request
    ) {

        PromoCode promoCode =
                findPromoCode(promoCodeId);

        promoCode.setActive(request.active());

        return toResponse(promoCode);
    }

    private void applyRequest(
            PromoCode promoCode,
            PromoCodeUpsertRequest request,
            String normalizedCode
    ) {

        promoCode.setEvent(
                resolveEvent(request.eventId())
        );

        promoCode.setCode(normalizedCode);

        promoCode.setDiscountType(
                request.discountType()
        );

        promoCode.setDiscountValue(
                normalizeMoney(request.discountValue())
        );

        promoCode.setMinimumOrderAmount(
                normalizeNullableMoney(
                        request.minimumOrderAmount()
                )
        );

        promoCode.setUsageLimit(
                request.usageLimit()
        );

        promoCode.setPerUserLimit(
                request.perUserLimit()
        );

        promoCode.setValidFrom(
                request.validFrom()
        );

        promoCode.setValidUntil(
                request.validUntil()
        );

        promoCode.setActive(
                request.active()
        );
    }

    private Event resolveEvent(
            UUID eventId
    ) {

        if (eventId == null) {
            return null;
        }

        return eventRepository
                .findById(eventId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Event not found"
                        )
                );
    }

    private PromoCode findPromoCode(
            UUID promoCodeId
    ) {

        return promoCodeRepository
                .findById(promoCodeId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Promo code not found"
                        )
                );
    }

    private void validateCodeAvailable(
            String code,
            UUID excludedPromoCodeId
    ) {

        promoCodeRepository
                .findByCodeIgnoreCase(code)
                .ifPresent(existingPromoCode -> {

                    boolean belongsToAnotherPromo =
                            excludedPromoCodeId == null
                                    || !existingPromoCode
                                    .getId()
                                    .equals(excludedPromoCodeId);

                    if (belongsToAnotherPromo) {
                        throw new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "Promo code already exists"
                        );
                    }
                });
    }

    private void validateConfiguration(
            PromoCodeUpsertRequest request
    ) {

        if (request.discountType()
                == DiscountType.PERCENTAGE
                && request.discountValue()
                .compareTo(ONE_HUNDRED) > 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Percentage discount cannot exceed 100"
            );
        }

        if (request.validFrom() != null
                && request.validUntil() != null
                && !request.validUntil()
                .isAfter(request.validFrom())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Valid-until date must be after valid-from date"
            );
        }
    }

    private String normalizeCode(
            String code
    ) {

        return code
                .trim()
                .toUpperCase(Locale.ROOT);
    }

    private BigDecimal normalizeMoney(
            BigDecimal value
    ) {

        return value.setScale(
                2,
                RoundingMode.HALF_UP
        );
    }

    private BigDecimal normalizeNullableMoney(
            BigDecimal value
    ) {

        return value == null
                ? null
                : normalizeMoney(value);
    }

    private AdminPromoCodeResponse toResponse(
            PromoCode promoCode
    ) {

        Event event = promoCode.getEvent();

        long redemptionCount =
                orderRepository
                        .countByPromoCodeIdAndStatus(
                                promoCode.getId(),
                                OrderStatus.PAID
                        );

        Long remainingUses = null;

        if (promoCode.getUsageLimit() != null) {
            remainingUses = Math.max(
                    (long) promoCode.getUsageLimit()
                            - redemptionCount,
                    0L
            );
        }

        return new AdminPromoCodeResponse(
                promoCode.getId(),

                event == null
                        ? null
                        : event.getId(),

                event == null
                        ? null
                        : event.getTitle(),

                promoCode.getCode(),
                promoCode.getDiscountType(),
                promoCode.getDiscountValue(),
                promoCode.getMinimumOrderAmount(),

                promoCode.getUsageLimit(),
                promoCode.getPerUserLimit(),

                redemptionCount,
                remainingUses,

                promoCode.getValidFrom(),
                promoCode.getValidUntil(),

                promoCode.isActive(),

                promoCode.getCreatedAt(),
                promoCode.getUpdatedAt()
        );
    }
}