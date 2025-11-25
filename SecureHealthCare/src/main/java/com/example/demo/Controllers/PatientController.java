package com.example.demo.Controllers;



import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.Controllers.HospitalController.LoginRequest;
import com.example.demo.Entites.Hospital;
import com.example.demo.Entites.Patient;
import com.example.demo.Entites.PatientDTO;
import com.example.demo.Entites.Report;
import com.example.demo.Repos.HospitalRepository;
import com.example.demo.Repos.PatientRepository;
import com.example.demo.Repos.ReportRepository;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api/patient")
@CrossOrigin(origins = "*")
public class PatientController {

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private PatientRepository patientRepository;
    
    @Autowired
    
    private ReportRepository reportRepository;


    @PostMapping("/register/{hospitalName}")
    public ResponseEntity<String> registerPatient(
            @RequestBody PatientDTO patientDTO,
            @PathVariable String hospitalName) {

        try {
            // Validate email
            if (patientDTO.getEmail() == null || patientDTO.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Email is required");
            }

            // Check hospital exists
            Optional<Hospital> hospitalOpt = hospitalRepository.findByHospitalName(hospitalName);
            if (hospitalOpt.isEmpty()) {
                return ResponseEntity.status(404).body("Hospital not found");
            }

            Hospital hospital = hospitalOpt.get();

            // ✅ Check if patient already registered to this hospital
            Optional<Patient> existingPatientSameHospital = patientRepository.findByEmailAndHospital(patientDTO.getEmail(), hospital);
            if (existingPatientSameHospital.isPresent()) {
                return ResponseEntity.status(400).body("This patient is already registered at this hospital.");
            }

            // ✅ Allow same patient to register in other hospitals

            // Generate patient ID
            String generatedId = generatePatientId(hospitalName);

            // Create patient object
            Patient patient = new Patient();
            patient.setPatientId(generatedId);
            patient.setFullName(patientDTO.getFullName());
            patient.setDob(patientDTO.getDob());
            patient.setGender(patientDTO.getGender());
            patient.setEmail(patientDTO.getEmail());
            patient.setPhoneNumber(patientDTO.getPhoneNumber());
            patient.setAddress(patientDTO.getAddress());
            patient.setPassword(patientDTO.getPassword());
            patient.setHospital(hospital);

            patientRepository.save(patient);

            // Save initial report if uploaded
            if (patientDTO.getExistingReportPdf() != null && !patientDTO.getExistingReportPdf().trim().isEmpty()) {
                try {
                    byte[] pdfBytes = Base64.getDecoder().decode(patientDTO.getExistingReportPdf());
                    Report rep = new Report();
                    rep.setPatientEmail(patientDTO.getEmail());
                    rep.setFilePdf(pdfBytes);
                    reportRepository.save(rep);
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.status(400).body("Invalid PDF format provided.");
                }
            }

            String successMessage = "Patient registered successfully! ID: " + generatedId;
            return ResponseEntity.ok(successMessage);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Registration failed: " + e.getMessage());
        }
    }

    private String generatePatientId(String hospitalName) {
     
        String prefix = hospitalName.length() >= 3 ?
                hospitalName.substring(0, 3).toUpperCase() :
                String.format("%-3s", hospitalName).replace(' ', 'X');

        Random random = new Random();
        int suffix = 100 + random.nextInt(900); 

        return prefix + suffix;
    }
    
    @GetMapping("/getPatients")
    public ResponseEntity<List<Patient>> getAllPatients() {
    	
    	List<Patient> p = patientRepository.findAll();
        return ResponseEntity.ok(p);
    }
    
    
    @GetMapping("/getPatients/{hospitalEmail}")
    public ResponseEntity<List<Patient>> getPatientsByHosp(@PathVariable String hospitalEmail) {
    	
    	Optional<Hospital> p = hospitalRepository.findByEmail(hospitalEmail);
    	
    	List<Patient> pts = patientRepository.findByHospital(p.get());
    	
        return ResponseEntity.ok(pts);
    }
    
    @PostMapping("/login")
    public ResponseEntity<String> loginHospital(@RequestBody LoginRequest loginRequest) {
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        // Find hospital by email
        Optional<Patient> hospitalOptional = patientRepository.findByEmail(email);

        if (hospitalOptional.isPresent()) {
            Patient hospital = hospitalOptional.get();

            // Check if password matches
            if (hospital.getPassword().equals(password)) {
                return ResponseEntity.ok("Login successful");
            } else {
                return ResponseEntity.status(401).body("Invalid credentials");
            }
        } else {
            return ResponseEntity.status(404).body("Hospital not found");
        }
    }
}
