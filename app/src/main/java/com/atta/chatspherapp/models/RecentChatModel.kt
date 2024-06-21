package com.atta.chatspherapp.models

data class RecentChatModel(
    var key:String="",
    var userModel: UserModel=UserModel(),
    var recentMessage:String="",
    var messageType:String="",
    var numberOfMessages:Int=0
)