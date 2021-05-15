package com.covvaccine.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Session {
    private String date;
    @JsonProperty("available_capacity")
    private int availableCapacity;
    @JsonProperty("min_age_limit")
    private int minAgeLimit;
    private String vaccine;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getAvailableCapacity() {
        return availableCapacity;
    }

    public void setAvailableCapacity(int availableCapacity) {
        this.availableCapacity = availableCapacity;
    }

    public int getMinAgeLimit() {
        return minAgeLimit;
    }

    public void setMinAgeLimit(int minAgeLimit) {
        this.minAgeLimit = minAgeLimit;
    }

    public String getVaccine() {
        return vaccine;
    }

    public void setVaccine(String vaccine) {
        this.vaccine = vaccine;
    }
}
