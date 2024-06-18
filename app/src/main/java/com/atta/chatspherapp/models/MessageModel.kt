package com.chatsphera.models

import kotlin.reflect.full.memberProperties


data class MessageModel (
    var key:String = "",
    var senderName:String = "",
    var senderImageUrl:String = "",
    var senderPhone:String = "",
    var message:String = "",
    var timeStamp:Long = 0,
    var senderUid:String = "",
    var receiverUid:String = "",
    var blockList:ArrayList<String> = arrayListOf(),
    var reactions:ArrayList<String> = arrayListOf(),
    var seen:Boolean = false,
    var delivered:Boolean = false,
    var forwarded:Boolean = false,
    var imageUrl:String = "",
    var voiceUrl:String = "",
    var documentUrl:String = "",
    var location:String = "",
    var referenceMessage:String = "",
){
    fun shrink(): Map<String, Any> {
        val propertiesMap = mutableMapOf<String, Any>()
        this::class.memberProperties.forEach { prop ->
            val value = prop.call(this)
            when (prop.returnType.toString()) {
                "kotlin.Boolean" ->propertiesMap[prop.name] = value as Any
                "kotlin.collections.List<kotlin.Any>" -> if ((value as List<*>).isNotEmpty()) propertiesMap[prop.name] = value
                "kotlin.Int" -> if (value as Int != 0) propertiesMap[prop.name] = value
                "kotlin.Double" -> if (value as Double != 0.0) propertiesMap[prop.name] = value
                "kotlin.Float" -> if (value as Float != 0.0f) propertiesMap[prop.name] = value
                "kotlin.Long" -> if (value as Long != 0L) propertiesMap[prop.name] = value
                "kotlin.collections.ArrayList<kotlin.Any> /* = java.util.ArrayList<kotlin.Any> */" -> if ((value as ArrayList<*>).isNotEmpty()) propertiesMap[prop.name] = value
                "kotlin.String" -> if (value as String != "") propertiesMap[prop.name] = value
            }
        }
        return propertiesMap
    }
}