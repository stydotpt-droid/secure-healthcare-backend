package com.example.demo.Controllers;


import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.Entites.Doctor;
import com.example.demo.Entites.DoctorRegistrationRequest;
import com.example.demo.Entites.Hospital;
import com.example.demo.Repos.HospitalRepository;
import com.example.demo.Services.DoctorService;



@RestController
@RequestMapping("/api/doctors")
@CrossOrigin(origins = "*")
public class DoctorController {
    
    @Autowired
    private DoctorService doctorService;
    
   
    
    @PostMapping("/register/{hospitalName}")
    public ResponseEntity<Map<String, Object>> registerDoctor(
            @PathVariable String hospitalName,
            @RequestParam("doctorName") String doctorName,
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("address") String address,
            @RequestParam("department") String department,
            @RequestParam("experience") Integer experience,
            @RequestParam("images") List<MultipartFile> images) {
    	
    	
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validate images count
            if (images == null || images.size() != 15) {
                response.put("success", false);
                response.put("message", "Exactly 15 images are required for registration");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Create registration request
            DoctorRegistrationRequest request = new DoctorRegistrationRequest();
            request.setHospitalName(hospitalName);
            request.setDoctorName(doctorName);
            request.setPhoneNumber(phoneNumber);
            request.setEmail(email);
            request.setPassword(password);
            request.setAddress(address);
            request.setDepartment(department);
            request.setExperience(experience);
            request.setImages(images);
            
            // Register doctor
            Doctor registeredDoctor = doctorService.registerDoctor(request);
            
            response.put("success", true);
            response.put("message", "Doctor registered successfully");
            response.put("doctorId", registeredDoctor.getId());
            response.put("totalImagesUploaded", registeredDoctor.getTotalImagesCount());
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "Failed to upload images to Google Drive: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Registration failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @GetMapping("/all")
    public ResponseEntity<List<Doctor>> getAllDoctors() {
        List<Doctor> doctors = doctorService.getAllDoctors();
        return ResponseEntity.ok(doctors);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Doctor> getDoctorById(@PathVariable Long id) {
        Optional<Doctor> doctor = doctorService.findById(id);
        return doctor.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/email/{email}")
    public ResponseEntity<Doctor> getDoctorByEmail(@PathVariable String email) {
        Optional<Doctor> doctor = doctorService.findByEmail(email);
        return doctor.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteDoctor(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            doctorService.deleteDoctor(id);
            response.put("success", true);
            response.put("message", "Doctor deleted successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to delete doctor: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @GetMapping("/{id}/profile-image")
    public ResponseEntity<byte[]> getProfileImage(@PathVariable Long id) {
        Optional<Doctor> doctor = doctorService.findById(id);
        
        if (doctor.isPresent() && doctor.get().getProfileImage() != null) {
            return ResponseEntity.ok()
                    .header("Content-Type", "image/jpeg")
                    .body(doctor.get().getProfileImage());
        }
        
        return ResponseEntity.notFound().build();
    }
}

