package com.climbing.yaho.repository

import android.util.Log
import com.climbing.yaho.data.UserData
import com.climbing.yaho.local.YahoPreference
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

interface LoginRepository {
    fun getUserID(): String?
    suspend fun saveUserID(uid: String): Flow<LoginResult>
}

@ExperimentalCoroutinesApi
class LoginRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestoreDB: FirebaseFirestore,
    private val yahoPreference: YahoPreference,
): LoginRepository {
    override fun getUserID(): String? {
        val uid = yahoPreference.userId ?: auth.uid
        Log.d("LoginRepository", "getUserID $uid")
        return uid
    }

    override suspend fun saveUserID(uid: String): Flow<LoginResult> = callbackFlow {
        yahoPreference.userId = uid

        var eventsCollection: DocumentReference? = null
        try {
            eventsCollection = firestoreDB
                .collection("users").document(uid)
                .collection("total").document(uid)
        } catch (e: Throwable) {
            offer(LoginResult.SignUpFail(e))
            close(e)
        }

        val subscription = eventsCollection?.get()
            ?.addOnSuccessListener {
                if(it.exists()) {
                    offer(LoginResult.LoginSuccess)
                    Log.d("LoginRepository", "LoginSuccess")
                } else {
                    eventsCollection.set(UserData())
                        .addOnSuccessListener {
                            offer(LoginResult.NewUserSignUp)
                            Log.d("LoginRepository", "OnSuccess NewUserSignUp")
                        }
                }
            }?.addOnFailureListener { e : Throwable ->
                offer(LoginResult.SignUpFail(e))
                Log.w("LoginRepository", "saveUserID error : ${e.message}")
            }

        awaitClose { (subscription as ListenerRegistration).remove() }

    }

}

sealed class LoginResult {
    object LoginSuccess : LoginResult()
    object NoLoginData: LoginResult()
    object NewUserSignUp : LoginResult()
    class SignUpFail(val e: Throwable): LoginResult()
}