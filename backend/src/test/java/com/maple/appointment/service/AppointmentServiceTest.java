package com.maple.appointment.service;

import com.maple.appointment.dto.AppointmentCreateDto;
import com.maple.appointment.entity.Appointment;
import com.maple.appointment.exception.OverlapException;
import com.maple.appointment.repository.AppointmentRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

@SpringBootTest
public class AppointmentServiceTest {

    @Autowired
    private AppointmentService service;

    @Autowired
    private AppointmentRepository repo;

    @Test
    public void testOverlapPrevention() {
        // clear repo
        repo.deleteAll();

        // existing appointment
        Appointment a = new Appointment();
        a.setCustomerName("Existing");
        a.setVehicleReg("XX11YY");
        a.setMechanicId(1L);
        a.setStartTime(LocalDateTime.of(2026,1,10,9,0));
        a.setEndTime(LocalDateTime.of(2026,1,10,10,0));
        a.setStatus("SCHEDULED");
        repo.save(a);

        // attempt overlapping create
        AppointmentCreateDto dto = new AppointmentCreateDto();
        dto.setCustomerName("New");
        dto.setVehicleReg("ZZ99TT");
        dto.setMechanicId(1L);
        dto.setStartTime(LocalDateTime.of(2026,1,10,9,30));
        dto.setEndTime(LocalDateTime.of(2026,1,10,10,30));

        Assertions.assertThrows(OverlapException.class, () -> service.create(dto));
    }
}
