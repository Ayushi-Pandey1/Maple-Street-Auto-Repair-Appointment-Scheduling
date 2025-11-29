package com.maple.appointment.controller;

import com.maple.appointment.dto.AppointmentCreateDto;
import com.maple.appointment.dto.AppointmentDto;
import com.maple.appointment.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@CrossOrigin(origins = "http://localhost:5173")
public class AppointmentController {

    private final AppointmentService service;

    public AppointmentController(AppointmentService service) {
        this.service = service;
    }

    @GetMapping
    public List<AppointmentDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppointmentDto> get(@PathVariable Long id) {
        AppointmentDto dto = service.get(id);
        if (dto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<AppointmentDto> create(@Valid @RequestBody AppointmentCreateDto dto) {
        System.out.println("👉 CREATE endpoint called with dto = " + dto);
        try {
            AppointmentDto created = service.create(dto);
            System.out.println("✅ Created appointment id=" + created.getId());
            return ResponseEntity.created(URI.create("/api/appointments/" + created.getId())).body(created);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(400).body(null);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<AppointmentDto> update(@PathVariable Long id, @Valid @RequestBody AppointmentCreateDto dto) {
        AppointmentDto updated = service.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelAppointment(@PathVariable("id") Long id) {
        try {
            service.cancel(id);
            return ResponseEntity.ok("Cancelled");
        } catch (Exception e) {
            return ResponseEntity.status(400).body("Cancel failed: " + e.getMessage());
        }
    }
}
