package com.example.demo.Controllers;

import java.time.LocalDateTime;

public class SharedReportDTO {
    private Long reportId;
    private String reportType;
    private LocalDateTime createdAt;
    private String pdfBase64;
    private String patientName;
    private String patientPhone;
    private String originalHospital;
    private String doctorName;
    private String doctorHospital;
    
    
    
    
	public SharedReportDTO(Long reportId, String reportType, LocalDateTime createdAt, String pdfBase64,
			String patientName, String patientPhone, String originalHospital, String doctorName,
			String doctorHospital) {
		super();
		this.reportId = reportId;
		this.reportType = reportType;
		this.createdAt = createdAt;
		this.pdfBase64 = pdfBase64;
		this.patientName = patientName;
		this.patientPhone = patientPhone;
		this.originalHospital = originalHospital;
		this.doctorName = doctorName;
		this.doctorHospital = doctorHospital;
	}
	public Long getReportId() {
		return reportId;
	}
	public void setReportId(Long reportId) {
		this.reportId = reportId;
	}
	public String getReportType() {
		return reportType;
	}
	public void setReportType(String reportType) {
		this.reportType = reportType;
	}
	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
	public String getPdfBase64() {
		return pdfBase64;
	}
	public void setPdfBase64(String pdfBase64) {
		this.pdfBase64 = pdfBase64;
	}
	public String getPatientName() {
		return patientName;
	}
	public void setPatientName(String patientName) {
		this.patientName = patientName;
	}
	public String getPatientPhone() {
		return patientPhone;
	}
	public void setPatientPhone(String patientPhone) {
		this.patientPhone = patientPhone;
	}
	public String getOriginalHospital() {
		return originalHospital;
	}
	public void setOriginalHospital(String originalHospital) {
		this.originalHospital = originalHospital;
	}
	public String getDoctorName() {
		return doctorName;
	}
	public void setDoctorName(String doctorName) {
		this.doctorName = doctorName;
	}
	public String getDoctorHospital() {
		return doctorHospital;
	}
	public void setDoctorHospital(String doctorHospital) {
		this.doctorHospital = doctorHospital;
	}

    // constructor, getters, setters
    
    
}

