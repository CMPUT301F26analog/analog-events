package com.example.birchbark.ui.notifications


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * A section for enabling/disabling notifications.
 * @param enabled and
 * @param onEnabledChange are used to control the state of the switch.
 */
@Composable
fun NotificationSettingsSection(
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    interactive: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Enable Notifications")

        Switch(
            checked = enabled,
            onCheckedChange = onEnabledChange,
            enabled = interactive
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NotificationSettingsPreview() {
    var enabled by remember { mutableStateOf(true) }

    NotificationSettingsSection(
        enabled = enabled,
        onEnabledChange = { enabled = it }
    )
}
