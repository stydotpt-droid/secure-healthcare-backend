package com.example.demo.Repos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.Entites.AdminFacialEmbedding;

public interface AdminFacialEmbeddingRepository extends JpaRepository<AdminFacialEmbedding, Long> {

	List<AdminFacialEmbedding> findByAdminEmail(String string);

}
