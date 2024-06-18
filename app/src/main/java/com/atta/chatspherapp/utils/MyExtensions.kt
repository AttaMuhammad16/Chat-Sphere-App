package com.atta.chatspherapp.utils

import android.util.Log
import java.util.Date
import kotlin.reflect.full.memberProperties

object MyExtensions {

    fun Any.logT(append:String = "" , tag:String = "TAG"){
        Log.i(tag, "$append:$this")
    }

    fun Any.shrink(): Map<String, Any> {
        val propertiesMap = mutableMapOf<String, Any>()
        this::class.memberProperties.forEach { prop ->
            val value = prop.getter.call(this)
            when (value) {
                is Boolean -> if (value) propertiesMap[prop.name] = value
                is Int -> if (value != 0) propertiesMap[prop.name] = value
                is Double -> if (value != 0.0) propertiesMap[prop.name] = value
                is Float -> if (value != 0.0f) propertiesMap[prop.name] = value
                is Long -> if (value != 0L) propertiesMap[prop.name] = value
                is String -> if (value.isNotEmpty()) propertiesMap[prop.name] = value
                is List<*> -> if (value.isNotEmpty()) propertiesMap[prop.name] = value
                is Short -> if (value != 0.toShort()) propertiesMap[prop.name] = value
                is Byte -> if (value != 0.toByte()) propertiesMap[prop.name] = value
                is Char -> if (value != '\u0000') propertiesMap[prop.name] = value // '\u0000' is the null char
                is Set<*> -> if (value.isNotEmpty()) propertiesMap[prop.name] = value
                is Map<*, *> -> if (value.isNotEmpty()) propertiesMap[prop.name] = value
                is Date -> propertiesMap[prop.name] = value
                is Any -> if (value::class.isData) propertiesMap[prop.name] = value.shrink()
            }
        }
        return propertiesMap
    }


}