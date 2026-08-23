package com.example.ticket.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventCreatedEvent implements Serializable {
    private String eventId;
    private String eventType;
    private Long id;
    private String name;
    private Integer totalTickets;
    private Double ticketPrice;
    private LocalDateTime timestamp;
}