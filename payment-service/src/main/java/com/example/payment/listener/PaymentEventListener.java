package com.example.payment.listener;

import com.example.payment.event.EventCreatedEvent;
import com.example.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventListener {

    private final PaymentService paymentService;

    @RabbitListener(queues = "${rabbitmq.queue.payment}")
    public void handleEventCreated(EventCreatedEvent event) {
        log.info("Payment Service recebeu EVENT_CREATED do evento ID: {}", event.getId());
        paymentService.setupEventFinancials(
                event.getId(),
                event.getName(),
                event.getTotalTickets(),
                event.getTicketPrice()
        );
    }
}