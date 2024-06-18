package com.atta.chatspherapp.data.auth.googleauth

import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.startActivityForResult
import com.atta.chatspherapp.data.auth.googleauth.AuthRepositoryWithGoogle
import com.atta.chatspherapp.utils.Constants.REQUEST_CODE_GOOGLE_SIGN_IN
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import javax.inject.Inject

class AuthRepositoryWithGoogleImpl @Inject constructor(private val auth:FirebaseAuth): AuthRepositoryWithGoogle {

    private lateinit var googleSignInClient: GoogleSignInClient

    override fun requestGoogleSignIn(activity: AppCompatActivity,serverClientId:String) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(serverClientId)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(activity, gso)
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(activity , signInIntent, REQUEST_CODE_GOOGLE_SIGN_IN , null)
    }

    override fun signInWithGoogle(activity: AppCompatActivity , account: GoogleSignInAccount? , callback:(task:Task<AuthResult>) ->Unit) {
        val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(activity) { task ->
                callback(task)
            }
    }
}

