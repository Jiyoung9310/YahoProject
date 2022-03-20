package com.climbing.yaho.repository

import android.util.Log
import com.climbing.yaho.data.MountainData
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject


interface MountainRepository {
    fun getMountainList() : Flow<List<MountainData>>
}

class MountainRepositoryImpl @Inject constructor(
    private val firestoreDB: FirebaseFirestore
) : MountainRepository {
    @ExperimentalCoroutinesApi
    override fun getMountainList(): Flow<List<MountainData>> = callbackFlow {
        var eventCollection: CollectionReference? = null
        try{
            eventCollection = firestoreDB.collection("mountains")
        } catch (e: Throwable) {
            close(e)
        }

        val subscription = eventCollection?.addSnapshotListener { snapshot, error ->
            if(snapshot == null) { return@addSnapshotListener }
            try {
                val data = snapshot.toObjects(MountainData::class.java)
                Log.w("MountainRepository", "getMountainList : $data")
                offer(data)
            } catch (e: Throwable) {
                Log.w("MountainRepository", "firestore error : ${e.message}")
                return@addSnapshotListener
            }
        }
        awaitClose { subscription?.remove() }
    }
}