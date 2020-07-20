package com.example.madcampweek2.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.madcampweek2.api.RetroApi
import com.example.madcampweek2.model.Contact
import com.example.madcampweek2.model.MapUser
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainViewModel : ViewModel() {
    var BASE_URL = "http://192.249.19.240:3080/"


    val _contacts: MutableLiveData<List<Contact>> by lazy {
        MutableLiveData<List<Contact>>().also {
            loadContacts(it)
        }
    }

    fun getContacts(): LiveData<List<Contact>> {
        return _contacts
    }

    private fun loadContacts(data: MutableLiveData<List<Contact>>) {
        // Do an asynchronous operation to fetch users.
        var retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        var retroApi = retrofit.create(RetroApi::class.java)

        val call: Call<List<MapUser>> = retroApi.users


    }
}
