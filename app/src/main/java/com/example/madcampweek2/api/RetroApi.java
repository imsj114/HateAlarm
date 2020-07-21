package com.example.madcampweek2.api;

import com.example.madcampweek2.model.Contact;
import com.example.madcampweek2.model.Image;
import com.example.madcampweek2.model.User;

import java.io.File;
import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface RetroApi {

    // Post : add a contact to user contacts on server
    @POST("api/contacts/post/{uid}")
    Call<Contact> addContact(@Path("uid") String uid, @Body Contact contact);

    // Post : register in server user database
    @POST("api/register")
    Call<User> registerUser(@Body User User);

    @Multipart
    @POST("api/images/post/{uid}")
    Call<String> addImage(@Path("uid") String uid, @Part MultipartBody.Part imageFile);

    // Post : try "log-in" on server
    @GET("api/login/{uid}")
    Call<String> loginUser(@Path("uid") String uid);

    // Get : load list of all users in database
    @GET("api/users")
    Call<List<User>> getUsers();

    // Get : load user's contacts via uid
    @GET("api/contacts/{uid}")
    Call<List<Contact>> getUserContacts(@Path("uid") String uid);

    // Get : load user's images via uid
    @GET("api/images/{uid}")
    Call<List<Image>> getUserImages(@Path("uid") String uid);

    // Get : load an image file via filename
    @GET("api/images/get/{filename}")
    Call<File> getImage(@Path("filename") String filename);

    // Get : delete an image via user id and filename
    // image file on the server is deleted and unlinked from user's image list
    @GET("api/images/{uid}/{filename}")
    Call<String> deleteImage(@Path("uid") String uid, @Path("filename") String filename);

}
