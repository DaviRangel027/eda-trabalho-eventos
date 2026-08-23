package com.example.payment.listener;

import com.example.payment.event.TicketPurchasedEvent;
import com.example.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TicketPurchasedEventListener {

    private final PaymentService paymentService;

    @RabbitListener(queues = "${rabbitmq.queue.purchase}")
    public void handleTicketPurchased(TicketPurchasedEvent event) {
        log.info("Payment Service recebeu TICKET_PURCHASED: ingresso {} do evento {}", event.getTicketCode(), event.getEventId());
        paymentService.confirmPayment(event.getEventId(), event.getPrice());
    }
}