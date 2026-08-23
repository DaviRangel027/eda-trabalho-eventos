package com.example.ticket.listener;

import com.example.ticket.event.EventCreatedEvent;
import com.example.ticket.service.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TicketEventListener {

    private final TicketService ticketService;

    @RabbitListener(queues = "${rabbitmq.queue.ticket}")
    public void handleEventCreated(EventCreatedEvent event) {
        log.info("Ticket Service recebeu EVENT_CREATED do evento ID: {}", event.getId());
        ticketService.generateTicketsForEvent(
                event.getId(),
                event.getName(),
                event.getTotalTickets(),
                event.getTicketPrice()
        );
    }
}