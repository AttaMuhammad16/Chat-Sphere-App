package com.atta.chatspherapp.ui.viewmodel

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import com.atta.chatspherapp.data.auth.email.AuthRepositoryWithEmail
import com.atta.chatspherapp.data.auth.google.SignInWIthGoogle
import com.atta.chatspherapp.data.auth.phone.AuthRepositoryWithPhone
import com.atta.chatspherapp.utils.MyResult
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class AuthViewModel @Inject constructor(private val authRepositoryWithEmail: AuthRepositoryWithEmail, private val authRepositoryWithPhone: AuthRepositoryWithPhone,private val signInWIthGoogle: SignInWIthGoogle) : ViewModel() {

    // Firebase Auth (with email)
    suspend fun registerWithEmailAndPassword(email:String,password:String): MyResult<String> {
        return withContext(Dispatchers.IO){authRepositoryWithEmail.registerUser(email, password)}
    }

    suspend fun loginWithEmailAndPassword(email:String, password:String): MyResult<String> {
        return withContext(Dispatchers.IO){authRepositoryWithEmail.loginUser(email, password)}
    }




    // signIn With google

    // call it to start Login
    fun signInWithGoogle(context: Activity,GOOGLEREQUESTCODE:Int,mGoogleSignInClient: GoogleSignInClient) {
        val signInIntent = mGoogleSignInClient.signInIntent
        context.startActivityForResult(signInIntent,GOOGLEREQUESTCODE)
    }

    suspend fun signInWithGoogleFirebase(context: Activity, idToken: String): MyResult<String> {
        return signInWIthGoogle.signInWithGoogleFirebase(context,idToken)
    }





        // Firebase Auth (with phone number)
    suspend fun signInWithPhone(phone:String, context: Activity, phoneAuthCredential:(PhoneAuthCredential)->Unit, authVerificationFailed:(String)->Unit, codeSend:(verificationId: String, token: PhoneAuthProvider.ForceResendingToken)->Unit): PhoneAuthProvider.OnVerificationStateChangedCallbacks{
        return withContext(Dispatchers.IO){authRepositoryWithPhone.signInUser(phone, context, phoneAuthCredential, authVerificationFailed, codeSend)}
    }

    suspend fun loginWithPhoneCredential(credential: PhoneAuthCredential): MyResult<String> {
        return withContext(Dispatchers.IO){authRepositoryWithPhone.loginUser(credential)}
    }

    suspend fun resendOtp(token: PhoneAuthProvider.ForceResendingToken, callBack: PhoneAuthProvider.OnVerificationStateChangedCallbacks, context: Activity, phoneNumber: String): MyResult<String> {
        return withContext(Dispatchers.IO){authRepositoryWithPhone.resendOtp(token, callBack, context, phoneNumber)}
    }




}