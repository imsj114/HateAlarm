package com.example.madcampweek2.model;

public class Contact {
    private String phoneNumber;
    private String name;
    private int profile;

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getProfile()  { return profile; }

    public void setProfile(int profile)  { this.profile = profile; }
}