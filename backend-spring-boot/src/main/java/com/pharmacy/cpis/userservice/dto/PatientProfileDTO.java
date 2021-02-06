package com.pharmacy.cpis.userservice.dto;

import com.pharmacy.cpis.userservice.model.users.Patient;
import com.pharmacy.cpis.userservice.service.ILoyaltyProgramService;

import javax.validation.constraints.NotEmpty;

public class PatientProfileDTO {

    @NotEmpty(message = "Name is required.")
    private String name;

    @NotEmpty(message = "Surname is required.")
    private String surname;

    @NotEmpty(message = "Phone number is required.")
    private String phoneNumber;

    @NotEmpty(message = "Address is required.")
    private String address;

    @NotEmpty(message = "City is required.")
    private String city;

    @NotEmpty(message = "Country is required.")
    private String country;

    @NotEmpty(message = "Email is required.")
    private String email;

    @NotEmpty(message = "Loyalty points are required.")
    private Integer loyaltyPoints;

    @NotEmpty(message = "User category is required.")
    private UserCategoryDTO userCategoryDTO;

    public PatientProfileDTO() {

    }

    public PatientProfileDTO(Patient patient, PatientEmailDTO patientEmail, ILoyaltyProgramService loyaltyProgramService){
        this.setName(patient.getName());
        this.setSurname(patient.getSurname());
        this.setAddress(patient.getAddress());
        this.setCountry(patient.getCountry());
        this.setCity(patient.getCity());
        this.setPhoneNumber(patient.getPhoneNumber());
        this.setLoyaltyPoints(patient.getLoyaltyPoints());
        this.setEmail(patientEmail.getEmail());
        this.setUserCategoryDTO(new UserCategoryDTO(loyaltyProgramService.findUserCategoryByLoyaltyPoints(patient.getLoyaltyPoints())));


    }
    public PatientProfileDTO(String name, String surname, String phoneNumber, String address, String city, String country, String email, Integer loyaltyPoints, UserCategoryDTO userCategoryDTO) {
        this.name = name;
        this.surname = surname;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.city = city;
        this.country = country;
        this.email = email;
        this.loyaltyPoints = loyaltyPoints;
        this.userCategoryDTO = userCategoryDTO;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
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

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getLoyaltyPoints() {
        return loyaltyPoints;
    }

    public void setLoyaltyPoints(Integer loyaltyPoints) {
        this.loyaltyPoints = loyaltyPoints;
    }

    public UserCategoryDTO getUserCategoryDTO() {
        return userCategoryDTO;
    }

    public void setUserCategoryDTO(UserCategoryDTO userCategoryDTO) {
        this.userCategoryDTO = userCategoryDTO;
    }
}