package com.example.event.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventCreatedEvent implements Serializable {
    private String eventId = UUID.randomUUID().toString();
    private String eventType = "EVENT_CREATED";
    private Long id;
    private String name;
    private Integer totalTickets;
    private Double ticketPrice;
    private LocalDateTime timestamp = LocalDateTime.now();

    public EventCreatedEvent(Long id, String name, Integer totalTickets, Double ticketPrice) {
        this();
        this.id = id;
        this.name = name;
        this.totalTickets = totalTickets;
        this.ticketPrice = ticketPrice;
    }
}