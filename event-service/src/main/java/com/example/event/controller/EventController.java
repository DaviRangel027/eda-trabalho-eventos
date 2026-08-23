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
    public ResponseEntity<Event> create(@RequestBody Event event) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.createEvent(event));
    }

    @GetMapping
    public ResponseEntity<List<Event>> listAll() {
        return ResponseEntity.ok(eventRepository.findAll());
    }
}