package com.example.demo.Controllers;



import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.demo.Entites.AdminFacialEmbedding;
import com.example.demo.Repos.AdminFacialEmbeddingRepository;
import com.example.demo.Services.OnnxEmbeddingService;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminLoginController {

    @Autowired
    private AdminFacialEmbeddingRepository adminEmbeddingRepo;

    @Autowired
    private OnnxEmbeddingService onnxEmbeddingService;

    @PostMapping(value = "/login", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> loginWithFace(@RequestParam("image") MultipartFile imageFile) {
        Map<String, Object> response = new HashMap<>();
        File tempFile = null;

        try {
            System.out.println("Admin login verification started...");

            if (imageFile.isEmpty()) {
                response.put("success", false);
                response.put("message", "No image uploaded.");
                return ResponseEntity.badRequest().body(response);
            }

            // Save the image to a temporary file
            String extension = ".jpg";
            String originalFilename = imageFile.getOriginalFilename();
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            tempFile = File.createTempFile("admin_login", extension);
            imageFile.transferTo(tempFile);

            // Extract embedding from live image
            float[] liveEmbedding = onnxEmbeddingService.extractSingleEmbedding(tempFile, "admin@gmail.com");
            if (liveEmbedding == null || liveEmbedding.length == 0) {
                response.put("success", false);
                response.put("message", "Face not detected or embedding failed.");
                return ResponseEntity.ok(response);
            }

            float[] normalizedLiveEmbedding = normalizeEmbedding(liveEmbedding);
            List<AdminFacialEmbedding> storedEmbeddings = adminEmbeddingRepo.findByAdminEmail("admin@gmail.com");

            if (storedEmbeddings.isEmpty()) {
                response.put("success", false);
                response.put("message", "No admin facial data available.");
                return ResponseEntity.ok(response);
            }

            boolean matched = false;
            double maxSimilarity = 0.0;
            double threshold = 0.8;

            for (int i = 0; i < storedEmbeddings.size(); i++) {
                float[] stored = EmbeddingUtils.byteArrayToFloatArray(storedEmbeddings.get(i).getEmbeddingVector());
                double similarity = EmbeddingUtils.cosineSimilarity(stored, normalizedLiveEmbedding);
                if (similarity > maxSimilarity) maxSimilarity = similarity;
                if (similarity > threshold) {
                    matched = true;
                    break;
                }
            }

            response.put("success", matched);
            response.put("similarity", maxSimilarity);
            response.put("message", matched ? 
                "Admin login successful." : 
                "Face mismatch. Similarity: " + String.format("%.3f", maxSimilarity));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);

        } finally {
            if (tempFile != null && tempFile.exists()) {
                boolean deleted = tempFile.delete();
                System.out.println("Temp file cleanup: " + (deleted ? "done" : "failed"));
            }
        }
    }

    private float[] normalizeEmbedding(float[] embedding) {
        float sum = 0;
        for (float val : embedding) sum += val * val;
        float norm = (float) Math.sqrt(sum);
        if (norm == 0) return embedding;

        float[] normalized = new float[embedding.length];
        for (int i = 0; i < embedding.length; i++) {
            normalized[i] = embedding[i] / norm;
        }
        return normalized;
    }
}

