package com.example.demo.Entites;



import jakarta.persistence.*;

@Entity
@Table(name = "admin_facial_embeddings")
public class AdminFacialEmbedding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String adminEmail;

    @Lob
    @Column(name = "embedding_vector", columnDefinition = "BLOB", nullable = false)
    private byte[] embeddingVector;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public String getAdminEmail() {
        return adminEmail;
    }

    public void setAdminEmail(String adminEmail) {
        this.adminEmail = adminEmail;
    }

    public byte[] getEmbeddingVector() {
        return embeddingVector;
    }

    public void setEmbeddingVector(byte[] embeddingVector) {
        this.embeddingVector = embeddingVector;
    }
}

