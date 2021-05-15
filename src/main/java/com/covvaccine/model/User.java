package com.covvaccine.model;

import java.util.List;

public class User {
    private List<String> pincode;
    private String email;

    public List<String> getPincode() {
        return pincode;
    }

    public void setPincode(List<String> pincode) {
        this.pincode = pincode;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
