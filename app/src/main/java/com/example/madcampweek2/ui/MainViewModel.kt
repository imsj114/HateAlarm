package com.example.madcampweek2.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.madcampweek2.model.Contact

class MainViewModel : ViewModel() {
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

    }
}