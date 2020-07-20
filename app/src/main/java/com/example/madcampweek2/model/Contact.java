package com.example.madcampweek2.model;

import com.google.gson.annotations.SerializedName;

public class Contact {
    @SerializedName("phoneNumber")
    private String phoneNumber;

    @SerializedName("name")
    private String name;

    @SerializedName("image")
    private String profile;

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

    public String getProfile()  { return profile; }

    public void setProfile(String profile)  { this.profile = profile; }

    public Image toImage() { return new Image(profile); }
}