package com.example.birchbark.model

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class OwnerWaitlistRepository {

    private val db = FirebaseFirestore.getInstance()

    fun observeWaitlist(
        lotteryId: String,
        onResult: (List<String>) -> Unit,
        onFailure: (Exception) -> Unit
    ): ListenerRegistration {
        return db.collection("lotteries")
            .document(lotteryId)
            .collection("waitlist")
            .addSnapshotListener { snapshot, error ->
                when {
                    error != null -> onFailure(error)
                    snapshot != null -> {
                        val ids = snapshot.documents.map { document ->
                            document.getString("userId")
                                ?.takeIf { it.isNotBlank() }
                                ?: document.id
                        }
                        onResult(ids.distinct())
                    }
                }
            }
    }

    fun loadEntrantName(
        userId: String,
        onResult: (String?) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                val first = document.getString("firstName").orEmpty().trim()
                val last = document.getString("lastName").orEmpty().trim()
                val username = document.getString("userName").orEmpty().trim()

                val fullName = listOf(first, last)
                    .filter { it.isNotBlank() }
                    .joinToString(" ")

                val displayName = fullName.ifBlank { username }
                onResult(displayName.takeIf { it.isNotBlank() })
            }
            .addOnFailureListener { error ->
                onFailure(error)
            }
    }
}
