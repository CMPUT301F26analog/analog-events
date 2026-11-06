package com.example.birchbark.ui.entrant

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

@Composable
fun DeleteProfileSection(
    working: Boolean,
    onConfirmDelete: () -> Unit
) {
    var showConfirmation by remember {
        mutableStateOf(false)
    }

    OutlinedButton(
        onClick = { showConfirmation = true },
        enabled = !working,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.error
        ),
        modifier = Modifier.testTag("deleteProfileButton")
    ) {
        Text(
            if (working) "Deleting profile..."
            else "Delete Profile"
        )
    }

    if (showConfirmation) {
        AlertDialog(
            onDismissRequest = {
                if (!working) {
                    showConfirmation = false
                }
            },
            title = {
                Text("Delete your profile?")
            },
            text = {
                Text(
                    "This will permanently remove your profile " +
                            "and withdraw you from all accessible lottery " +
                            "waitlists. This action cannot be undone."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmation = false
                        onConfirmDelete()
                    },
                    enabled = !working,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.testTag(
                        "confirmDeleteProfileButton"
                    )
                ) {
                    Text("Delete Permanently")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showConfirmation = false },
                    enabled = !working,
                    modifier = Modifier.testTag(
                        "cancelDeleteProfileButton"
                    )
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
