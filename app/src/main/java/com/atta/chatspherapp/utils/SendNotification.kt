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

//    access token expire in every 12 hour
    var accessToken = "ya29.c.c0AY_VpZhiVCG-ba-0GIXcu5ss0iSa2svwHTCTPAFa09H1nZSW1JzwkvhBfwwPLLtaGNxKhmuBWJe41pqj-O272pUSvGulYQw8ffrsNFpATp92n51-w-mEBCSyWGASjBDKyYi7wLPVZd9LtES1lM-1M_SUpUh5HU_SS6B6FckMw0Emt1GXOCIfCnbXEaFqEMqWkuciiHYcaDx9EHhs8AfMIozxN6V5D_ZQjIRlD8To_-OUutdjQErpppEpTjY8d91bOk-06C6BUfLMrFVTO03tiuGO1LQvMMO5M_j_Y0LwDSy7sW6--K1ceKGtCg1BgyArB0H2VyaEwqh14lAAsZzkoiyuHxqJeXkb0qSa93Wr0_F7DblOyAANslylZAN387PpfarB2kwzWBjmruzaqFsj2RqvM0515JYijOXylFWaJqM31g1jni84r245O2SzyoiBeo1s9avnUvbBOa6mg-Z-71p0XtbcZptVc6O_Qbgrds0yxrtaB_a0fk96hWtnmvM98Xh8SdzIsl3SwhpFvbZZoZv9g9cif1kSbIBqnZ8kUR7USncYz3QystOhVj-qk0Q_dm409OnJ4gXFf121__1XIYiMwWYfRV-hyp-mBiFfsl5ksgpYetsa8j0qvQoFsMbx7pbcYXe4ZmbpYU6pmFcqaa_hQQQVZggbroc__4_75h9zvpBhROa9ntv7IvrO-cm62snQyQgWVc97ctg67OQgVX7dg7VIwISv7WcQFvzJ4nht-jezMp4m20stbsMk06c_UoWQo8wdZsplpmo7r_-vr45g5_qRQZWsvfwmzbkd33oB4thYe9c8XuB8k_4XyIFzjiole6m9Xi0uB4mW6bgIwajzsiIyIzx-c1t85ekooIxfWcJtqR2Sv7rbdyr1bdzxOwIBZbo9WeZ48qtwJf9R_gOOeyVYwy2ZwOd65Q0gI8rO1pb5d0v6_oO2xw4_ouo-6I_BrQp0O81nRV8aadMZ72BxtwFyrdY8Baey3dton7bJl29so9QrfOt"

    suspend fun sendMessageNotification(
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