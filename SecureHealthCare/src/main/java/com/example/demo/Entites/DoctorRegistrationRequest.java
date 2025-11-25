package com.example.demo.Entites;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public class DoctorRegistrationRequest {
    
    private Hospital hosp;
    private String hospitalName;
    private String doctorName;
    private String phoneNumber;
    private String email;
    private String password;
    private String address;
    private String department;
    private Integer experience;
    private List<MultipartFile> images; 
    
    public DoctorRegistrationRequest() {}
    
    public Hospital getHosp() {
		return hosp;
	}

	public void setHosp(Hospital hosp) {
		this.hosp = hosp;
	}
	public String getDoctorName() {
        return doctorName;
    }
    
    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getDepartment() {
        return department;
    }
    
    public void setDepartment(String department) {
        this.department = department;
    }
    
    public Integer getExperience() {
        return experience;
    }
    
    public void setExperience(Integer experience) {
        this.experience = experience;
    }
    
    public String getHospitalName() {
		return hospitalName;
	}

	public void setHospitalName(String hospitalName) {
		this.hospitalName = hospitalName;
	}

	public List<MultipartFile> getImages() {
        return images;
    }
    
    public void setImages(List<MultipartFile> images) {
        this.images = images;
    }
}

