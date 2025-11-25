package com.example.demo.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.Entites.Doctor;
import com.example.demo.Entites.Hospital;
import com.example.demo.Entites.Patient;
import com.example.demo.Repos.DoctorRepo;
import com.example.demo.Repos.HospitalRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/api/hospital")
@CrossOrigin(origins = "*")
public class HospitalController {

    @Autowired
    private HospitalRepository hospitalRepository;
    
    @Autowired
    
    private DoctorRepo doctorRepo;
    
    @PostMapping("/regNewHospital")
    public ResponseEntity<String> registerNewHospital(@RequestBody Hospital hospital) {

        Optional<Hospital> existingHospital = hospitalRepository.findByHospitalNameAndAddress(hospital.getHospitalName(), hospital.getAddress());

        if (existingHospital.isPresent()) {
            return ResponseEntity.status(400).body("Hospital with the same name and address already exists.");
        }
        
        hospitalRepository.save(hospital);
        return ResponseEntity.ok("New Hospital Registered Successfully!");
    }

    // GET: Get all hospitals
    @GetMapping("/getAllHospitals")
    public ResponseEntity<List<Hospital>> getAllHospitals() {
        List<Hospital> hospitals = hospitalRepository.findAll();
        return ResponseEntity.ok(hospitals);
    }

    // GET: Get hospital by name
    @GetMapping("/getHospitalByEmail/{hospitalEmail}")
    public ResponseEntity<?> getHospitalByName(@PathVariable String hospitalEmail) {
        Optional<Hospital> hospital = hospitalRepository.findByEmail(hospitalEmail);

        if (hospital.isEmpty()) {
            return ResponseEntity.status(404).body("No hospital found with the given name.");
        }

        return ResponseEntity.ok(hospital.get());
    }
    
    @PostMapping("/login")
    public ResponseEntity<String> loginHospital(@RequestBody LoginRequest loginRequest) {
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        // Find hospital by email
        Optional<Hospital> hospitalOptional = hospitalRepository.findByEmail(email);

        if (hospitalOptional.isPresent()) {
            Hospital hospital = hospitalOptional.get();

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
    
    @GetMapping("/getAllDoctorsByHospitalEMail/{hospitalEmail}")
    
    public ResponseEntity<Set<Doctor>> getPatientsByHosp(@PathVariable String hospitalEmail) {
    	
    	Optional<Hospital> p = hospitalRepository.findByEmail(hospitalEmail);
    	
    	Set<Doctor> pts = p.get().getDoctors();
    	
        return ResponseEntity.ok(pts);
    }
    
    public static class LoginRequest {
        private String email;
        private String password;

        // Getters and Setters
        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}