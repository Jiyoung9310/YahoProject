package com.android.yaho.repository

import android.util.Log
import com.android.yaho.UserClimbingData
import com.android.yaho.local.YahoPreference
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

interface LoginRepository {
    fun getUserID(): String?
    suspend fun saveUserID(uid: String): Flow<LoginResult>
    suspend fun updateNewUser(): Flow<LoginResult>
}

@ExperimentalCoroutinesApi
class LoginRepositoryImpl(private val auth: FirebaseAuth,
                          private val firestoreDB: FirebaseFirestore
): LoginRepository, KoinComponent {
    override fun getUserID(): String? = get<YahoPreference>().userId ?: auth.uid

    override suspend fun saveUserID(uid: String): Flow<LoginResult> = callbackFlow {
        get<YahoPreference>().userId = uid

        val subscription = firestoreDB.collection("users")
            .document(uid)
            .addSnapshotListener { snapshot, _ ->
                if(snapshot != null && snapshot.exists()) {
                    offer(LoginResult.LoginSuccess)
                } else {
                    offer(LoginResult.NoLoginData)
                }
            }

        awaitClose { subscription.remove() }

    }

    override suspend fun updateNewUser(): Flow<LoginResult> = callbackFlow {
        if(!get<YahoPreference>().userId.isNullOrEmpty()) {
            firestoreDB.collection("users")
                .document(get<YahoPreference>().userId!!)
                .set(UserClimbingData())
                .addOnSuccessListener { documentReference ->
                    Log.d("LoginRepository","DocumentSnapshot added with ID: ${documentReference}")
                    offer(LoginResult.NewUserSignUp)
                }
                .addOnFailureListener { e ->
                    Log.w("LoginRepository","Error adding document", e)
                    offer(LoginResult.SignUpFail(e))
                }
        }
    }
}

sealed class LoginResult {
    object LoginSuccess : LoginResult()
    object NoLoginData: LoginResult()
    object NewUserSignUp : LoginResult()
    class SignUpFail(val e: Throwable): LoginResult()
}