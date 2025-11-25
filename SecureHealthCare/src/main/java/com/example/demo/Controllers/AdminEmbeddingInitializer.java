package com.example.demo.Controllers;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.demo.Repos.AdminFacialEmbeddingRepository;
import com.example.demo.Services.AdminEmbeddingService;


@Component
public class AdminEmbeddingInitializer implements CommandLineRunner {

    @Autowired
    private AdminEmbeddingService adminEmbeddingService;

    @Autowired
    private AdminFacialEmbeddingRepository embeddingRepository;

    @Override
    public void run(String... args) {
        try {
            if (embeddingRepository.count() > 0) {
                System.out.println("Admin embeddings already initialized. Skipping.");
                return;
            }

            System.out.println("Initializing admin embeddings...");
            adminEmbeddingService.initializeAdminEmbeddings("C://Users//Dell//Documents//workspace-spring-tool-suite-4-4.26.0.RELEASE//SecureHealthCare//AdminImages");
            System.out.println("Admin embeddings successfully initialized.");

        } catch (Exception e) {
            System.err.println("Failed to initialize admin embeddings: " + e.getMessage());
            e.printStackTrace();
        }
    }
}


