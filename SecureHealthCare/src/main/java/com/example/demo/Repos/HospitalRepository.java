package com.example.demo.Repos;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.Entites.Hospital;

public interface HospitalRepository extends JpaRepository<Hospital, Long> {

	Optional<Hospital> findByHospitalNameAndAddress(String hospitalName, String address);

	Optional<Hospital> findByHospitalName(String hospitalName);

	Optional<Hospital> findByEmail(String email);

}
