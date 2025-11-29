package com.maple.appointment.repository;

import com.maple.appointment.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    @Query("SELECT a FROM Appointment a WHERE a.mechanicId = :mechanicId AND (a.startTime < :end AND a.endTime > :start) AND a.status = 'SCHEDULED'")
    List<Appointment> findOverlapping(@Param("mechanicId") Long mechanicId,
                                      @Param("start") LocalDateTime start,
                                      @Param("end") LocalDateTime end);
}
