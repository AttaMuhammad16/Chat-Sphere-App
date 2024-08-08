package com.atta.chatspherapp.data.auth.google

import android.app.Activity
import com.atta.chatspherapp.utils.MyResult
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth

interface SignInWIthGoogle {
    suspend fun signInWithGoogleFirebase(context: Activity, idToken:String): MyResult<String>
}