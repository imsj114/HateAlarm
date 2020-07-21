package com.example.madcampweek2.model;

public class Image {

    public Image(String _url){
        this.Url = "http://192.249.19.240:3080/api/images/get/" + _url;
        this.filename = _url;
    }

    private String filename;
    private String Url;

    public String getFilename() { return filename; }
    public String getUrl() { return Url; }

    public void setFilename(String filename) { this.filename = filename; }
    public void setUrl(String url) { this.Url = url; }

}
