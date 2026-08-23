package com.example.event.controller;
import com.example.event.model.Event;
import com.example.event.repository.EventRepository;
import com.example.event.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {
    private final EventService eventService;
    private final EventRepository eventRepository;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Event event) {
        if (event.getName() == null || event.getName().isBlank()) {
            return ResponseEntity.badRequest().body("Nome do evento é obrigatório");
        }
        if (event.getTotalTickets() == null || event.getTotalTickets() <= 0) {
            return ResponseEntity.badRequest().body("Quantidade de ingressos deve ser maior que zero");
        }
        if (event.getTicketPrice() == null || event.getTicketPrice() <= 0) {
            return ResponseEntity.badRequest().body("Preço do ingresso deve ser maior que zero");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.createEvent(event));
    }

    @GetMapping
    public ResponseEntity<List<Event>> listAll() {
        return ResponseEntity.ok(eventRepository.findAll());
    }
}