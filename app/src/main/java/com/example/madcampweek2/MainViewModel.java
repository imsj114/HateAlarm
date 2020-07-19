package com.example.madcampweek2;

import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.madcampweek2.api.RetroApi;
import com.example.madcampweek2.model.Contact;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.content.ContentValues.TAG;

public class MainViewModel extends ViewModel {

    private RetroApi retroApi;
    private String BASE_URL = "http://192.249.19.240:3080/";
    private String uid = "kakao";

    //this is the data that we will fetch asynchronously
    private MutableLiveData<List<Contact>> _contacts;

    //we will call this method to get the data
    public LiveData<List<Contact>> getContacts() {
        //if the list is null
        if (_contacts == null) {
            _contacts = new MutableLiveData<List<Contact>>();
            //we will load it asynchronously from server in this method
            loadContacts(uid);
        }
        //finally we will return the list
        return _contacts;
    }


    //This method is using Retrofit to get the JSON data from URL
    private void loadContacts(String uid) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RetroApi retroApi = retrofit.create(RetroApi.class);
        Call<List<Contact>> call = retroApi.getUserContacts(uid);

        call.enqueue(new Callback<List<Contact>>() {
            @Override
            public void onResponse(Call<List<Contact>> call, Response<List<Contact>> response) {
                if(response.isSuccessful()){
                    List<Contact> result = response.body();
                    Log.d(TAG, "ViewModel getUserContacts Succeess\n Result: " + result.toString());
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
}
