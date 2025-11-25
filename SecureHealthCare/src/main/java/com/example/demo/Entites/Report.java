package com.example.demo.Entites;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "reports")

public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long appointmentId;

    private String patientEmail;
    private String doctorEmail;

    private String diseaseCategory;
    private String reportType;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String parametersJson;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String prescriptionText;

    @Lob
    private byte[] filePdf;

    private String sha256Hash;

    private LocalDateTime createdAt;

    // New fields for sharing logic
    private boolean shareRequested = false;      // Patient requested to share
    private boolean shareApproved = false;       // Doctor approved the request
    private String sharedToEmail;                // Email of new hospital or doctor
    private LocalDateTime shareTimestamp;  
    
    public Report() {
		// TODO Auto-generated constructor stub
	}
    
    
    
    
    
	public Report(Long id, Long appointmentId, String patientEmail, String doctorEmail, String diseaseCategory,
			String reportType, String parametersJson, String prescriptionText, byte[] filePdf, String sha256Hash,
			LocalDateTime createdAt, boolean shareRequested, boolean shareApproved, String sharedToEmail,
			LocalDateTime shareTimestamp) {
		super();
		this.id = id;
		this.appointmentId = appointmentId;
		this.patientEmail = patientEmail;
		this.doctorEmail = doctorEmail;
		this.diseaseCategory = diseaseCategory;
		this.reportType = reportType;
		this.parametersJson = parametersJson;
		this.prescriptionText = prescriptionText;
		this.filePdf = filePdf;
		this.sha256Hash = sha256Hash;
		this.createdAt = createdAt;
		this.shareRequested = shareRequested;
		this.shareApproved = shareApproved;
		this.sharedToEmail = sharedToEmail;
		this.shareTimestamp = shareTimestamp;
	}





	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getAppointmentId() {
		return appointmentId;
	}
	public void setAppointmentId(Long appointmentId) {
		this.appointmentId = appointmentId;
	}
	public String getPatientEmail() {
		return patientEmail;
	}
	public void setPatientEmail(String patientEmail) {
		this.patientEmail = patientEmail;
	}
	public String getDoctorEmail() {
		return doctorEmail;
	}
	public void setDoctorEmail(String doctorEmail) {
		this.doctorEmail = doctorEmail;
	}
	public String getDiseaseCategory() {
		return diseaseCategory;
	}
	public void setDiseaseCategory(String diseaseCategory) {
		this.diseaseCategory = diseaseCategory;
	}
	public String getReportType() {
		return reportType;
	}
	public void setReportType(String reportType) {
		this.reportType = reportType;
	}
	public String getParametersJson() {
		return parametersJson;
	}
	public void setParametersJson(String parametersJson) {
		this.parametersJson = parametersJson;
	}
	public String getPrescriptionText() {
		return prescriptionText;
	}
	public void setPrescriptionText(String prescriptionText) {
		this.prescriptionText = prescriptionText;
	}
	public byte[] getFilePdf() {
		return filePdf;
	}
	public void setFilePdf(byte[] filePdf) {
		this.filePdf = filePdf;
	}
	public String getSha256Hash() {
		return sha256Hash;
	}
	public void setSha256Hash(String sha256Hash) {
		this.sha256Hash = sha256Hash;
	}
	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
	public boolean isShareRequested() {
		return shareRequested;
	}
	public void setShareRequested(boolean shareRequested) {
		this.shareRequested = shareRequested;
	}
	public boolean isShareApproved() {
		return shareApproved;
	}
	public void setShareApproved(boolean shareApproved) {
		this.shareApproved = shareApproved;
	}
	public String getSharedToEmail() {
		return sharedToEmail;
	}
	public void setSharedToEmail(String sharedToEmail) {
		this.sharedToEmail = sharedToEmail;
	}
	public LocalDateTime getShareTimestamp() {
		return shareTimestamp;
	}
	public void setShareTimestamp(LocalDateTime shareTimestamp) {
		this.shareTimestamp = shareTimestamp;
	}
    
    
    
    
    
    
}

