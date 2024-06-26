package com.atta.chatspherapp.utils

import android.util.Log
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

object SendNotification {

    var accessToken = "ya29.c.c0AY_VpZh_PtBTg0A3ibOuPPRJgec3B-cf2KbILc5QB_UxQS8fnvKwKqYuiZAQIEDZzzuDT23JjzAWwPHkrmz6l9ibQngXnb5e62XDS82yDfjSiESeoZAuc9O_iUcfU02oalmkwDmI7iuMrXA_oPTlbDu-HMRK_beoM9gHEAS-x8qQa_DI_ip6BL-hBSEuK3QZ8KGtoONfA1qhhqXxdhplm3N8jJKptOqdOktRyeB8RJ_NmIdOjhftqq2AKn0SyVmA3mTqSMGi-mAlk0brLWKaTEV7-jpkzTpXAg8BdyqpIUtxreFOuUKHLu1CQtxch8HpYOj3hX9TleD2yTf4QUWG43vr0wDauR3rtWKmEXjyHfKUcv5llAQsC_TyE385Aci15z_fv7n3-mmRirac8_Q5i5-mMq8o0Xo3bly0YB-XxmYwXZ3eyOjQWvvQX6goxUVQRr6jtsdnxpSQMRFqFvUR1YUhQ9OS_O6OxwVYJdJW6ti439m9RguYx6iOz0hjdYcpzsYmfldwf1uMyx40iRvYmB3XYo00ow63xdnffXcIsm8ybMwUjZOFqOm4fhQmk8iMzk6mruW2Yen64pfV5YZi-Yuhg8dF4sp72pno16dxdn5q12ZxMY1xzFV_1p7-Yheald7RMwOkw0az1c26xiixrern4s_paF4wfpF-Zn_k1Q7ymczSU2v4Qh_y-IaRiOXze8rJ_4-9J8Smtqxdzqrv-7XW90hbIlsnIRXhs3rl6cUBbRypBIufOF6tn3b7qs6B6vJ6ioSbBXvd4bioRZRtJIidpgt1ssg7oJUeZa7-vQS-3t4XtIms8X_9nnF0cIwzrhakSyb5Wm92I6wy0ad78XMyufl8hFdMyyfuwntQ8UmwX_gYe29z9cWlmI8r6jM8Uigcb-fIhq0gcJ5z3VdkRfY4WarYZobc9ZueqFzcYBVhJhVhf0nm7R6JSe0lc9xFovgoBfcnk_8_x7r85vsx6yaV65gy8s1q7gkxahU24FXXk2RYpfm6_nh"

    suspend fun sendNotification(
        userName: String,
        message: String,
        token: String,
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
            put("click_action", "target_2")
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