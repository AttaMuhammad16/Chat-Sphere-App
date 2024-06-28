package com.atta.chatspherapp.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserModel(
    var key: String = "",
    var fullName: String = "",
    var phone: String = "",
    var profileUrl: String = "",
    var coverUrl: String = "",
    var timeStamp: Long = 0,
    var token: String = "",
    var activityState:Boolean=false,
    var chattingWith:String="",
    var status:String=""
) : Parcelable
