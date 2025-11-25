package com.example.demo.Entites;

import java.util.Map;

public class CreateReportDTO {
	 private Long appointmentId;
	    private String reportType;
	    private String patientName;
	    private String patientPhone;
	    private String patientAddress;
	    private String diseaseCategory;
	    private String doctorEmail;
	    private String doctorName;
	    private String notes;
	    private Map<String, String> parameters;
	    
	    // Constructors
	    public CreateReportDTO() {}
	    
	    // Getters and Setters
	    public Long getAppointmentId() { return appointmentId; }
	    public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }
	    
	    public String getReportType() { return reportType; }
	    public void setReportType(String reportType) { this.reportType = reportType; }
	    
	    public String getPatientName() { return patientName; }
	    public void setPatientName(String patientName) { this.patientName = patientName; }
	    
	    public String getPatientPhone() { return patientPhone; }
	    public void setPatientPhone(String patientPhone) { this.patientPhone = patientPhone; }
	    
	    public String getPatientAddress() { return patientAddress; }
	    public void setPatientAddress(String patientAddress) { this.patientAddress = patientAddress; }
	    
	    public String getDiseaseCategory() { return diseaseCategory; }
	    public void setDiseaseCategory(String diseaseCategory) { this.diseaseCategory = diseaseCategory; }
	    
	    public String getDoctorEmail() { return doctorEmail; }
	    public void setDoctorEmail(String doctorEmail) { this.doctorEmail = doctorEmail; }
	    
	    public String getDoctorName() { return doctorName; }
	    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
	    
	    public String getNotes() { return notes; }
	    public void setNotes(String notes) { this.notes = notes; }
	    
	    public Map<String, String> getParameters() { return parameters; }
	    public void setParameters(Map<String, String> parameters) { this.parameters = parameters; }
}
