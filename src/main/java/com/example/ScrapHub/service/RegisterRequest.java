package com.example.ScrapHub.service;

public class RegisterRequest {
    private String name;
    private String phoneNumber;
    private String password;
    private String companyName; // for consumer only
    private int pinCode;

    // Default constructor
    public RegisterRequest() {}

    // Parameterized constructor
    public RegisterRequest(String name, String phoneNumber, String password, String companyName, int pinCode) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.companyName = companyName;
        this.pinCode = pinCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public int getPinCode() {
        return pinCode;
    }

    public void setPinCode(int pinCode) {
        this.pinCode = pinCode;
    }
}
