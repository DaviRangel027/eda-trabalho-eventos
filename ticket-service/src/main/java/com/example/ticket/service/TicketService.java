package com.example.ticket.service;

import com.example.ticket.event.TicketPurchasedEvent;
import com.example.ticket.model.Ticket;
import com.example.ticket.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketService {

    private final TicketRepository ticketRepository;
    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.payment}")
    private String paymentExchangeName;
    @Value("${rabbitmq.routing.purchased}")
    private String purchasedRoutingKey;

    @Transactional
    public void generateTicketsForEvent(Long eventId, String eventName, Integer totalTickets, Double price) {
        log.info("Iniciando geração de {} ingressos para o evento ID: {}", totalTickets, eventId);

        for (int i = 1; i <= totalTickets; i++) {
            Ticket ticket = new Ticket();
            ticket.setEventId(eventId);
            ticket.setEventName(eventName);
            ticket.setTicketCode("TCK-" + eventId + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            ticket.setPrice(price);
            ticket.setStatus("AVAILABLE");
            ticket.setGeneratedAt(LocalDateTime.now());

            ticketRepository.save(ticket);
        }

        log.info("Sucesso: {} ingressos gerados e persistidos no banco de dados.", totalTickets);
    }

    @Transactional
    public Ticket purchaseTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ingresso não encontrado: " + ticketId));

        if (!"AVAILABLE".equals(ticket.getStatus())) {
            throw new IllegalStateException("Ingresso não está disponível para compra (status atual: " + ticket.getStatus() + ")");
        }

        ticket.setStatus("SOLD");
        Ticket saved = ticketRepository.save(ticket);
        log.info("Ingresso {} vendido com sucesso.", saved.getTicketCode());

        TicketPurchasedEvent event = new TicketPurchasedEvent(
                saved.getId(),
                saved.getEventId(),
                saved.getEventName(),
                saved.getTicketCode(),
                saved.getPrice()
        );

        rabbitTemplate.convertAndSend(paymentExchangeName, purchasedRoutingKey, event);
        log.info("Evento TICKET_PURCHASED publicado para o ingresso {}", saved.getTicketCode());

        return saved;
    }
}