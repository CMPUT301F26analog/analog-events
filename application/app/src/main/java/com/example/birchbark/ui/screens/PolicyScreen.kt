package com.example.birchbark.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.birchbark.model.AppPolicy
import com.example.birchbark.ui.theme.birchBarkButtonColors

import androidx.compose.material3.Surface
import androidx.compose.ui.tooling.preview.Preview

/**
 * US 01.11.01: See the app policy and guidelines
 *
 * Every section in [AppPolicy.sections] is shown in order, and the whole page
 * scrolls. It's a plain Column (not a LazyColumn) because the policy is short,
 * and that keeps every section on the page for the tests to find.
 *
 * @param onBack called when the entrant taps Back, to return to the menu.
 * @param modifier modifier from the parent layout.
 */
@Composable
fun PolicyScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .statusBarsPadding() // Pushes content below the camera/status bar
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Back button
            Button(onClick = onBack, colors = birchBarkButtonColors()) {
                Text("Back")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Page heading
            Text(AppPolicy.TITLE, style = MaterialTheme.typography.headlineSmall)

            // One heading and paragraph per section
            for (section in AppPolicy.sections) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(section.title, style = MaterialTheme.typography.titleMedium)
                Text(section.body)
            }
        }
    }

/**
 * The menu entry that opens the policy page (US 01.11.01). Put this in the
 * entrant menu and have [onClick] show [PolicyScreen].
 *
 * @param onClick called when the entrant taps the menu entry.
 * @param modifier modifier from the parent layout.
 */
@Composable
fun PolicyMenuItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .testTag("policyMenuItem")
    ) {
        Text("App Policy and Guidelines")
    }
}

/*
@Preview(showBackground = true, name = "Policy Screen")
@Composable
fun PolicyScreenPreview() {
    // Wrap this in your app theme (e.g., BirchBarkTheme) if available
    // so birchBarkButtonColors() and typography render accurately.
    Surface {
        PolicyScreen(onBack = {})
    }
}
*/