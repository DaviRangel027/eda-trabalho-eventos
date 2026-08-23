package com.example.event.service;

import com.example.event.event.EventCreatedEvent;
import com.example.event.model.Event;
import com.example.event.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {

    private final EventRepository eventRepository;
    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;

    @Value("${rabbitmq.routing.created}")
    private String createdRoutingKey;

    @Transactional
    public Event createEvent(Event event) {
        event.setCreatedAt(LocalDateTime.now());
        Event saved = eventRepository.save(event);
        log.info("Evento criado com sucesso no banco: ID {}", saved.getId());

        EventCreatedEvent eventMsg = new EventCreatedEvent(
                saved.getId(),
                saved.getName(),
                saved.getTotalTickets(),
                saved.getTicketPrice()
        );

        rabbitTemplate.convertAndSend(exchangeName, createdRoutingKey, eventMsg);
        log.info("Mensagem EVENT_CREATED publicada no RabbitMQ para o evento ID {}", saved.getId());

        return saved;
    }
}