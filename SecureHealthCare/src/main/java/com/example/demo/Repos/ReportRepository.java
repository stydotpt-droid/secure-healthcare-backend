package com.example.demo.Repos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.Entites.Report;

public interface ReportRepository extends JpaRepository<Report, Long> {

	List<Report> findByPatientEmail(String email);

	List<Report> findByDoctorEmail(String email);

	List<Report> findByDoctorEmailAndShareRequestedTrueAndShareApprovedFalse(String doctorEmail);

	List<Report> findByPatientEmailAndSharedToEmail(String patientEmail, String hospitalEmail);

	List<Report> findBySharedToEmailAndShareApprovedTrue(String hospitalEmail);

}
