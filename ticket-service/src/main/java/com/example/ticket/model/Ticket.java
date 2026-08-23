package com.example.ticket.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long eventId;
    private String eventName;
    private String ticketCode;
    private Double price;
    private String status; // AVAILABLE, SOLD, CANCELLED
    private LocalDateTime generatedAt;
}