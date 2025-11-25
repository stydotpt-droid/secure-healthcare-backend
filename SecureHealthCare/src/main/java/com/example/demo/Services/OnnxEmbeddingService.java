package com.example.demo.Services;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import org.springframework.stereotype.Service;
import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;

@Service
public class OnnxEmbeddingService {
    private final OrtEnvironment env = OrtEnvironment.getEnvironment();
    private final OrtSession session;

    public OnnxEmbeddingService() throws OrtException, IOException {
        String modelPath = "C:\\Users\\Dell\\Downloads\\facenet.onnx";
        session = env.createSession(modelPath, new OrtSession.SessionOptions());
        System.out.println("ONNX Model loaded successfully from: " + modelPath);
    }

    public Map<String, float[]> extractEmbeddings(String userImageDir, String userEmail) throws Exception {
        Map<String, float[]> embeddingsMap = new HashMap<>();
        File dir = new File(userImageDir);
        File[] files = dir.listFiles((d, name) -> 
            name.toLowerCase().endsWith(".jpg") || 
            name.toLowerCase().endsWith(".png") || 
            name.toLowerCase().endsWith(".jpeg"));
            
        if (files == null || files.length == 0) {
            throw new IllegalArgumentException("User image directory empty or invalid: " + userImageDir);
        }

        for (File imgFile : files) {
            try {
                float[][][][] imgTensor = preprocessImage(imgFile);
                OnnxTensor inputTensor = OnnxTensor.createTensor(env, imgTensor);
                OrtSession.Result results = session.run(Collections.singletonMap("input", inputTensor));
                float[][] embedding = (float[][]) results.get(0).getValue();
                embeddingsMap.put(imgFile.getName(), embedding[0]);
                
                inputTensor.close();
                results.close();
            } catch (Exception e) {
                System.err.println("Failed to process image: " + imgFile.getName() + ", Error: " + e.getMessage());
            }
        }
        return embeddingsMap;
    }

    private float[][][][] preprocessImage(File imgFile) throws IOException {
        System.out.println("Preprocessing image: " + imgFile.getName());
        
        BufferedImage img = ImageIO.read(imgFile);
        if (img == null) {
            throw new IOException("Could not read image file: " + imgFile.getAbsolutePath());
        }
        
        System.out.println("Original image dimensions: " + img.getWidth() + "x" + img.getHeight());
        
        BufferedImage resizedImg = resize(img, 160, 160);
        float[][][][] input = new float[1][160][160][3];

        // Improved preprocessing with proper normalization
        for (int y = 0; y < 160; y++) {
            for (int x = 0; x < 160; x++) {
                int rgb = resizedImg.getRGB(x, y);
                Color color = new Color(rgb);
                
                // Normalize to [-1, 1] range (common for FaceNet models)
                input[0][y][x][0] = (color.getRed() / 127.5f) - 1.0f;
                input[0][y][x][1] = (color.getGreen() / 127.5f) - 1.0f;
                input[0][y][x][2] = (color.getBlue() / 127.5f) - 1.0f;
            }
        }
        
        System.out.println("Image preprocessing completed");
        return input;
    }

    private BufferedImage resize(BufferedImage img, int w, int h) {
        // Use better quality scaling
        Image tmp = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
        BufferedImage resized = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resized.createGraphics();
        
        // Enable antialiasing for better quality
        g2d.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, 
                           java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, 
                           java.awt.RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, 
                           java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();
        return resized;
    }

    public float[] extractSingleEmbedding(File tempFile, String email) {
        OnnxTensor inputTensor = null;
        OrtSession.Result results = null;
        
        try {
            System.out.println("Extracting embedding for file: " + tempFile.getName());
            
            if (!tempFile.exists() || tempFile.length() == 0) {
                System.err.println("File does not exist or is empty: " + tempFile.getAbsolutePath());
                return null;
            }
            
            float[][][][] imgTensor = preprocessImage(tempFile);
            inputTensor = OnnxTensor.createTensor(env, imgTensor);
            results = session.run(Collections.singletonMap("input", inputTensor));
            float[][] embedding = (float[][]) results.get(0).getValue();
            
            if (embedding == null || embedding.length == 0 || embedding[0] == null) {
                System.err.println("Invalid embedding output from model");
                return null;
            }
            
            System.out.println("Successfully extracted embedding of length: " + embedding[0].length);
            return embedding[0];
            
        } catch (OrtException | IOException e) {
            System.err.println("Error extracting embedding: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            // Clean up resources
            try {
                if (inputTensor != null) inputTensor.close();
                if (results != null) results.close();
            } catch (Exception e) {
                System.err.println("Error cleaning up ONNX resources: " + e.getMessage());
            }
        }
    }
}

