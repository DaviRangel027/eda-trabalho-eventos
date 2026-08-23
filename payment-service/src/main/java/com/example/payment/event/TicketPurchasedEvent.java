package com.example.payment.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketPurchasedEvent {
    private Long ticketId;
    private Long eventId;
    private String eventName;
    private String ticketCode;
    private Double price;
}