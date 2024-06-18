package com.atta.chatspherapp.data.auth.googleauth

import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult

interface AuthRepositoryWithGoogle {
    fun requestGoogleSignIn(activity: AppCompatActivity, serverClientId:String)
    fun signInWithGoogle(activity: AppCompatActivity, account: GoogleSignInAccount?, callback:(task: Task<AuthResult>) ->Unit)
}