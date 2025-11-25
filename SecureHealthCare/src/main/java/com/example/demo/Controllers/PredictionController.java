package com.example.demo.Controllers;

import com.example.demo.Entites.Doctor;
import com.example.demo.Entites.Hospital;
import com.example.demo.Entites.Patient;
import com.example.demo.Repos.AppointmentRepo;
import com.example.demo.Repos.DoctorRepo;
import com.example.demo.Repos.HospitalRepository;
import com.example.demo.Repos.PatientRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;

@RestController
@RequestMapping("/api/predictDept")
@CrossOrigin(origins = "*")
public class PredictionController {
    
    private final OrtEnvironment env = OrtEnvironment.getEnvironment();
    private OrtSession session;
    private List<String> symptomsList;
    private Map<String, String> symptomMappings;
    
    @Autowired
    private HospitalRepository hospitalRepository;
    
    @Autowired
    private DoctorRepo doctorRepo;
    
    @Autowired
    private AppointmentRepo appointmentRepo;
    
    @Autowired 
    private PatientRepository patientRepository;
    
    public PredictionController() throws OrtException, IOException {
        // Load ONNX model
        String modelPath = "C:\\Users\\Dell\\Downloads\\medical_department_model.onnx";
        session = env.createSession(modelPath, new OrtSession.SessionOptions());
        System.out.println("ONNX Model loaded successfully from: " + modelPath);
        
        // Load symptoms list from JSON file (not as text lines)
        loadSymptomsFromJson();
        
        // Initialize symptom mappings for better text extraction
        initializeSymptomMappings();
        
        System.out.println("Loaded " + symptomsList.size() + " symptoms");
        System.out.println("First few symptoms: " + symptomsList.subList(0, Math.min(5, symptomsList.size())));
    }
    
    private void loadSymptomsFromJson() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String jsonPath = "C:\\Users\\Dell\\Downloads\\symptoms_list.json";
        
        try {
            // Read JSON file and parse as List<String>
            String jsonContent = new String(Files.readAllBytes(Paths.get(jsonPath)));
            symptomsList = mapper.readValue(jsonContent, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            System.err.println("Error loading symptoms JSON: " + e.getMessage());
            // Fallback: try to read as text lines (if it's not proper JSON)
            symptomsList = Files.readAllLines(Paths.get(jsonPath));
        }
    }
    
    private void initializeSymptomMappings() {
        symptomMappings = new HashMap<>();
        
        // Add common symptom mappings for better text extraction
        symptomMappings.put("cold", "cold");
        symptomMappings.put("cough", "cough");
        symptomMappings.put("fever", "fever");
        symptomMappings.put("headache", "headache");
        symptomMappings.put("body pain", "body pain");
        symptomMappings.put("bodyache", "body pain");
        symptomMappings.put("muscle pain", "muscle pain");
        symptomMappings.put("joint pain", "joint pain");
        symptomMappings.put("chest pain", "chest pain");
        symptomMappings.put("stomach pain", "stomach pain");
        symptomMappings.put("stomach ache", "stomach ache");
        symptomMappings.put("back pain", "back pain");
        symptomMappings.put("sore throat", "sore throat");
        symptomMappings.put("runny nose", "runny nose");
        symptomMappings.put("nausea", "nausea");
        symptomMappings.put("vomiting", "vomiting");
        symptomMappings.put("diarrhea", "diarrhea");
        symptomMappings.put("fatigue", "fatigue");
        symptomMappings.put("weakness", "weakness");
        symptomMappings.put("dizziness", "dizziness");
        symptomMappings.put("shortness of breath", "shortness of breath");
        symptomMappings.put("breathing problem", "shortness of breath");
        symptomMappings.put("difficulty breathing", "shortness of breath");
        symptomMappings.put("rash", "rash");
        symptomMappings.put("itching", "itching");
        symptomMappings.put("vision problem", "vision problems");
        symptomMappings.put("blurred vision", "blurred vision");
        symptomMappings.put("eye pain", "eye pain");
        symptomMappings.put("anxiety", "anxiety");
        symptomMappings.put("depression", "depression");
        symptomMappings.put("sleep disorder", "sleep disorders");
        symptomMappings.put("insomnia", "sleep disorders");
        symptomMappings.put("palpitations", "palpitations");
        symptomMappings.put("heart beating fast", "palpitations");
        symptomMappings.put("rapid heartbeat", "rapid heartbeat");
    }
    
    @PostMapping("/predict/{patientEmail}/{hospitalEmail}")
    public ResponseEntity<Map<String, Object>> predictDepartment(@RequestBody Map<String, String> request, @PathVariable String patientEmail, @PathVariable String hospitalEmail ) {
        
        Optional<Patient> pt = patientRepository.findByEmail(patientEmail);
        
        if (!pt.isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Patient not found with email: " + patientEmail));
        }
        
        Optional<Hospital> hosp = hospitalRepository.findByEmail(hospitalEmail);
        Set<Doctor> doctors = hosp.get().getDoctors();
        
        String userInput = request.get("symptoms");
        if (userInput == null || userInput.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Symptoms input is required."));
        }
        
        List<String> inputSymptoms = extractSymptomsImproved(userInput.toLowerCase());
        System.out.println("Extracted symptoms: " + inputSymptoms);
        
        if (inputSymptoms.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                "error", "No recognizable symptoms found.",
                "message", "Please use symptoms like: fever, cough, headache, body pain, chest pain, etc."
            ));
        }
        
        // Create feature vector
        float[] featureVector = new float[symptomsList.size()];
        for (int i = 0; i < symptomsList.size(); i++) {
            featureVector[i] = inputSymptoms.contains(symptomsList.get(i)) ? 1.0f : 0.0f;
        }
        
        
        try {
            // Create ONNX tensor with correct shape
            long[] shape = {1, featureVector.length};
            OnnxTensor inputTensor = OnnxTensor.createTensor(env, new float[][]{featureVector});
            
            // Run prediction
            Map<String, OnnxTensor> inputs = Collections.singletonMap("float_input", inputTensor);
            OrtSession.Result output = session.run(inputs);
            
            // Extract prediction result
            String[] predictions = (String[]) output.get(0).getValue();
            String predictedDepartment = predictions[0];
            
            // Get confidence if available
            float confidence = 0.0f;
            try {
                if (output.size() > 1) {
                    float[][] probabilities = (float[][]) output.get(1).getValue();
                    confidence = getMaxProbability(probabilities[0]);
                }
            } catch (Exception e) {
                System.out.println("Confidence not available: " + e.getMessage());
            }
            
            // Filter doctors based on predicted department
            List<Doctor> departmentDoctors = doctors.stream()
                .filter(doctor -> doctor.getDepartment().equalsIgnoreCase(predictedDepartment))
                .collect(Collectors.toList());
            
            String assignedDoctorName = null;
            if (!departmentDoctors.isEmpty()) {
                // Assign first available doctor from the department
                Doctor assignedDoctor = departmentDoctors.get(0);
                assignedDoctorName = assignedDoctor.getDoctorName();
                System.out.println("Assigned doctor: " + assignedDoctorName + " from " + predictedDepartment + " department");
            } else {
                System.out.println("No doctors found in " + predictedDepartment + " department for this hospital");
            }
            
            // Clean up resources
            inputTensor.close();
            output.close();
            
            // Return response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("predictedDepartment", predictedDepartment.toUpperCase());
            response.put("confidence", confidence);
            response.put("matchedSymptoms", inputSymptoms);
            response.put("assignedDoctor", assignedDoctorName);
            response.put("hospitalName", hosp.get().getHospitalName());
            response.put("patientEmail", patientEmail);
            response.put("availableDoctorsInDepartment", departmentDoctors.size());
            
            if (assignedDoctorName != null) {
                response.put("message", "Please visit Dr. " + assignedDoctorName + " in the " + predictedDepartment + " department for proper medical consultation.");
            } else {
                response.put("message", "No doctors available in the " + predictedDepartment + " department at your hospital. Please contact hospital administration.");
            }
            
            response.put("warning", "⚠️ This is an AI prediction for guidance only. Always consult with medical professionals for accurate diagnosis.");
           
            
            return ResponseEntity.ok(response);
            
        } catch (OrtException e) {
            System.err.println("ONNX Runtime error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                "error", "Model inference failed.", 
                "details", e.getMessage()
            ));
        }
    }
    
    private List<String> extractSymptomsImproved(String input) {
        List<String> found = new ArrayList<>();
        
        // First, check symptom mappings for better matching
        for (Map.Entry<String, String> entry : symptomMappings.entrySet()) {
            String keyword = entry.getKey();
            String mappedSymptom = entry.getValue();
            
            if (input.contains(keyword) && symptomsList.contains(mappedSymptom)) {
                if (!found.contains(mappedSymptom)) {
                    found.add(mappedSymptom);
                }
            }
        }
        
        // Then check for direct symptom matches
        for (String symptom : symptomsList) {
            if (input.contains(symptom.toLowerCase()) && !found.contains(symptom)) {
                found.add(symptom);
            }
        }
        
        return found;
    }
    
    private float getMaxProbability(float[] probabilities) {
        float max = 0.0f;
        for (float prob : probabilities) {
            if (prob > max) {
                max = prob;
            }
        }
        return max;
    }
}