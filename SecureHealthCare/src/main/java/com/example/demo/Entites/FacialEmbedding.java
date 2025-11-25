package com.example.demo.Entites;



import jakarta.persistence.*;

@Entity
@Table(name = "facial_embeddings")
public class FacialEmbedding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(name = "embedding_vector", columnDefinition = "BLOB", nullable = false)
    private byte[] embeddingVector;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    // Constructors
    public FacialEmbedding() {}

    public FacialEmbedding(byte[] embeddingVector, Doctor doctor) {
        this.embeddingVector = embeddingVector;
        this.doctor = doctor;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public byte[] getEmbeddingVector() {
        return embeddingVector;
    }

    public void setEmbeddingVector(byte[] embeddingVector) {
        this.embeddingVector = embeddingVector;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }
}
