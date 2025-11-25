package com.example.demo.Repos;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.Entites.Hospital;
import com.example.demo.Entites.Patient;

public interface PatientRepository extends JpaRepository<Patient, Long> {

	Optional<Patient> findByEmail(String email);
	
	List<Patient> findByHospital(Hospital hosp);

	Optional<Patient> findByEmailAndHospital(String email, Hospital hospital);

}
