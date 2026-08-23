package com.example.ticket.controller;

import com.example.ticket.model.Ticket;
import com.example.ticket.repository.TicketRepository;
import com.example.ticket.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketRepository ticketRepository;
    private final TicketService ticketService;

    @GetMapping
    public ResponseEntity<List<Ticket>> getAllTickets() {
        return ResponseEntity.ok(ticketRepository.findAll());
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<Ticket>> getTicketsByEvent(@PathVariable Long eventId) {
        return ResponseEntity.ok(ticketRepository.findByEventId(eventId));
    }

    @PostMapping("/{id}/purchase")
    public ResponseEntity<?> purchase(@PathVariable Long id) {
        try {
            Ticket ticket = ticketService.purchaseTicket(id);
            return ResponseEntity.ok(ticket);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }
}