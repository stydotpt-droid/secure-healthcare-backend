package com.example.demo.Entites;



import java.util.List;



public class PatientDTO {

    private String fullName;
    private String dob;
    private String gender;
    private String email;
    private String phoneNumber;
    private String address;
    private String password;

    private String existingReportPdf;

    // Getters and Setters

    public String getFullName() {
        return fullName;
    }

    public String getExistingReportPdf() {
		return existingReportPdf;
	}

	public void setExistingReportPdf(String existingReportPdf) {
		this.existingReportPdf = existingReportPdf;
	}

	public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    
}
