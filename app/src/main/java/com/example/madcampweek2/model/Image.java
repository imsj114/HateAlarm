package com.example.madcampweek2.model;

public class Image {

    public Image(String _url){
        this.Url = "http://192.249.19.240:3080/api/images/get/" + _url;
    }

    private String Url;

    public String getUrl() { return Url; }

    public void setUrl(String url) { this.Url = url; }

}
