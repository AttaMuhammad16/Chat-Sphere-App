package com.atta.chatspherapp.data.auth.phone

import android.app.Activity
import com.atta.chatspherapp.utils.MyResult
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider

interface AuthRepositoryWithPhone {
    suspend fun signInUser(phone:String, context: Activity, phoneAuthCredential:(PhoneAuthCredential)->Unit, authVerificationFailed:(String)->Unit, codeSend:(verificationId: String, token: PhoneAuthProvider.ForceResendingToken)->Unit): PhoneAuthProvider.OnVerificationStateChangedCallbacks
    suspend fun loginUser(credential: PhoneAuthCredential): MyResult<String>
    suspend fun resendOtp(token: PhoneAuthProvider.ForceResendingToken, callBack: PhoneAuthProvider.OnVerificationStateChangedCallbacks, context: Activity, phoneNumber: String): MyResult<String>
}