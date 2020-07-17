package com.example.madcampweek2.model

import com.example.madcampweek2.R
import com.example.madcampweek2.ui.contact.Contact

class User {
    fun toContact() : Contact {
        return Contact().apply {
            phoneNumber = "000-0000-0000"
            name = "test"
            profile = R.drawable.frodo
        }
    }
}