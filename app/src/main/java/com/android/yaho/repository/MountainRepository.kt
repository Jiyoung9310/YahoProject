package com.android.yaho.repository

import com.android.yaho.data.MountainData
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow


interface MountainRepository {
    suspend fun getNearBy(latitude: Double, longitude: Double) : Flow<List<MountainData>>
}

class MountainRepositoryImpl(private val firestoreDB: FirebaseFirestore) : MountainRepository {

    @ExperimentalCoroutinesApi
    override suspend fun getNearBy(latitude: Double, longitude: Double): Flow<List<MountainData>> = callbackFlow {
        val maxLat = latitude + 0.1
        val minLat = latitude - 0.1
        val maxLong = longitude + 0.1
        val minLong = longitude - 0.1
        val subscription = firestoreDB.collection("mountains")
            .whereLessThanOrEqualTo("latitude", maxLat)
            .whereGreaterThanOrEqualTo("latitude", minLat)
//            .whereLessThanOrEqualTo("longitude", maxLong)
//            .whereGreaterThanOrEqualTo("longitude", minLong)
            .addSnapshotListener { snapshot, error ->
                if(snapshot != null && !snapshot.isEmpty) {
                    val data = snapshot.toObjects(MountainData::class.java)
                    offer(data)
                } else {
                    offer(emptyList<MountainData>())
                }
            }
        awaitClose { subscription.remove() }
    }
}