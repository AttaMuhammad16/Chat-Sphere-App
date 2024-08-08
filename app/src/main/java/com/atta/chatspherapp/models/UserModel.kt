package com.atta.chatspherapp.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserModel(
    var key: String = "",
    var profileUrl: String = "",
    var coverUrl: String = "",
    var timeStamp: Long = 0,
    var token: String = "",
    var activityState:Boolean=false,
    var chattingWith:String="",
    var status:String="",
    var fullName: String? = "",
    var email:String?="",
    var password:String=""
) : Parcelable
