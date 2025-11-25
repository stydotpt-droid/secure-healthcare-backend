package com.example.demo.Controllers;


import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.Entites.Appointment;
import com.example.demo.Entites.Doctor;
import com.example.demo.Entites.Hospital;
import com.example.demo.Entites.Patient;
import com.example.demo.Entites.Report;
import com.example.demo.Repos.AppointmentRepo;
import com.example.demo.Repos.DoctorRepo;
import com.example.demo.Repos.HospitalRepository;
import com.example.demo.Repos.PatientRepository;
import com.example.demo.Repos.ReportRepository;
import com.example.demo.Services.GeneratePDFService;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    @Autowired
    private DoctorRepo doctorRepo;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private AppointmentRepo appointmentRepo;

    @Autowired
    private GeneratePDFService generatePDFService;
    
    @Autowired
    
    private HospitalRepository hospitalRepository;

    @PostMapping("/makeReport")
    public ResponseEntity<String> makeReport(@RequestBody Map<String, Object> payload) {
        try {
            Long appointmentId = Long.valueOf(payload.get("appointmentId").toString());
            String reportType = payload.get("reportType").toString();
            String diseaseCategory = payload.get("diseaseCategory").toString();
            String prescription = payload.get("prescription").toString();
            String doctorEmail = payload.get("doctorEmail").toString();

            // Parameters map (converted from JSON object)
            Map<String, Object> parameters = (Map<String, Object>) payload.get("parameters");

            // Fetch appointment, doctor, patient
            Optional<Appointment> appointmentOpt = appointmentRepo.findById(appointmentId);
            if (!appointmentOpt.isPresent()) {
                return ResponseEntity.badRequest().body("Invalid appointment ID");
            }

            Appointment appointment = appointmentOpt.get();
            Optional<Doctor> doctorOpt = doctorRepo.findByEmail(doctorEmail);
            Optional<Patient> patientOpt = patientRepository.findByEmail(appointment.getPatientEmail());
            
            patientOpt.get().setPrescription(prescription);
            
            patientRepository.save(patientOpt.get());

            if (!doctorOpt.isPresent() || !patientOpt.isPresent()) {
                return ResponseEntity.badRequest().body("Doctor or patient not found");
            }

            // Generate PDF file from the report data
            byte[] pdfBytes = generatePDFService.createPdf(patientOpt.get().getFullName(), doctorOpt.get().getDoctorName(), doctorOpt.get().getHospitalName(), doctorOpt.get().getHospital(),reportType, parameters, prescription);

            // Generate SHA-256 hash of the PDF (simulate storing in blockchain)
            String sha256Hash = generateSHA256(pdfBytes);

            Report report = new Report();
            report.setAppointmentId(appointmentId);
            report.setPatientEmail(appointment.getPatientEmail());
            report.setDoctorEmail(doctorEmail);
            report.setReportType(reportType);
            report.setDiseaseCategory(diseaseCategory);
            report.setParametersJson(parameters.toString());
            report.setPrescriptionText(prescription);
            report.setFilePdf(pdfBytes);
            report.setSha256Hash(sha256Hash);
            report.setCreatedAt(LocalDateTime.now());
            report.setShareRequested(false);
            report.setShareApproved(false);
            report.setSharedToEmail(null);
            report.setShareTimestamp(null);


            reportRepository.save(report);

            return ResponseEntity.ok("Report successfully generated and stored");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error generating report: " + e.getMessage());
        }
    }

    private String generateSHA256(byte[] pdfBytes) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(pdfBytes);
        return Base64.getEncoder().encodeToString(hashBytes);
    }
    
    
    @GetMapping("/patient")
    public ResponseEntity<?> getReportsByPatientEmail(@RequestParam String email) {
        try {
            List<Report> reports = reportRepository.findByPatientEmail(email);
            if (reports.isEmpty()) {
                return ResponseEntity.status(404).body("No reports found for patient: " + email);
            }
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error fetching reports for patient: " + e.getMessage());
        }
    }
    @GetMapping("/doctor")
    public ResponseEntity<?> getReportsByDoctorEmail(@RequestParam String email) {
        try {
            List<Report> reports = reportRepository.findByDoctorEmail(email);
            if (reports.isEmpty()) {
                return ResponseEntity.status(404).body("No reports found for doctor: " + email);
            }
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error fetching reports for doctor: " + e.getMessage());
        }
    }

    
    @PostMapping("/requestShare")
    public ResponseEntity<String> requestShare(@RequestBody Map<String, String> payload) {
        String patientEmail = payload.get("patientEmail");
        String hospitalEmail = payload.get("hospitalEmail");

        List<Report> reports = reportRepository.findByPatientEmail(patientEmail);

        if (reports.isEmpty()) {
            return ResponseEntity.badRequest().body("No reports found for this patient.");
        }

        // ❌ Block if already shared with any hospital
        boolean alreadySharedToAny = reports.stream()
            .anyMatch(r -> Boolean.TRUE.equals(r.isShareApproved()));

        if (alreadySharedToAny) {
            return ResponseEntity.badRequest().body("Reports already shared with a hospital.");
        }

        // ❌ Block if already requested to any hospital
        boolean alreadyRequestedToAny = reports.stream()
            .anyMatch(r -> Boolean.TRUE.equals(r.isShareRequested()) && !Boolean.TRUE.equals(r.isShareApproved()));

        if (alreadyRequestedToAny) {
            return ResponseEntity.badRequest().body("You already have a pending share request to another hospital.");
        }

        // ✅ Proceed to create a new share request to selected hospital
        for (Report report : reports) {
            report.setShareRequested(true);
            report.setShareApproved(false);
            report.setSharedToEmail(hospitalEmail);
            report.setShareTimestamp(null);
            reportRepository.save(report);
        }

        return ResponseEntity.ok("Share request sent to current doctor.");
    }


    
    @GetMapping("/pendingShareRequests")
    public ResponseEntity<List<Report>> getPendingShareRequests(@RequestParam String doctorEmail) {
        List<Report> pendingRequests = reportRepository
            .findByDoctorEmailAndShareRequestedTrueAndShareApprovedFalse(doctorEmail);
        return ResponseEntity.ok(pendingRequests);
    }

    @PostMapping("/approveShare")
    public ResponseEntity<String> approveShare(@RequestBody Map<String, String> payload) {
        String patientEmail = payload.get("patientEmail");
        String hospitalEmail = payload.get("hospitalEmail");

        List<Report> reports = reportRepository.findByPatientEmailAndSharedToEmail(patientEmail, hospitalEmail);

        for (Report report : reports) {
            report.setShareApproved(true);
            report.setShareTimestamp(LocalDateTime.now());
            reportRepository.save(report);
        }

        return ResponseEntity.ok("Reports approved and shared with: " + hospitalEmail);
    }

    @GetMapping("/getAllHospitals")
    public ResponseEntity<List<HospitalDTO>> getAllHospitals(@RequestParam String patientEmail) {

        Optional<Patient> ptOpt = patientRepository.findByEmail(patientEmail);
        if (!ptOpt.isPresent()) {
            return ResponseEntity.badRequest().build();
        }

        Hospital patientHospital = ptOpt.get().getHospital();
        Long patientHospitalId = patientHospital.getId();

        List<HospitalDTO> hospitals = hospitalRepository.findAll().stream()
            .filter(h -> !h.getId().equals(patientHospitalId))  // exclude patient's hospital
            .map(h -> new HospitalDTO(h.getEmail(), h.getHospitalName(), h.getAddress()))
            .collect(Collectors.toList());

        return ResponseEntity.ok(hospitals);
    }
    public class HospitalDTO {
        private String email, name, location;

		public HospitalDTO(String email, String name, String location) {
			super();
			this.email = email;
			this.name = name;
			this.location = location;
		}

		public String getEmail() {
			return email;
		}

		public void setEmail(String email) {
			this.email = email;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getLocation() {
			return location;
		}

		public void setLocation(String location) {
			this.location = location;
		}
        
    }
    
    @GetMapping("/sharedReports")
    public ResponseEntity<List<SharedReportDTO>> getSharedReports(@RequestParam String hospitalEmail) {
        List<Report> sharedReports = reportRepository.findBySharedToEmailAndShareApprovedTrue(hospitalEmail);

        List<SharedReportDTO> dtos = sharedReports.stream().map(r -> {
            Patient pat = patientRepository.findByEmail(r.getPatientEmail()).orElse(null);
            Doctor doc = doctorRepo.findByEmail(r.getDoctorEmail()).orElse(null);
            Hospital originalHosp = pat != null ? pat.getHospital() : null;

            return new SharedReportDTO(
                r.getId(),
                r.getReportType(),
                r.getCreatedAt(),
                Base64.getEncoder().encodeToString(r.getFilePdf()),
                pat != null ? pat.getFullName() : null,
                pat != null ? pat.getPhoneNumber() : null,
                pat != null && originalHosp != null ? originalHosp.getHospitalName() : null,
                doc != null ? doc.getDoctorName() : null,
                doc != null && doc.getHospital() != null ? doc.getHospital().getHospitalName() : null
            );
        }).collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/doctor-matching-reports")
    public ResponseEntity<?> getDoctorAccessibleReports(@RequestParam String doctorEmail) {
        try {
            Optional<Doctor> doctorOpt = doctorRepo.findByEmail(doctorEmail);
            if (doctorOpt.isEmpty()) {
                return ResponseEntity.status(404).body("Doctor not found");
            }

            Doctor doctor = doctorOpt.get();
            String doctorHospitalEmail = doctor.getHospital().getEmail();
            String doctorCategory = doctor.getDepartment();
            String doctorName = doctor.getDoctorName();

            // ✅ Fetch appointments by doctor name
            List<Appointment> appointments = appointmentRepo.findByAssignedDoctorOrderByAppointmentDateTimeAsc(doctorName);
            Set<String> patientEmails = appointments.stream()
                    .map(Appointment::getPatientEmail)
                    .collect(Collectors.toSet());

            List<Report> sharedReports = reportRepository
                    .findBySharedToEmailAndShareApprovedTrue(doctorHospitalEmail);

            List<Report> matchingReports = sharedReports.stream()
                    .filter(r -> doctorCategory != null
                            && doctorCategory.equalsIgnoreCase(r.getDiseaseCategory())
                            && patientEmails.contains(r.getPatientEmail()))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(matchingReports);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }



}
