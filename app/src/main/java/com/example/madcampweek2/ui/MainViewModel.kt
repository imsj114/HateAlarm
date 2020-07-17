package com.example.madcampweek2.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.madcampweek2.model.User
import com.example.madcampweek2.ui.contact.Contact

class MainViewModel : ViewModel() {
    val _users: MutableLiveData<List<User>> by lazy {
        MutableLiveData<List<User>>().also {
            loadUsers(it)
        }
    }
    private val _contacts: LiveData<List<Contact>> by lazy {
        Transformations.map(_users){
            it.map{ it.toContact() }
        }
    }

    fun getUsers(): LiveData<List<User>> {
        return _users
    }

    fun getContacts(): LiveData<List<Contact>> {
        return _contacts
    }

    private fun loadUsers(data: MutableLiveData<List<User>>) {
        // Do an asynchronous operation to fetch users.

    }
}