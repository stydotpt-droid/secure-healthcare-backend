package com.example.demo.Services;



import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.Controllers.EmbeddingUtils;
import com.example.demo.Entites.AdminFacialEmbedding;
import com.example.demo.Repos.AdminFacialEmbeddingRepository;

@Service
public class AdminEmbeddingService {

    @Autowired
    private OnnxEmbeddingService onnxEmbeddingService;

    @Autowired
    private AdminFacialEmbeddingRepository embeddingRepository;

    public void initializeAdminEmbeddings(String imageDirPath) throws Exception {
        File imageDir = new File(imageDirPath);
        if (!imageDir.exists() || !imageDir.isDirectory()) {
            throw new IllegalArgumentException("Admin image directory not found: " + imageDirPath);
        }

        File[] imageFiles = imageDir.listFiles((dir, name) ->
                name.toLowerCase().endsWith(".jpg") ||
                name.toLowerCase().endsWith(".jpeg") ||
                name.toLowerCase().endsWith(".png"));

        if (imageFiles == null || imageFiles.length != 15) {
            throw new IllegalArgumentException("Expected 15 admin images, found: " +
                                               (imageFiles == null ? 0 : imageFiles.length));
        }

        for (File file : imageFiles) {
            float[] embedding = onnxEmbeddingService.extractSingleEmbedding(file, "admin@gmail.com");
            if (embedding == null) continue;

            float[] normalized = normalizeEmbedding(embedding);
            byte[] byteArray = EmbeddingUtils.floatArrayToByteArray(normalized);

            AdminFacialEmbedding afe = new AdminFacialEmbedding();
            afe.setAdminEmail("admin@gmail.com");
            afe.setEmbeddingVector(byteArray);

            embeddingRepository.save(afe);
            System.out.println("Stored embedding for: " + file.getName());
        }
    }

    private float[] normalizeEmbedding(float[] embedding) {
        float sum = 0f;
        for (float val : embedding) {
            sum += val * val;
        }
        float norm = (float) Math.sqrt(sum);
        if (norm == 0f) return embedding;

        float[] normalized = new float[embedding.length];
        for (int i = 0; i < embedding.length; i++) {
            normalized[i] = embedding[i] / norm;
        }
        return normalized;
    }
}

