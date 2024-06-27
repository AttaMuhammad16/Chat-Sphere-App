package com.atta.chatspherapp.utils

import android.content.Context
import android.util.Log
import com.atta.chatspherapp.R
import com.atta.chatspherapp.utils.NewUtils.getAccessToken
import com.google.auth.oauth2.GoogleCredentials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject

object SendNotification {
//    access token expire in every 1 hour

    suspend fun sendMessageNotification(
        userName: String,
        message: String,
        token: String,
        accessToken:String
    ) {
        val client = OkHttpClient()
        val mediaType = "application/json".toMediaTypeOrNull()

        val jsonNotif = JSONObject().apply {
            put("title", userName)
            put("body", message)
        }

        val jsonData = JSONObject().apply {
            put("title", userName)
            put("body", message)
            put("click_action", "target_main")
        }

        val wholeObj = JSONObject().apply {
            put("message", JSONObject().apply {
                put("token", token)
                put("notification", jsonNotif)
                put("data", jsonData)
                put("android", JSONObject().apply {
                    put("priority", "high")
                })
            })
        }

        val requestBody = wholeObj.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url("https://fcm.googleapis.com/v1/projects/chat-spher-app/messages:send")
            .post(requestBody)
            .addHeader("Authorization", "Bearer $accessToken")
            .addHeader("Content-Type", "application/json")
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("Failed to send notification: ${e.message}")
            }
            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                try {
                    val responseJson = JSONObject(responseBody ?: "{}")
                    val success = responseJson.optInt("success", 0)
                    Log.i("TAG", "success $success. Response: $responseBody")
                } catch (e: Exception) {
                    println("Failed to send notification: ${e.message}")
                    Log.i("TAG", "Failed to send notification: ${e.message}")
                }
            }
        })
    }


}