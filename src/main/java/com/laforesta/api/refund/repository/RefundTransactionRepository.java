package com.laforesta.api.refund.repository;

import com.laforesta.api.refund.entity.RefundTransaction;
import com.laforesta.api.refund.model.RefundStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefundTransactionRepository
        extends JpaRepository<RefundTransaction, UUID> {

    List<RefundTransaction>
    findAllByOrderIdOrderByCreatedAtDesc(
            UUID orderId
    );

    Optional<RefundTransaction>
    findFirstByOrderIdAndStatusOrderByCreatedAtDesc(
            UUID orderId,
            RefundStatus status
    );
}