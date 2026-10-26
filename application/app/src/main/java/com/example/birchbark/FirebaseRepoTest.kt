package com.example.birchbark

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class FirebaseRepoTest {
    private val db = Firebase.firestore

    fun testConnection() {
        db.collection("test")
            .document("connection")
            .get()
            .addOnSuccessListener {
                println("Firestore connected successfully.")
            }
            .addOnFailureListener { exception ->
                println("Firestore did not connect: ${exception.message}")
            }
    }
}