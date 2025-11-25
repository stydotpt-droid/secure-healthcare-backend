package com.example.demo.Entites;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "appointments")
public class Appointment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "patient_email", nullable = false)
    private String patientEmail;
    
    @Column(name = "appointment_date_time", nullable = false)
    private LocalDateTime appointmentDateTime;
    
    @Column(name = "disease_department", nullable = false)
    private String diseaseDepartment;
    
    @Column(name = "assigned_doctor")
    private String assignedDoctor;
    
    public enum AppointmentStatus {
        JUST_ASSIGNED_TO_DOCTOR,
        COMPLETED
    }
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AppointmentStatus status;
    
    private String symptoms;
    
    
  
    public Appointment() {}

    public Appointment(Long id, String patientEmail, LocalDateTime appointmentDateTime, String diseaseDepartment,
			String assignedDoctor, AppointmentStatus status, String symptoms) {
		super();
		this.id = id;
		this.patientEmail = patientEmail;
		this.appointmentDateTime = appointmentDateTime;
		this.diseaseDepartment = diseaseDepartment;
		this.assignedDoctor = assignedDoctor;
		this.status = status;
		this.symptoms = symptoms;
		
	}

    public String getSymptoms() {
		return symptoms;
	}

	public void setSymptoms(String symptoms) {
		this.symptoms = symptoms;
	}



	public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getPatientEmail() {
        return patientEmail;
    }
    
    public void setPatientEmail(String patientEmail) {
        this.patientEmail = patientEmail;
    }
    
    public LocalDateTime getAppointmentDateTime() {
        return appointmentDateTime;
    }
    
    public void setAppointmentDateTime(LocalDateTime appointmentDateTime) {
        this.appointmentDateTime = appointmentDateTime;
    }
    
    public String getDiseaseDepartment() {
        return diseaseDepartment;
    }
    
    public void setDiseaseDepartment(String diseaseDepartment) {
        this.diseaseDepartment = diseaseDepartment;
    }
    
    public String getAssignedDoctor() {
        return assignedDoctor;
    }
    
    public void setAssignedDoctor(String assignedDoctor) {
        this.assignedDoctor = assignedDoctor;
    }
    
    public AppointmentStatus getStatus() {
        return status;
    }
    
    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }
    
    @Override
    public String toString() {
        return "Appointment{" +
                "id=" + id +
                ", patientEmail='" + patientEmail + '\'' +
                ", appointmentDateTime=" + appointmentDateTime +
                ", diseaseDepartment='" + diseaseDepartment + '\'' +
                ", assignedDoctor='" + assignedDoctor + '\'' +
                ", status=" + status +
                '}';
    }
}


