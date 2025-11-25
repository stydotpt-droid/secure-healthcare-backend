package com.example.demo.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.Entites.Appointment;
import com.example.demo.Entites.Appointment.AppointmentStatus;
import com.example.demo.Entites.Doctor;
import com.example.demo.Entites.Patient;
import com.example.demo.Repos.AppointmentRepo;
import com.example.demo.Repos.DoctorRepo;
import com.example.demo.Repos.HospitalRepository;
import com.example.demo.Repos.PatientRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/appointment")
@CrossOrigin(origins = "*")
public class AppointmentController {

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private DoctorRepo doctorRepo;

    @Autowired
    private AppointmentRepo appointmentRepo;

    @Autowired
    private PatientRepository patientRepository;

    // DTO class for appointment request
    public static class AppointmentRequest {
        private String patientEmail;
        private String appointmentDateTime;
        private String diseaseDepartment;
        private String assignedDoctor;
        private String status;
        private String symptoms;
       

        // Constructors
        public AppointmentRequest() {}

        

        public AppointmentRequest(String patientEmail, String appointmentDateTime, String diseaseDepartment,
				String assignedDoctor, String status, String symptoms) {
			super();
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



		// Getters and Setters
        public String getPatientEmail() {
            return patientEmail;
        }

        public void setPatientEmail(String patientEmail) {
            this.patientEmail = patientEmail;
        }

        public String getAppointmentDateTime() {
            return appointmentDateTime;
        }

        public void setAppointmentDateTime(String appointmentDateTime) {
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

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    @PostMapping("/register/{patientEmail}")
    public ResponseEntity<?> makeAppointment(@PathVariable String patientEmail, 
                                           @RequestBody AppointmentRequest appointmentRequest) {
        try {
            // Validate patient exists
            Optional<Patient> patientOpt = patientRepository.findByEmail(patientEmail);
            if (!patientOpt.isPresent()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Patient not found with email: " + patientEmail);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            Patient patient = patientOpt.get();

            // Parse and validate appointment date time
            LocalDateTime appointmentDateTime;
            try {
                // Handle both formats: "2025-07-03T10:30:00" and "2025-07-03 10:30:00"
                String dateTimeStr = appointmentRequest.getAppointmentDateTime();
                if (dateTimeStr.contains("T")) {
                    appointmentDateTime = LocalDateTime.parse(dateTimeStr);
                } else {
                    // Replace space with T for parsing
                    dateTimeStr = dateTimeStr.replace(" ", "T");
                    appointmentDateTime = LocalDateTime.parse(dateTimeStr);
                }
            } catch (DateTimeParseException e) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Invalid date time format. Use YYYY-MM-DDTHH:MM:SS or YYYY-MM-DD HH:MM:SS");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            // Validate appointment is in the future
            if (appointmentDateTime.isBefore(LocalDateTime.now())) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Appointment date must be in the future");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            // Find doctor by name
            Optional<Doctor> doctorOpt = doctorRepo.findByDoctorName(appointmentRequest.getAssignedDoctor());
            if (!doctorOpt.isPresent()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Doctor not found: " + appointmentRequest.getAssignedDoctor());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            Doctor doctor = doctorOpt.get();

            // Validate doctor's department matches requested department
            if (!doctor.getDepartment().equalsIgnoreCase(appointmentRequest.getDiseaseDepartment())) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Doctor " + doctor.getDoctorName() + " is not available in " + appointmentRequest.getDiseaseDepartment() + " department");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            // Check for conflicting appointments (same doctor, same time)
            List<Appointment> conflictingAppointments = appointmentRepo.findByAssignedDoctorAndAppointmentDateTime(doctor.getDoctorName(), appointmentDateTime);
            if (!conflictingAppointments.isEmpty()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Doctor " + doctor.getDoctorName() + " is not available at the requested time");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
            }

            // Check if patient already has an appointment at the same time
            List<Appointment> patientConflicts = appointmentRepo.findByPatientEmailAndAppointmentDateTime(patient.getEmail(), appointmentDateTime);
            if (!patientConflicts.isEmpty()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "You already have an appointment scheduled at this time");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
            }

            // Create new appointment
            Appointment appointment = new Appointment();
            appointment.setPatientEmail(patientEmail);
            appointment.setAssignedDoctor(doctor.getDoctorName());
            appointment.setAppointmentDateTime(appointmentDateTime);
            appointment.setDiseaseDepartment(appointmentRequest.getDiseaseDepartment());
            appointment.setStatus(AppointmentStatus.JUST_ASSIGNED_TO_DOCTOR);
            appointment.setSymptoms(appointmentRequest.getSymptoms());
            Appointment savedAppointment = appointmentRepo.save(appointment);

            // Prepare success response
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Appointment scheduled successfully!");
            response.put("appointmentId", savedAppointment.getId());
            response.put("doctorName", doctor.getDoctorName());
            response.put("department", savedAppointment.getDiseaseDepartment());
            response.put("hospitalName", doctor.getHospital().getHospitalName());
            response.put("appointmentDateTime", savedAppointment.getAppointmentDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            response.put("status", AppointmentStatus.JUST_ASSIGNED_TO_DOCTOR);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to schedule appointment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // Get all appointments for a patient
    @GetMapping("/patient/{patientEmail}")
    public ResponseEntity<?> getPatientAppointments(@PathVariable String patientEmail) {
        try {
            Optional<Patient> patientOpt = patientRepository.findByEmail(patientEmail);
            if (!patientOpt.isPresent()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Patient not found with email: " + patientEmail);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            List<Appointment> appointments = appointmentRepo.findByPatientEmailOrderByAppointmentDateTimeDesc(patientOpt.get().getEmail());
            return ResponseEntity.ok(appointments);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to fetch appointments: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // Get all appointments for a doctor
    @GetMapping("/doctor/{email}")
    public ResponseEntity<?> getDoctorAppointments(@PathVariable String email) {
        try {
            Optional<Doctor> doctorOpt = doctorRepo.findByEmail(email);
            if (!doctorOpt.isPresent()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Doctor not found: " + email);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            Doctor doctor = doctorOpt.get();
            List<Appointment> appointments = appointmentRepo.findByAssignedDoctorOrderByAppointmentDateTimeAsc(doctor.getDoctorName());

            // Prepare detailed response
            List<Map<String, Object>> responseList = new java.util.ArrayList<>();

            for (Appointment appointment : appointments) {
                Map<String, Object> appointmentDetails = new HashMap<>();

                // Find the patient by email
                Optional<Patient> patientOpt = patientRepository.findByEmail(appointment.getPatientEmail());
                if (patientOpt.isPresent()) {
                    Patient patient = patientOpt.get();

                    appointmentDetails.put("appointmentId", appointment.getId());
                    appointmentDetails.put("appointmentDateTime", appointment.getAppointmentDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                    appointmentDetails.put("status", appointment.getStatus());
                    appointmentDetails.put("patientName", patient.getFullName());
                    appointmentDetails.put("patientPhoneNumber", patient.getPhoneNumber());
                    appointmentDetails.put("patientAddress", patient.getAddress());
                    appointmentDetails.put("diseaseCategory", doctor.getDepartment());
                    appointmentDetails.put("symptoms", appointment.getSymptoms());
                    responseList.add(appointmentDetails);
                }
            }

            return ResponseEntity.ok(responseList);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to fetch appointments: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PutMapping("/{appointmentId}/complete")
    public ResponseEntity<?> markAppointmentAsCompleted(@PathVariable Long appointmentId) {
        try {
            Optional<Appointment> appointmentOpt = appointmentRepo.findById(appointmentId);
            if (!appointmentOpt.isPresent()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Appointment not found with ID: " + appointmentId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            Appointment appointment = appointmentOpt.get();
            appointment.setStatus(Appointment.AppointmentStatus.COMPLETED);
            appointmentRepo.save(appointment);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Appointment marked as completed");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to update appointment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
}