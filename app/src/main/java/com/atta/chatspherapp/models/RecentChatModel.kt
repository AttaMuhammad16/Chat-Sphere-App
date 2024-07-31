package com.atta.chatspherapp.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class RecentChatModel(
    var key:String="",
    var recentMessage:String="",
    var messageType:String="",
    var numberOfMessages:Int=0,
    var timeStamp:Long=0,
    var userModel: UserModel= UserModel()
) : Parcelable