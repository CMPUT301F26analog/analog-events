package com.example.birchbark

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.birchbark.ui.theme.BirchBarkTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.auth


// Set this flag to disable firestore access or enable temporary database functionality
private const val ENABLE_FIREBASE_TEST = false

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BirchBarkTheme {

                if (ENABLE_FIREBASE_TEST) {
                    FirebaseTestScreen()
                } else {
                    // Without developer login for database
                    AdminBrowseScreen()
                }
            }
        }
    }
}

@Composable
fun FirebaseTestScreen() {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var status by remember {
        mutableStateOf("Not authenticated")
    }

    Column(
        modifier = Modifier.fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") }
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation()
        )

        Button(
            enabled = email.isNotBlank() &&
                    password.isNotBlank(),
            onClick = {

                Firebase.auth.signInWithEmailAndPassword(
                    email.trim(),
                    password
                )
                    .addOnSuccessListener { result ->

                        val uid = result.user?.uid

                        Log.d(
                            "Firebase_Test",
                            "Authenticated UID: $uid"
                        )

                        status = "Authenticated! Testing Firestore..."

                        FirebaseRepoTest().testConnection()
                    }
                    .addOnFailureListener { e ->

                        status = "Authentication Failed."

                        Log.e(
                            "Firebase_Test",
                            "Login Failed.",
                            e
                        )
                    }
            }
        ) {
            Text("Authenticate and Test Firestore")
        }

        Text(status)
    }
}
