package com.example.birchbark.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.birchbark.ui.theme.BirchBarkColors
import com.example.birchbark.ui.theme.ScreenTitle

/**
 * A blank entrant screen with only the standard UI chrome:
 * a gold Back button, system back handling, and an optional screen title.
 *
 * When rendered inside [com.example.birchbark.ui.entrant.EntrantApp], the bottom navigation bar remains
 * visible automatically.
 *
 * @param onBack called when the user taps Back or presses the system back button.
 * @param title optional heading shown below the Back button.
 */
@Composable
fun BlankEntrantScreen(
    onBack: () -> Unit,
    title: String? = null
) {
    BackHandler(onBack = onBack)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BirchBarkColors.DarkGreen)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(onClick = onBack) {
            Text("Back", color = BirchBarkColors.Gold)
        }

        if (!title.isNullOrBlank()) {
            ScreenTitle(title)
        }

        // Empty content area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )
    }
}
