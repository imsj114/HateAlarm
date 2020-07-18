package com.example.madcampweek2.model;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class User {
    @SerializedName("uid")
    private String uid;

    @SerializedName("name")
    private String name;

    @SerializedName("email")
    private String email;

    @SerializedName("contacts")
    private List<Contact> contacts;     // 본인 주소록

    @SerializedName("images")
    private List<String> images;

    public String getUid() { return uid; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public List<Contact> getContacts() { return contacts; }
    public List<String> getImages() { return images; }

    public void setUid(String uid) { this.uid = uid; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setContacts(List<Contact> contacts) { this.contacts = contacts; }
    public void setImages(List<String> images) { this.images = images; }

    @NonNull
    @Override
    public String toString() {
        return "Result: " + this.uid ;
    }
}

