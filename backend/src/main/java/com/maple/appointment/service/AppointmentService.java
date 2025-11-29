package com.maple.appointment.service;

import com.maple.appointment.dto.AppointmentCreateDto;
import com.maple.appointment.dto.AppointmentDto;
import com.maple.appointment.entity.Appointment;
import com.maple.appointment.exception.OverlapException;
import com.maple.appointment.repository.AppointmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    private final AppointmentRepository repository;

    public AppointmentService(AppointmentRepository repository) {
        this.repository = repository;
    }

    public List<AppointmentDto> listAll() {
        return repository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    public AppointmentDto get(Long id) {
        return repository.findById(id).map(this::toDto).orElse(null);
    }

    @Transactional
    public AppointmentDto create(AppointmentCreateDto dto) {
        validateTimes(dto.getStartTime(), dto.getEndTime());
        // check overlap for mechanic
        List<Appointment> overlaps = repository.findOverlapping(dto.getMechanicId(), dto.getStartTime(), dto.getEndTime());
        if (!overlaps.isEmpty()) {
            throw new OverlapException("Selected mechanic has an overlapping appointment");
        }
        Appointment a = new Appointment();
        a.setCustomerName(dto.getCustomerName());
        a.setVehicleReg(dto.getVehicleReg());
        a.setMechanicId(dto.getMechanicId());
        a.setStartTime(dto.getStartTime());
        a.setEndTime(dto.getEndTime());
        a.setStatus("SCHEDULED");
        Appointment saved = repository.save(a);
        return toDto(saved);
    }

    @Transactional
    public AppointmentDto update(Long id, AppointmentCreateDto dto) {
        validateTimes(dto.getStartTime(), dto.getEndTime());
        Appointment existing = repository.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
        List<Appointment> overlaps = repository.findOverlapping(dto.getMechanicId(), dto.getStartTime(), dto.getEndTime());
        boolean realOverlap = overlaps.stream().anyMatch(o -> !o.getId().equals(id));
        if (realOverlap) throw new OverlapException("Selected mechanic has an overlapping appointment");

        existing.setCustomerName(dto.getCustomerName());
        existing.setVehicleReg(dto.getVehicleReg());
        existing.setMechanicId(dto.getMechanicId());
        existing.setStartTime(dto.getStartTime());
        existing.setEndTime(dto.getEndTime());
        repository.save(existing);
        return toDto(existing);
    }

    @Transactional
    public void cancel(Long id) {
        Appointment a = repository.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
        a.setStatus("CANCELLED");
        repository.save(a);
    }

    private void validateTimes(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) throw new IllegalArgumentException("Start and end must be provided");
        if (!end.isAfter(start)) throw new IllegalArgumentException("End time must be after start time");
        if (start.isBefore(LocalDateTime.now())) throw new IllegalArgumentException("Cannot schedule in the past");
    }

    private AppointmentDto toDto(Appointment a) {
        AppointmentDto d = new AppointmentDto();
        d.setId(a.getId());
        d.setCustomerName(a.getCustomerName());
        d.setVehicleReg(a.getVehicleReg());
        d.setMechanicId(a.getMechanicId());
        d.setStartTime(a.getStartTime());
        d.setEndTime(a.getEndTime());
        d.setStatus(a.getStatus());
        return d;
    }
}
