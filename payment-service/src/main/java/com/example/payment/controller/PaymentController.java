package com.example.payment.controller;

import com.example.payment.model.PaymentLedger;
import com.example.payment.repository.PaymentLedgerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentLedgerRepository ledgerRepository;

    @GetMapping("/ledgers")
    public ResponseEntity<List<PaymentLedger>> getAllLedgers() {
        return ResponseEntity.ok(ledgerRepository.findAll());
    }

    @GetMapping("/ledgers/event/{eventId}")
    public ResponseEntity<PaymentLedger> getLedgerByEvent(@PathVariable Long eventId) {
        return ledgerRepository.findByEventId(eventId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}