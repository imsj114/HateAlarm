package com.example.madcampweek2.api;

import com.example.madcampweek2.model.Post;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface RetroApi {
    // @GET( EndPoint-자원위치(URI) )
    @GET("posts/{post}")
    Call<Post> getPosts(@Path("post") String post);
}
