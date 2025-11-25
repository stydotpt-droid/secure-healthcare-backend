package com.example.demo.Repos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.Entites.FacialEmbedding;

public interface FacialEmbeddingRepo extends JpaRepository<FacialEmbedding, Long> {

	List<FacialEmbedding> findByDoctorId(Long id);
 
}
