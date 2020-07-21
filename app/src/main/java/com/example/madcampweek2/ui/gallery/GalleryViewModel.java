package com.example.madcampweek2.ui.gallery;


import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.madcampweek2.api.RetroApi;
import com.example.madcampweek2.model.Image;
import com.example.madcampweek2.model.User;
import com.facebook.Profile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GalleryViewModel extends ViewModel {

    private String BASE_URL = "http://192.249.19.240:3080/";
    private String profileId;
    private String TAG = "TAG";

    private Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    private RetroApi retroApi = retrofit.create(RetroApi.class);


    //this is the data that we will fetch asynchronously
    private MutableLiveData<List<Image>> _Images;

    //we will call this method to get the data
    public LiveData<List<Image>> getImages() {
        //if the list is null
            if (_Images == null) {
            _Images = new MutableLiveData<List<Image>>();
            //we will load it asynchronously from server in this method
            loadImages(profileId);
        }
        //finally we will return the list
        return _Images;
    }

    // This method is using Retrofit to get the Images list of the given Uid user
    // @GET getUserImages(:uid)
    private void loadImages(String uid) {
        Call<List<String>> call = retroApi.getUserImages(uid);

        call.enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                if(response.isSuccessful()){
                    List<String> result = response.body();
                    Log.d(TAG, "ViewModel getUserImages Succeess uid: " + uid);
                    List<Image> list = new ArrayList<Image>();
                    for(String url : result){
                        list.add(new Image(url));
                    }
                    _Images.setValue(list);
                } else{
                    Log.d(TAG, "ViewModel getUserImages Fail");
                }
            }

            @Override
            public void onFailure(Call<List<String>> call, Throwable t) {
                Log.d(TAG, "ViewModel getUserImages Fail:" + t.getMessage());
            }
        });
    }

    private void loginToServer(String uid){
        Call<String> call = retroApi.loginUser(uid);

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if(response.isSuccessful()){
                    String result = response.body();
                    Log.d(TAG, "ViewModel login Succeess: " + result);
                } else{
                    String result = response.body();
                    Log.d(TAG, "ViewModel login Fail: " + result);
                    registerToServer();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.d(TAG, "ViewModel login no response:" + t.getMessage());
            }
        });
    }

    private void registerToServer(){
        User new_user = new User();
        new_user.setUid(profileId);

        Call<User> call = retroApi.registerUser(new_user);

        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if(response.isSuccessful()){
                    User result = response.body();
                    Log.d(TAG, "ViewModel register Succeess: " + result.toString());
                } else{
                    Log.d(TAG, "ViewModel register Fail");
                    registerToServer();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.d(TAG, "ViewModel register Fail:" + t.getMessage());
            }
        });
    }

    public void ReloadImages(String uid){
        loginToServer(uid);
        loadImages(uid);
    }


    public void addImage(File file){

        RequestBody requestFile =
                RequestBody.create(MediaType.parse("multipart/form-data"), file);

        // MultipartBody.Part is used to send also the actual file name
        MultipartBody.Part body =
                MultipartBody.Part.createFormData("image", file.getName(), requestFile);

//        // add another part within the multipart request
//        RequestBody fullName =
//                RequestBody.create(MediaType.parse("multipart/form-data"), "Your Name");

        Call<String> call = retroApi.addImage(Profile.getCurrentProfile().getId(), body);

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if(response.isSuccessful()){
                    String result = response.body();
                    Log.d(TAG, "ViewModel addImage Succeess: " + result);
                    loadImages(profileId);
                } else{
                    Log.d(TAG, "ViewModel addImage Fail");
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.d(TAG, "ViewModel addImage Fail:" + t.getMessage());
            }
        });
    }

    public void setProfileId(String uid) { this.profileId = uid; }

}
