package com.android.yaho.repository

import android.util.Log
import com.android.yaho.data.UserClimbingData
import com.android.yaho.local.YahoPreference
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
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

        var eventsCollection: DocumentReference? = null
        try {
            eventsCollection = firestoreDB.collection("users").document(uid)
        } catch (e: Throwable) {
            close(e)
        }

        val subscription = eventsCollection?.addSnapshotListener { snapshot, _ ->
            if (snapshot == null || !snapshot.exists()) {
                offer(LoginResult.NoLoginData)
                return@addSnapshotListener
            }
            try {

                offer(LoginResult.LoginSuccess)
                Log.w("LoginRepository", "LoginSuccess")
            } catch (e: Throwable) {
                Log.w("LoginRepository", "saveUserID error : ${e.message}")
            }
        }

        awaitClose { subscription?.remove() }

    }

    override suspend fun updateNewUser(): Flow<LoginResult> = callbackFlow {
        val uid = get<YahoPreference>().userId
        if(!uid.isNullOrEmpty()) {
            val script = firestoreDB.collection("users")
                .document(uid)
                .collection("total")
                .add(UserClimbingData())
                .addOnSuccessListener { documentReference ->
                    Log.w("LoginRepository", "DocumentSnapshot added with ID: $documentReference")
                    if(documentReference != null) {
                        offer(LoginResult.NewUserSignUp)
                    } else {
                        offer(LoginResult.SignUpFail(Throwable("유저 Document 생성 실패")))
                    }
                }
                .addOnFailureListener { e ->
                    Log.w("LoginRepository", "Error adding document", e)
                    offer(LoginResult.SignUpFail(e))
                }
            awaitClose { (script as ListenerRegistration).remove() }
        }
    }
}

sealed class LoginResult {
    object LoginSuccess : LoginResult()
    object NoLoginData: LoginResult()
    object NewUserSignUp : LoginResult()
    class SignUpFail(val e: Throwable): LoginResult()
}