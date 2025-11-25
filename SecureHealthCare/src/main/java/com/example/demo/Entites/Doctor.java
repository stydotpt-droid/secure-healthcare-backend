package com.example.demo.Entites;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "doctors")
public class Doctor {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "hospital_name", nullable = false)
    private String hospitalName;
    
    @Column(name = "doctor_name", nullable = false)
    private String doctorName;
    
    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;
    
    @Column(name = "email", nullable = false, unique = true)
    private String email;
    
    @Column(name = "password", nullable = false)
    private String password;
    
    @Column(name = "address", nullable = false)
    private String address;
    
    @Column(name = "department", nullable = false)
    private String department;
    
    @Column(name = "experience", nullable = false)
    private Integer experience;
    
    @Lob
    @Column(name = "profile_image", columnDefinition = "LONGBLOB")
    private byte[] profileImage;
    
  
    @Column(name = "registration_date")
    private LocalDateTime registrationDate;
    
    @Column(name = "total_images_count")
    private Integer totalImagesCount;
    
    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<FacialEmbedding> facialEmbeddings = new ArrayList<>();
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    @JsonBackReference
    private Hospital hospital;
    
    // Constructors
    public Doctor() {
        this.registrationDate = LocalDateTime.now();
        this.totalImagesCount = 0;
    }
    

    public Doctor(Long id, String hospitalName, String doctorName, String phoneNumber, String email, String password,
			String address, String department, Integer experience, byte[] profileImage, LocalDateTime registrationDate,
			Integer totalImagesCount, List<FacialEmbedding> facialEmbeddings, Hospital hospital) {
		super();
		this.id = id;
		this.hospitalName = hospitalName;
		this.doctorName = doctorName;
		this.phoneNumber = phoneNumber;
		this.email = email;
		this.password = password;
		this.address = address;
		this.department = department;
		this.experience = experience;
		this.profileImage = profileImage;
		this.registrationDate = registrationDate;
		this.totalImagesCount = totalImagesCount;
		this.facialEmbeddings = facialEmbeddings;
		this.hospital = hospital;
	}

	public Hospital getHospital() {
		return hospital;
	}


	public void setHospital(Hospital hospital) {
		this.hospital = hospital;
	}


	public List<FacialEmbedding> getFacialEmbeddings() {
		return facialEmbeddings;
	}

	public void setFacialEmbeddings(List<FacialEmbedding> facialEmbeddings) {
		this.facialEmbeddings = facialEmbeddings;
	}

	public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getHospitalName() {
        return hospitalName;
    }
    
    public void setHospitalName(String hospitalName) {
        this.hospitalName = hospitalName;
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
    
    public byte[] getProfileImage() {
        return profileImage;
    }
    
    public void setProfileImage(byte[] profileImage) {
        this.profileImage = profileImage;
    }
    
   
    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }
    
    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }
    
    public Integer getTotalImagesCount() {
        return totalImagesCount;
    }
    
    public void setTotalImagesCount(Integer totalImagesCount) {
        this.totalImagesCount = totalImagesCount;
    }
}


