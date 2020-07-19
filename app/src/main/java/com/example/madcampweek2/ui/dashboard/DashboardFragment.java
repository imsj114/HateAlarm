package com.example.madcampweek2.ui.dashboard;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.madcampweek2.R;
import com.example.madcampweek2.api.RetroApi;
import com.example.madcampweek2.model.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.content.ContentValues.TAG;

public class DashboardFragment extends Fragment {
    private String BASE_URL = "https://jsonplaceholder.typicode.com/";
    private String testurl = "http://192.249.19.240:3080/";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);

        final TextView textView = root.findViewById(R.id.text_dashboard);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(testurl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RetroApi retroApi = retrofit.create(RetroApi.class);


        return root;
    }
}
