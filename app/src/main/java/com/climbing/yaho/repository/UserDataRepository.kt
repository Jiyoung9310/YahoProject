package com.climbing.yaho.repository

import android.util.Log
import com.climbing.yaho.data.UserData
import com.climbing.yaho.local.YahoPreference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

interface UserDataRepository {
    fun getUserData(): Flow<UserData>
    fun updateUserData(userData: UserData): Flow<UserData>
}

class UserDataRepositoryImpl @Inject constructor(
    private val firestoreDB: FirebaseFirestore,
    private val yahoPreference: YahoPreference,
) : UserDataRepository {
    @ExperimentalCoroutinesApi
    override fun getUserData(): Flow<UserData> = callbackFlow {
        val uid = yahoPreference.userId
        if(uid == null) offer(UserData())

        val subscription = firestoreDB
            .collection("users").document(uid!!)
            .collection("total").document(uid)
            .get()
            .addOnSuccessListener { snapshot ->
                try {
                    val totalData = snapshot.toObject(UserData::class.java)
                    if(totalData == null) {
                        offer(UserData())
                        Log.d("UserDataRepository", "totalData isEmpty")
                    } else {
                        offer(totalData)
                        Log.d("UserDataRepository", "get totalData success")
                    }

                } catch (e: Throwable) {
                    offer(UserData())
                    Log.w("UserDataRepository", "firestore error : ${e.message}")
                    return@addOnSuccessListener
                }
            }.addOnFailureListener { e: Throwable ->
                offer(UserData())
                Log.w("UserDataRepository", "firestore fail : ${e.message}")
            }
        awaitClose { (subscription as ListenerRegistration).remove() }
    }
    @ExperimentalCoroutinesApi
    override fun updateUserData(userData: UserData): Flow<UserData> = callbackFlow {
        val uid = yahoPreference.userId!!
        var eventsCollection: DocumentReference? = null
        try {
            eventsCollection = firestoreDB
                .collection("users").document(uid)
                .collection("total").document(uid)
        } catch (e: Throwable) {
            offer(userData)
            close(e)
        }

        val subscription = eventsCollection?.get()
            ?.addOnSuccessListener {
                eventsCollection.set(userData)
                    .addOnSuccessListener {
                        offer(userData)
                        Log.d("UserDataRepository", "OnSuccess UpdateUserData")
                    }
            }?.addOnFailureListener { e : Throwable ->
                offer(userData)
                Log.w("UserDataRepository", "UpdateUserData error : ${e.message}")
            }

        awaitClose { (subscription as ListenerRegistration).remove() }
    }
}