package com.example.madcampweek2.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.madcampweek2.model.Contact
import com.example.madcampweek2.model.Image

class MainViewModel : ViewModel() {
    val _contacts: MutableLiveData<List<Contact>> by lazy {
        MutableLiveData<List<Contact>>().also {
            loadContacts(it)
        }
    }

    private val _images: LiveData<List<Image>> by lazy {
        Transformations.map(_contacts) { list ->
            list.map{ it.toImage() }
        }
    }

    fun getContacts(): LiveData<List<Contact>> {
        return _contacts
    }

    fun getImages(): LiveData<List<Image>> {
        return _images
    }

    private fun loadContacts(data: MutableLiveData<List<Contact>>) {
        // Do an asynchronous operation to fetch users.

    }
}