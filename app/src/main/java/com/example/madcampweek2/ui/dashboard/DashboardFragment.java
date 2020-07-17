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
import com.example.madcampweek2.model.Post;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import static android.content.ContentValues.TAG;

public class DashboardFragment extends Fragment {
    String BASE_URL = "https://jsonplaceholder.typicode.com/";
    String testurl = "http://192.249.19.240:3080/";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);

        final TextView textView = root.findViewById(R.id.text_dashboard);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RetroApi retroApi = retrofit.create(RetroApi.class);

        Call<Post> call = retroApi.getPosts("1");

        call.enqueue(new Callback<Post>() {
            @Override
            public void onResponse(Call<Post> call, Response<Post> response) {
                if(response.isSuccessful()){
                    Post result = response.body();
                    Toast.makeText(getActivity(),"성공, 결과 \n" + result.toString(), Toast.LENGTH_LONG ).show();
                    Log.d(TAG, "성공, 결과 \n" + result.toString());
                } else{
                    Toast.makeText(getActivity(),"실패", Toast.LENGTH_LONG ).show();
                    Log.d(TAG, "실패");
                }
            }

            @Override
            public void onFailure(Call<Post> call, Throwable t) {
                Toast.makeText(getActivity(),"실패", Toast.LENGTH_LONG ).show();
                Log.d(TAG, "실패:"+t.getMessage());
            }
        });


        return root;
    }
}
