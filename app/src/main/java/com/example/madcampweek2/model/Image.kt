package com.example.madcampweek2.model

import android.net.Uri
import com.example.madcampweek2.R


class Image(val profile: Int) {
    fun getPath() : String {
        return Uri.parse(
            "android.resource://" + R::class.java.getPackage().name + "/" + profile
        ).toString()
    }
    val getId = {profile}
}