package com.laforesta.api.promo.service;

import com.laforesta.api.order.model.OrderStatus;
import com.laforesta.api.order.repository.OrderRepository;
import com.laforesta.api.promo.dto.PromoCalculationResult;
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
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PromoCodeService {

    private static final BigDecimal ONE_HUNDRED =
            new BigDecimal("100");

    private final PromoCodeRepository promoCodeRepository;
    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public PromoCalculationResult validateAndCalculate(
            String code,
            UUID eventId,
            UUID userId,
            BigDecimal subtotalAmount
    ) {

        if (code == null || code.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Promo code is required"
            );
        }

        if (eventId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Event is required"
            );
        }

        if (userId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "User is required"
            );
        }

        if (subtotalAmount == null
                || subtotalAmount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Subtotal amount must be greater than zero"
            );
        }

        PromoCode promoCode = promoCodeRepository
                .findByCodeIgnoreCase(
                        code.trim()
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Promo code not found"
                        )
                );

        validatePromo(
                promoCode,
                eventId,
                userId,
                subtotalAmount
        );

        BigDecimal normalizedSubtotal =
                subtotalAmount.setScale(
                        2,
                        RoundingMode.HALF_UP
                );

        BigDecimal discountAmount =
                calculateDiscount(
                        promoCode,
                        normalizedSubtotal
                );

        BigDecimal finalAmount =
                normalizedSubtotal
                        .subtract(discountAmount)
                        .max(BigDecimal.ZERO)
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        return new PromoCalculationResult(
                promoCode.getId(),
                promoCode.getCode(),
                promoCode.getDiscountType(),
                promoCode.getDiscountValue(),
                normalizedSubtotal,
                discountAmount,
                finalAmount
        );
    }

    private void validatePromo(
            PromoCode promoCode,
            UUID eventId,
            UUID userId,
            BigDecimal subtotalAmount
    ) {

        if (!promoCode.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Promo code is inactive"
            );
        }

        OffsetDateTime now =
                OffsetDateTime.now();

        if (promoCode.getValidFrom() != null
                && now.isBefore(
                promoCode.getValidFrom()
        )) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Promo code is not active yet"
            );
        }

        if (promoCode.getValidUntil() != null
                && now.isAfter(
                promoCode.getValidUntil()
        )) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Promo code has expired"
            );
        }

        if (promoCode.getEvent() != null
                && !promoCode.getEvent()
                .getId()
                .equals(eventId)) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Promo code is not valid for this event"
            );
        }

        if (promoCode.getMinimumOrderAmount() != null
                && subtotalAmount.compareTo(
                promoCode.getMinimumOrderAmount()
        ) < 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Minimum order amount has not been reached"
            );
        }

        validateDiscountConfiguration(
                promoCode
        );

        validateUsageLimit(
                promoCode
        );

        validatePerUserLimit(
                promoCode,
                userId
        );
    }

    private void validateUsageLimit(
            PromoCode promoCode
    ) {

        if (promoCode.getUsageLimit() == null) {
            return;
        }

        long totalUsage =
                orderRepository
                        .countByPromoCodeIdAndStatus(
                                promoCode.getId(),
                                OrderStatus.PAID
                        );

        if (totalUsage >=
                promoCode.getUsageLimit()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Promo code usage limit has been reached"
            );
        }
    }

    private void validatePerUserLimit(
            PromoCode promoCode,
            UUID userId
    ) {

        if (promoCode.getPerUserLimit() == null) {
            return;
        }

        long userUsage =
                orderRepository
                        .countByPromoCodeIdAndUserIdAndStatus(
                                promoCode.getId(),
                                userId,
                                OrderStatus.PAID
                        );

        if (userUsage >=
                promoCode.getPerUserLimit()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Promo code usage limit for this user has been reached"
            );
        }
    }

    private void validateDiscountConfiguration(
            PromoCode promoCode
    ) {

        BigDecimal value =
                promoCode.getDiscountValue();

        if (value == null
                || value.compareTo(
                BigDecimal.ZERO
        ) <= 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid promo discount value"
            );
        }

        if (promoCode.getDiscountType()
                == DiscountType.PERCENTAGE
                && value.compareTo(
                ONE_HUNDRED
        ) > 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Percentage discount cannot exceed 100"
            );
        }
    }

    private BigDecimal calculateDiscount(
            PromoCode promoCode,
            BigDecimal subtotalAmount
    ) {

        BigDecimal discount;

        if (promoCode.getDiscountType()
                == DiscountType.PERCENTAGE) {

            discount =
                    subtotalAmount
                            .multiply(
                                    promoCode.getDiscountValue()
                            )
                            .divide(
                                    ONE_HUNDRED,
                                    2,
                                    RoundingMode.HALF_UP
                            );

        } else if (promoCode.getDiscountType()
                == DiscountType.FIXED_AMOUNT) {

            discount =
                    promoCode.getDiscountValue();

        } else {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unsupported discount type"
            );
        }

        return discount
                .min(subtotalAmount)
                .setScale(
                        2,
                        RoundingMode.HALF_UP
                );
    }
}