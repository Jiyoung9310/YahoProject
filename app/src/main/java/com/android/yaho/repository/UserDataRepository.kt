package com.android.yaho.repository

import android.util.Log
import com.android.yaho.data.UserClimbingData
import com.android.yaho.local.YahoPreference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

interface UserDataRepository {
    fun getUserData(): Flow<UserClimbingData>
}

class UserDataRepositoryImpl(private val firestoreDB: FirebaseFirestore) : UserDataRepository, KoinComponent {
    @ExperimentalCoroutinesApi
    override fun getUserData(): Flow<UserClimbingData> = callbackFlow {
        val uid = get<YahoPreference>().userId
        if(uid == null) offer(UserClimbingData())

        val subscription = firestoreDB
            .collection("users").document(uid!!)
            .collection("total").document(uid)
            .get()
            .addOnSuccessListener { snapshot ->
                try {
                    snapshot.toObject(UserClimbingData::class.java)?.let {
                        offer(it)
                        Log.d("UserDataRepository", "getUserData : $it")
                    }
                } catch (e: Throwable) {
                    offer(UserClimbingData())
                    Log.w("UserDataRepository", "firestore error : ${e.message}")
                    return@addOnSuccessListener
                }
            }
        awaitClose { (subscription as ListenerRegistration).remove() }
    }
}