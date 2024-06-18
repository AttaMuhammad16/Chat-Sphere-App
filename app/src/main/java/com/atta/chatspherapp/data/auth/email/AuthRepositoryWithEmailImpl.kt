package com.atta.chatspherapp.data.auth.email


import com.atta.chatspherapp.utils.MyResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AuthRepositoryWithEmailImpl @Inject constructor(private val auth:FirebaseAuth): AuthRepositoryWithEmail {

    override suspend fun registerUser(email: String, password: String): MyResult<String> = suspendCoroutine { cont ->
        auth.createUserWithEmailAndPassword(email, password).addOnSuccessListener {
            cont.resume(MyResult.Success("Successfully Registered."))
        }.addOnCanceledListener {
            cont.resume(MyResult.Error("Something Wrong or check internet connection."))
        }.addOnFailureListener {
            if (it is FirebaseAuthUserCollisionException) {
                cont.resume(MyResult.Error("User exits choose another email.")) // FirebaseAuthUserCollisionException
            } else {
                cont.resume(MyResult.Error(it.message.toString())) // FirebaseAuthWeakPasswordException
            }
        }
    }

    override suspend fun loginUser(email: String, password: String): MyResult<String> = suspendCoroutine { cont ->
        auth.signInWithEmailAndPassword(email, password).addOnSuccessListener {
            cont.resume(MyResult.Success("Successfully Login."))
        }.addOnFailureListener { exception ->
            when (exception) {
                is FirebaseAuthInvalidUserException -> {
                    cont.resume(MyResult.Error("This user does not exit."))
                }
                is FirebaseAuthInvalidCredentialsException -> {
                    cont.resume(MyResult.Error("Wrong email or password."))
                }
                else -> {
                    cont.resume(MyResult.Error("Something wrong or check email or password."))
                }
            }
        }.addOnCanceledListener {
            cont.resume(MyResult.Error("Something wrong or check internet."))
        }
    }

}