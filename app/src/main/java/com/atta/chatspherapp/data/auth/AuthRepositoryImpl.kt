package com.atta.chatspherapp.data.auth

import android.app.Activity
import com.atta.chatspherapp.data.auth.AuthRepository
import com.atta.chatspherapp.utils.MyResult
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(private val auth: FirebaseAuth): AuthRepository {

    override suspend fun signInUser(phone: String,context:Activity,phoneAuthCredential:(PhoneAuthCredential)->Unit,authVerificationFailed:(String)->Unit,codeSend:(verificationId: String, token: PhoneAuthProvider.ForceResendingToken)->Unit): PhoneAuthProvider.OnVerificationStateChangedCallbacks {
        val callBack=object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                phoneAuthCredential(p0)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                authVerificationFailed(e.message!!)
            }
            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                codeSend(verificationId,token)
            }
        }
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(context)
            .setCallbacks(callBack).build()
        PhoneAuthProvider.verifyPhoneNumber(options)
        return callBack
    }

    override suspend fun loginUser(credential: PhoneAuthCredential): MyResult<String> {
        return try {
            auth.signInWithCredential(credential).await()
            MyResult.Success("Login")
        }catch (e:Exception){
            MyResult.Success("${e.message}")
        }
    }

    override suspend fun resendOtp(token: PhoneAuthProvider.ForceResendingToken,callBack:PhoneAuthProvider.OnVerificationStateChangedCallbacks,context:Activity,phoneNumber: String): MyResult<String> {
       return try {
           PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber, 60, TimeUnit.SECONDS, context, callBack, token)
           MyResult.Success("Code send.")
       }catch (e:Exception){
           MyResult.Success("${e.message}")
       }
    }

}

