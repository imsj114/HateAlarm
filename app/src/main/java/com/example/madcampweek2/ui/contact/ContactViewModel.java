package com.example.madcampweek2.ui.contact;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.madcampweek2.api.RetroApi;
import com.example.madcampweek2.model.Contact;
import com.example.madcampweek2.model.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.content.ContentValues.TAG;

public class ContactViewModel extends ViewModel {

    private String BASE_URL = "http://192.249.19.240:3080/";
    private String profileId;

    private Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    private RetroApi retroApi = retrofit.create(RetroApi.class);


    //this is the data that we will fetch asynchronously
    private MutableLiveData<List<Contact>> _contacts;

    //we will call this method to get the data
    public LiveData<List<Contact>> getContacts() {
        //if the list is null
        if (_contacts == null) {
            _contacts = new MutableLiveData<List<Contact>>();
            //we will load it asynchronously from server in this method
            loadContacts(profileId);
        }
        //finally we will return the list
        return _contacts;
    }

    // This method is using Retrofit to get the Contacts list of the given Uid user
    // @GET getUserContacts(:uid)
    private void loadContacts(String uid) {
        Call<List<Contact>> call = retroApi.getUserContacts(uid);

        call.enqueue(new Callback<List<Contact>>() {
            @Override
            public void onResponse(Call<List<Contact>> call, Response<List<Contact>> response) {
                if(response.isSuccessful()){
                    List<Contact> result = response.body();
                    Log.d(TAG, "ViewModel getUserContacts Succeess uid: " + uid);
                    _contacts.setValue(result);
                } else{
                    Log.d(TAG, "ViewModel getUserContacts Fail");
                }
            }

            @Override
            public void onFailure(Call<List<Contact>> call, Throwable t) {
                Log.d(TAG, "ViewModel getUserContacts Fail:" + t.getMessage());
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
                    registerToServer(uid);
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.d(TAG, "ViewModel login no response:" + t.getMessage());
            }
        });
    }

    private void registerToServer(String uid){
        User new_user = new User();
        new_user.setUid(uid);

        Call<User> call = retroApi.registerUser(new_user);

        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if(response.isSuccessful()){
                    User result = response.body();
                    Log.d(TAG, "ViewModel register Succeess: " + result.toString());
                } else{
                    Log.d(TAG, "ViewModel register Fail");
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.d(TAG, "ViewModel register Fail:" + t.getMessage());
            }
        });
    }

    public void ReloadContacts(String uid){
        loginToServer(uid);
        loadContacts(uid);
    }

    public void addContact(Contact contact){
        Call<Contact> call = retroApi.addContact(profileId, contact);

        call.enqueue(new Callback<Contact>() {
            @Override
            public void onResponse(Call<Contact> call, Response<Contact> response) {
                if(response.isSuccessful()){
                    Contact result = response.body();
                    Log.d(TAG, "ViewModel addContact Succeess: " + result.toString());
                    loadContacts(profileId);
                } else{
                    Log.d(TAG, "ViewModel addContact Fail");
                }
            }

            @Override
            public void onFailure(Call<Contact> call, Throwable t) {
                Log.d(TAG, "ViewModel addContact Fail:" + t.getMessage());
            }
        });
    }

    public void setProfileId(String uid) { this.profileId = uid;}

}
