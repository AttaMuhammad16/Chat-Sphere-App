package com.atta.chatspherapp.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class MessageModel (

    // message
    var key:String = "",
    var senderName:String = "",
    var senderImageUrl:String = "",
    var senderPhone:String = "",
    var message:String = "",
    var timeStamp:Long = 0,
    var currentAudioPosition:Int = 0,
    var senderUid:String = "",
    var blockList:ArrayList<String> = arrayListOf(),
    var imageUrl:String = "",
    var voiceUrl:String = "",
    var documentUrl:String = "",
    var documentFileName:String="",
    var videoUrl:String="",

    // likes
    var like:Int=0,
    var heart:Int=0,
    var surprise:Int=0,
    var happy:Int=0,
    var angry:Int=0,

    // delete
    var deleteMessageFromMe:Boolean=false,
    var deletedMessagesList:ArrayList<String> = arrayListOf(),

    // reference name
    var referenceMessageSenderName:String="",
    //for text
    var referenceMessage:String="",
    // for img
    var referenceImgUrl:String="",
    // for document
    var referenceDocumentName:String="",
    // forVideoUrl
    var referenceVideoUrl:String="",
//    id
    var referenceMessageId:String="",
//    for voice
    var referenceVoiceUrl:String="",
    var fcmToken:String=""
): Parcelable


data class ReactionModel(
    var senderKey:String="",
    var reactionId:Int=0,
    var senderName:String="",
    var senderImageUrl:String="",

)
