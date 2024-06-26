package com.atta.chatspherapp.models

data class RecentChatModel(
    var key:String="",
    var recentMessage:String="",
    var messageType:String="",
    var numberOfMessages:Int=0,
    var timeStamp:Long=0,
    var userModel: UserModel= UserModel()
)