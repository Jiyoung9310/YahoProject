package com.climbing.yaho.repository

import android.util.Log
import com.climbing.yaho.data.UserClimbingData
import com.climbing.yaho.local.YahoPreference
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import javax.inject.Inject

interface LoginRepository {
    fun getUserID(): String?
    suspend fun saveUserID(uid: String): Flow<LoginResult>
}

@ExperimentalCoroutinesApi
class LoginRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestoreDB: FirebaseFirestore
): LoginRepository, KoinComponent {
    override fun getUserID(): String? = get<YahoPreference>().userId ?: auth.uid

    override suspend fun saveUserID(uid: String): Flow<LoginResult> = callbackFlow {
        get<YahoPreference>().userId = uid

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
                    Log.w("LoginRepository", "LoginSuccess")
                } else {
                    eventsCollection.set(UserClimbingData())
                        .addOnSuccessListener {
                            offer(LoginResult.NewUserSignUp)
                            Log.w("LoginRepository", "OnSuccess NewUserSignUp")
                        }
                }
            }?.addOnFailureListener { e : Throwable ->
                offer(LoginResult.SignUpFail(e))
                Log.w("LoginRepository", "saveUserID error : ${e.message}")
            }
/*
        val subscription = eventsCollection?.collection("total")?.addSnapshotListener { snapshot, _ ->
            if(snapshot?.documents.isNullOrEmpty()) {
                offer(LoginResult.NoLoginData)
                return@addSnapshotListener
            } else {
                offer(LoginResult.LoginSuccess)
                Log.w("LoginRepository", "LoginSuccess")
            }
            try {
                offer(LoginResult.LoginSuccess)
                Log.w("LoginRepository", "LoginSuccess")
            } catch (e: Throwable) {
                offer(LoginResult.SignUpFail(e))
                Log.w("LoginRepository", "saveUserID error : ${e.message}")
            }
        }*/

        awaitClose { (subscription as ListenerRegistration).remove() }

    }

    /*override suspend fun updateNewUser(): Flow<LoginResult> = callbackFlow {
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
    }*/
}

sealed class LoginResult {
    object LoginSuccess : LoginResult()
    object NoLoginData: LoginResult()
    object NewUserSignUp : LoginResult()
    class SignUpFail(val e: Throwable): LoginResult()
}