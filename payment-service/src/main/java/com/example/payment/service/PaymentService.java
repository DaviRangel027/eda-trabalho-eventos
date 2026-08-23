package com.example.payment.service;

import com.example.payment.model.PaymentLedger;
import com.example.payment.repository.PaymentLedgerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentLedgerRepository ledgerRepository;

    @Transactional
    public void setupEventFinancials(Long eventId, String eventName, Integer totalTickets, Double ticketPrice) {
        Double expectedRevenue = totalTickets * ticketPrice;
        log.info("Inicializando controle financeiro para o evento ID {}. Receita estimada: R$ {}", eventId, expectedRevenue);

        PaymentLedger ledger = new PaymentLedger();
        ledger.setEventId(eventId);
        ledger.setEventName(eventName);
        ledger.setExpectedRevenue(expectedRevenue);
        ledger.setTotalCollected(0.0);
        ledger.setStatus("OPEN");
        ledger.setCreatedAt(LocalDateTime.now());

        ledgerRepository.save(ledger);
        log.info("Livro contábil registrado para o evento ID {}", eventId);
    }

    @Transactional
    public void confirmPayment(Long eventId, Double amount) {
        PaymentLedger ledger = ledgerRepository.findByEventId(eventId)
                .orElseThrow(() -> new IllegalStateException("Ledger não encontrado para o evento ID " + eventId));

        ledger.setTotalCollected(ledger.getTotalCollected() + amount);

        if (ledger.getTotalCollected() >= ledger.getExpectedRevenue()) {
            ledger.setStatus("CLOSED");
            log.info("Livro contábil do evento {} atingiu a receita esperada e foi fechado.", eventId);
        }

        ledgerRepository.save(ledger);
        log.info("Pagamento confirmado. Total arrecadado no evento {}: R$ {}", eventId, ledger.getTotalCollected());
    }
}