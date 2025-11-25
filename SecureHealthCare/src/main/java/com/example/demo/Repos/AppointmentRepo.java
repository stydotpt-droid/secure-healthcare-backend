package com.example.demo.Repos;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.Entites.Appointment;
import com.example.demo.Entites.Doctor;
import com.example.demo.Entites.Patient;

public interface AppointmentRepo extends JpaRepository<Appointment, Long> {

	List<Appointment> findByPatientEmailOrderByAppointmentDateTimeDesc(String patientEmail);

	List<Appointment> findByAssignedDoctorOrderByAppointmentDateTimeAsc(String assignedDoctor);

	List<Appointment> findByAssignedDoctorAndAppointmentDateTime(String assignedDoctor, LocalDateTime appointmentDateTime);

	List<Appointment> findByPatientEmailAndAppointmentDateTime(String patientEmail, LocalDateTime appointmentDateTime);

	List<Appointment> findByAssignedDoctor(String doctorEmail);

}
