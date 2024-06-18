package com.atta.chatspherapp.data.auth.email

import com.atta.chatspherapp.utils.MyResult


interface AuthRepositoryWithEmail {
    suspend fun registerUser(email:String,password:String): MyResult<String>
    suspend fun loginUser(email:String, password:String): MyResult<String>

}