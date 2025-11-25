package com.example.demo.Repos;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.Entites.Doctor;

public interface DoctorRepo extends JpaRepository<Doctor, Long> {

	boolean existsByEmail(String email);

	Optional<Doctor> findByEmail(String email);

	Optional<Doctor> findByDoctorName(String assignedDoctor);

}
