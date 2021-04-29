package com.android.yaho.repository

import android.util.Log
import com.android.yaho.data.UserClimbingData
import com.android.yaho.local.YahoPreference
import com.google.firebase.firestore.FirebaseFirestore
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

        val subscription = firestoreDB.collection("users")
            .document(uid!!)
            .collection("total")
            .addSnapshotListener { snapshot, error ->
                try {
                    val data = snapshot?.documents?.firstOrNull()
                    if(data != null) {
                        val totalData = data.toObject(UserClimbingData::class.java)
                        totalData?.let { offer(it) }
                        Log.d("UserDataRepository", "geteUserData : $totalData")
                    }
                } catch (e: Throwable) {
                    Log.w("UserDataRepository", "firestore error : ${e.message}")
                    return@addSnapshotListener
                }
            }
            awaitClose { subscription.remove() }
    }
}