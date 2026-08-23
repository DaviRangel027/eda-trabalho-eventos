package com.example.payment.repository;

import com.example.payment.model.PaymentLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PaymentLedgerRepository extends JpaRepository<PaymentLedger, Long> {
    Optional<PaymentLedger> findByEventId(Long eventId);
}