package com.example.demo.Services;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.Entites.Doctor;
import com.example.demo.Entites.DoctorRegistrationRequest;
import com.example.demo.Entites.FacialEmbedding;
import com.example.demo.Entites.Hospital;
import com.example.demo.Repos.DoctorRepo;
import com.example.demo.Repos.FacialEmbeddingRepo;
import com.example.demo.Repos.HospitalRepository;

import ai.onnxruntime.OrtException;


@Service
public class DoctorService {

    @Autowired
    private DoctorRepo doctorRepository;

    
    @Autowired
    private OnnxEmbeddingService onnxEmbeddingService;

    @Autowired
    private FacialEmbeddingRepo facialEmbeddingRepository;
    
    @Autowired
    
    private HospitalRepository hospitalRepository;

    private static final String DATASET_ROOT = "dataset";

    public Doctor registerDoctor(DoctorRegistrationRequest request) throws IOException, OrtException {
        // Check if email already exists
        if (doctorRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists: " + request.getEmail());
        }
        Optional<Hospital> hosp = hospitalRepository.findByHospitalName(request.getHospitalName());
        // Create doctor entity
        Doctor doctor = new Doctor();
        doctor.setHospitalName(request.getHospitalName());
        doctor.setDoctorName(request.getDoctorName());
        doctor.setPhoneNumber(request.getPhoneNumber());
        doctor.setEmail(request.getEmail());
        doctor.setPassword(request.getPassword()); // Consider hashing this
        doctor.setAddress(request.getAddress());
        doctor.setDepartment(request.getDepartment());
        doctor.setExperience(request.getExperience());
        doctor.setHospital(hosp.get());
        // Store first image as profile image in database
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            MultipartFile firstImage = request.getImages().get(0);
            doctor.setProfileImage(firstImage.getBytes());
        }

        // Save doctor to get ID
        doctor = doctorRepository.save(doctor);

        if (request.getImages() != null && !request.getImages().isEmpty()) {
            // Save images to folder and get list of saved files
            List<File> savedFiles = saveImagesToFolder(request.getImages(), doctor.getDoctorName());
            doctor.setTotalImagesCount(savedFiles.size());

            // For each saved image, extract embedding and save to FacialEmbedding repo
            for (File imageFile : savedFiles) {
                float[] embedding = onnxEmbeddingService.extractSingleEmbedding(imageFile, doctor.getEmail());

                if (embedding != null && embedding.length > 0) {
                    // Normalize the embedding before storing
                    float[] normalizedEmbedding = normalizeEmbedding(embedding);
                    
                    FacialEmbedding facialEmbedding = new FacialEmbedding();
                    byte[] embeddingBytes = floatArrayToByteArray(normalizedEmbedding);
                    facialEmbedding.setEmbeddingVector(embeddingBytes);
                    facialEmbedding.setDoctor(doctor);

                    facialEmbeddingRepository.save(facialEmbedding);
                    System.out.println("Saved embedding for doctor: " + doctor.getEmail() + 
                                     ", embedding length: " + normalizedEmbedding.length);
                } else {
                    System.err.println("Failed to extract embedding from image: " + imageFile.getName());
                }
            }
        }

        return doctorRepository.save(doctor);
    }

    // Helper method to normalize embeddings
    private float[] normalizeEmbedding(float[] embedding) {
        float sum = 0;
        for (float val : embedding) {
            sum += val * val;
        }
        float norm = (float) Math.sqrt(sum);
        
        if (norm == 0) return embedding; // Avoid division by zero
        
        float[] normalized = new float[embedding.length];
        for (int i = 0; i < embedding.length; i++) {
            normalized[i] = embedding[i] / norm;
        }
        return normalized;
    }

    // Helper method to convert float[] to byte[]
    private byte[] floatArrayToByteArray(float[] floats) {
        ByteBuffer buffer = ByteBuffer.allocate(floats.length * 4);
        buffer.order(ByteOrder.LITTLE_ENDIAN);  // or ByteOrder.BIG_ENDIAN based on your needs
        for (float f : floats) {
            buffer.putFloat(f);
        }
        return buffer.array();
    }
    
    private List<File> saveImagesToFolder(List<MultipartFile> images, String doctorName) throws IOException {
        List<File> savedFiles = new ArrayList<>();

        // Sanitize doctor name for folder name
        String sanitizedDoctorName = doctorName.replaceAll("\\s+", "_");

        // Define paths
        Path datasetPath = Paths.get("dataset");
        Path doctorFolderPath = datasetPath.resolve(sanitizedDoctorName);

        // Create directories if not exist
        if (!Files.exists(datasetPath)) {
            Files.createDirectories(datasetPath);
        }
        if (!Files.exists(doctorFolderPath)) {
            Files.createDirectories(doctorFolderPath);
        }

        // Save each image
        for (int i = 0; i < images.size(); i++) {
            MultipartFile image = images.get(i);
            String fileName = String.format("%s_image_%d.jpg", sanitizedDoctorName, i + 1);
            Path targetLocation = doctorFolderPath.resolve(fileName);

            // Write file to disk
            try {
                Files.write(targetLocation, image.getBytes());
                savedFiles.add(targetLocation.toFile());  // Add saved file to list
            } catch (IOException e) {
                throw new IOException("Failed to write image file: " + fileName, e);
            }
        }

        return savedFiles;  // Return list of saved files
    }


    private String createDoctorFolderName(Doctor doctor) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        return String.format("%s_%s_%s",
                doctor.getDoctorName().replaceAll("\\s+", "_"),
                doctor.getHospitalName().replaceAll("\\s+", "_"),
                timestamp);
    }

    public Optional<Doctor> findByEmail(String email) {
        return doctorRepository.findByEmail(email);
    }

    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    public Optional<Doctor> findById(Long id) {
        return doctorRepository.findById(id);
    }

    public void deleteDoctor(Long id) {
        Optional<Doctor> doctorOpt = doctorRepository.findById(id);
        if (doctorOpt.isPresent()) {
            Doctor doctor = doctorOpt.get();

            // Optionally delete the doctor's image folder
            String doctorFolderName = createDoctorFolderName(doctor);
            Path doctorFolderPath = Paths.get(DATASET_ROOT, doctorFolderName);

            if (Files.exists(doctorFolderPath)) {
                try {
                    deleteDirectory(doctorFolderPath);
                } catch (IOException e) {
                    System.err.println("Failed to delete doctor image folder: " + e.getMessage());
                }
            }

            // Delete from DB
            doctorRepository.deleteById(id);
        }
    }

    // Utility method to delete a directory recursively
    private void deleteDirectory(Path path) throws IOException {
        if (Files.exists(path)) {
            Files.walk(path)
                 .sorted(java.util.Comparator.reverseOrder())
                 .forEach(p -> {
                     try {
                         Files.delete(p);
                     } catch (IOException e) {
                         System.err.println("Failed to delete file: " + p);
                     }
                 });
        }
    }

}
