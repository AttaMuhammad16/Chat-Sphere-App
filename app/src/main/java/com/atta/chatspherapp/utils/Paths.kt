package com.atta.chatspherapp.utils

import com.google.firebase.auth.FirebaseAuth

object Paths {
    fun getPhoneNumberPath(auth:FirebaseAuth):String = "Users/${auth.currentUser!!.uid}/phone"

}