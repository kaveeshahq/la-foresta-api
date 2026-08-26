package com.laforesta.api.payment.repository;

import com.laforesta.api.payment.entity.PaymentTransaction;
import com.laforesta.api.payment.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentTransactionRepository
        extends JpaRepository<PaymentTransaction, UUID> {

    List<PaymentTransaction>
    findAllByOrderIdOrderByCreatedAtDesc(
            UUID orderId
    );

    Optional<PaymentTransaction>
    findFirstByOrderIdAndStatusOrderByCreatedAtDesc(
            UUID orderId,
            PaymentStatus status
    );

    List<PaymentTransaction>
    findAllByOrderIdAndStatus(
            UUID orderId,
            PaymentStatus status
    );
}