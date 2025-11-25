package com.example.demo.Controllers;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.Entites.Doctor;
import com.example.demo.Entites.FacialEmbedding;
import com.example.demo.Repos.DoctorRepo;
import com.example.demo.Repos.FacialEmbeddingRepo;
import com.example.demo.Services.OnnxEmbeddingService;

@RestController
@RequestMapping("/api/doctors")
@CrossOrigin(origins = "*")
public class FacialEmbedController {

    @Autowired
    private FacialEmbeddingRepo facialEmbeddingRepository;

    @Autowired
    private OnnxEmbeddingService onnxEmbeddingService;

    @Autowired
    private DoctorRepo doctorRepository;

    @PostMapping(value = "/verify-face", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> verifyFace(
            @RequestParam("image") MultipartFile imageFile,
            @RequestParam("email") String email) {

        Map<String, Object> response = new HashMap<>();
        File tempFile = null;

        try {
            System.out.println("Starting face verification for email: " + email);
            
            // Validate input
            if (imageFile.isEmpty()) {
                response.put("verified", false);
                response.put("message", "No image file provided.");
                return ResponseEntity.ok(response);
            }

            if (email == null || email.trim().isEmpty()) {
                response.put("verified", false);
                response.put("message", "Email is required.");
                return ResponseEntity.ok(response);
            }

            // Save uploaded image temporarily with proper extension
            String originalFilename = imageFile.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".jpg";
            
            tempFile = File.createTempFile("live_image", extension);
            imageFile.transferTo(tempFile);
            
            System.out.println("Temporary file created: " + tempFile.getAbsolutePath());
            System.out.println("File size: " + tempFile.length() + " bytes");

            // Extract embedding from live image
            float[] liveEmbedding = onnxEmbeddingService.extractSingleEmbedding(tempFile, email);

            if (liveEmbedding == null || liveEmbedding.length == 0) {
                response.put("verified", false);
                response.put("message", "Failed to extract embedding from image. Please ensure the image contains a clear face.");
                return ResponseEntity.ok(response);
            }

            // Normalize the live embedding
            float[] normalizedLiveEmbedding = normalizeEmbedding(liveEmbedding);
            System.out.println("Live embedding extracted successfully. Length: " + normalizedLiveEmbedding.length);

            // Fetch doctor by email
            Optional<Doctor> doctorOpt = doctorRepository.findByEmail(email.trim());
            if (!doctorOpt.isPresent()) {
                System.out.println("Doctor not found for email: " + email);
                response.put("verified", false);
                response.put("message", "Doctor not found with this email.");
                return ResponseEntity.ok(response);
            }

            Doctor doctor = doctorOpt.get();
            List<FacialEmbedding> embeddings = facialEmbeddingRepository.findByDoctorId(doctor.getId());

            if (embeddings.isEmpty()) {
                System.out.println("No facial embeddings found for doctor: " + email);
                response.put("verified", false);
                response.put("message", "No facial data found for this doctor. Please complete registration first.");
                return ResponseEntity.ok(response);
            }

            System.out.println("Found " + embeddings.size() + " stored embeddings for comparison");

            // Compare embeddings using cosine similarity with lower threshold
            boolean matched = false;
            double threshold = 0.8; // Lowered threshold for better matching
            double maxSimilarity = 0.0;

            for (int i = 0; i < embeddings.size(); i++) {
                FacialEmbedding fe = embeddings.get(i);
                float[] storedEmbedding = EmbeddingUtils.byteArrayToFloatArray(fe.getEmbeddingVector());
                
                if (storedEmbedding == null || storedEmbedding.length == 0) {
                    System.out.println("Invalid stored embedding at index " + i);
                    continue;
                }

                double similarity = EmbeddingUtils.cosineSimilarity(storedEmbedding, normalizedLiveEmbedding);
                System.out.println("Similarity with stored embedding " + i + ": " + similarity);
                
                if (similarity > maxSimilarity) {
                    maxSimilarity = similarity;
                }
                
                if (similarity > threshold) {
                    matched = true;
                    System.out.println("Match found! Similarity: " + similarity);
                    break;
                }
            }

            System.out.println("Maximum similarity achieved: " + maxSimilarity);
            System.out.println("Threshold: " + threshold);
            System.out.println("Match result: " + matched);

            response.put("verified", matched);
            response.put("similarity", maxSimilarity);
            response.put("threshold", threshold);
            response.put("message", matched ? 
                "Face verified successfully." : 
                "Face verification failed. Similarity: " + String.format("%.3f", maxSimilarity) + 
                ", Required: " + threshold);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Error during face verification: " + e.getMessage());
            e.printStackTrace();
            response.put("verified", false);
            response.put("message", "Server error during verification: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } finally {
            // Clean up temporary file
            if (tempFile != null && tempFile.exists()) {
                boolean deleted = tempFile.delete();
                System.out.println("Temporary file cleanup: " + (deleted ? "Success" : "Failed"));
            }
        }
    }

    // Helper method to normalize embeddings
    private float[] normalizeEmbedding(float[] embedding) {
        float sum = 0;
        for (float val : embedding) {
            sum += val * val;
        }
        float norm = (float) Math.sqrt(sum);
        
        if (norm == 0) return embedding;
        
        float[] normalized = new float[embedding.length];
        for (int i = 0; i < embedding.length; i++) {
            normalized[i] = embedding[i] / norm;
        }
        return normalized;
    }
}