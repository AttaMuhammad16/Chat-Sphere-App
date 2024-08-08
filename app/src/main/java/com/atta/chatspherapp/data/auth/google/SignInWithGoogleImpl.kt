package com.atta.chatspherapp.data.auth.google

import android.app.Activity
import com.atta.chatspherapp.utils.MyResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import java.lang.Exception
import javax.inject.Inject

class SignInWithGoogleImpl @Inject constructor(private val auth: FirebaseAuth):SignInWIthGoogle {

    override suspend fun signInWithGoogleFirebase(context: Activity, idToken: String, ): MyResult<String> {
        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
        return try {
            auth.signInWithCredential(firebaseCredential).await()
            MyResult.Success("Successfully SignIn")
        }catch (e: Exception){
            MyResult.Error(e.message.toString())
        }
    }


}
