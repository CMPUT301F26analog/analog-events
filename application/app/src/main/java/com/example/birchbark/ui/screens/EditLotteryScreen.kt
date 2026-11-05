package com.example.birchbark.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.birchbark.model.EditPermission
import com.example.birchbark.model.Lottery
import com.example.birchbark.ui.theme.BirchBarkColors.Gold
import com.example.birchbark.ui.theme.birchBarkButtonColors
import com.example.birchbark.ui.theme.birchBarkFieldColors
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

// See [3] in LLM.md, used for help organizing and starting up the user story. Further explanations
// were resolved within the same session.

/**
 * US 02.04.06: the owner's "Update lottery" form. Registration period, booking period, number of winners and
 * waiting list limit. Everything is read-only once the draw has happened (EditPermission).
 *
 * It only shows things and reports a click: loading and saving are done by EditLotteryRoute.
 *
 * @param errors problems from saving (Akshat's rule messages, or a network problem), shown in red.
 * @param successMessage shown in gold after a save worked.
 */
@Composable
fun EditLotteryScreen(
    campsiteName: String,
    lottery: Lottery?,
    loading: Boolean,
    errors: List<String>,
    successMessage: String?,
    onSave: (Lottery) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // The typed text. Rebuilt from the lottery whenever a new one arrives (after loading, after saving)
    var form by remember(lottery) { mutableStateOf(lottery?.toForm() ?: LotteryForm()) }
    // Problems reading the text (a date that isn't a date, a number that isn't a number)
    var formErrors by remember { mutableStateOf<List<String>>(emptyList()) }
    val canEdit = lottery != null && EditPermission.canEdit(lottery)

    // DESIGN
    Column(modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        TextButton(onClick = onBack) { Text("Back", color = Gold) }
        // Build out contextual header presenting the current target campsite asset identifier name
        Text("Lottery for $campsiteName", fontSize = 22.sp, fontWeight = FontWeight.Bold)

        // Implement conditional router handling screen states layout structures cleanly:
        when {
            loading -> Text("Loading...", modifier = Modifier.padding(top = 16.dp))

            lottery == null -> Text("This campsite doesn't have a lottery yet.", modifier = Modifier.padding(top = 16.dp))

            else -> {
                if (!canEdit) {
                    Text(
                        "This lottery has already been drawn, so it can't be edited.",
                        color = Gold, modifier = Modifier.padding(top = 8.dp)
                    )
                }
                Text("Dates are typed like 2026-11-15", modifier = Modifier.padding(top = 8.dp))

                // Registration Block
                SectionTitle("Registration")
                LotteryField("Registration start", form.registrationStart, canEdit) { form = form.copy(registrationStart = it) }
                LotteryField("Registration end", form.registrationEnd, canEdit) { form = form.copy(registrationEnd = it) }

                // Booking Block
                SectionTitle("Booking")
                LotteryField("Booking start", form.bookingStart, canEdit) { form = form.copy(bookingStart = it) }
                LotteryField("Booking end", form.bookingEnd, canEdit) { form = form.copy(bookingEnd = it) }

                // Draw Constraints Block
                SectionTitle("Draw")
                LotteryField("Number of winners", form.numWinners, canEdit, numeric = true) { form = form.copy(numWinners = it) }
                LotteryField("Waiting list limit (blank = no limit)", form.waitlistCapacity, canEdit, numeric = true) {
                    form = form.copy(waitlistCapacity = it)
                }

                // Save Changes
                Button(
                    enabled = canEdit,
                    colors = birchBarkButtonColors(),
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    onClick = {
                        val result = form.applyTo(lottery)
                        formErrors = result.errors
                        if (result.lottery != null) onSave(result.lottery)
                    }
                ) { Text("Save changes") }
            }
        }

        (formErrors + errors).forEach {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 4.dp))
        }
        if (successMessage != null) {
            Text(successMessage, color = Gold, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

/**
 * A simple text heading used to split the form into clear visual sections.
 *
 * This keeps the user interface organized by grouping related inputs together
 * (like putting all "Registration" fields under one title, and "Booking" under another).
 * It automatically applies consistent text sizes, bold lettering, and spacing at the top.
 *
 * @param text The title message to display on screen (e.g., "Registration" or "Booking").
 */
@Composable
private fun SectionTitle(text: String) {
    Text(text, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Gold, modifier = Modifier.padding(top = 16.dp))
}

/**
 * A standardized text input box designed for capturing lottery options.
 *
 * Instead of writing a brand new text field for every single prompt, this reusable component
 * handles everything automatically. It knows how to lock itself down if editing is disabled,
 * maps theme colors consistently, and swaps out the on-screen keyboard automatically if the field
 * expects numbers instead of standard text.
 *
 * @param label The text displayed inside or above the box to tell the user what to type (e.g., "Registration start").
 * @param value The actual text currently typed inside the box, passed down from the form's state.
 * @param enabled Controls whether the user can type in this box. Set to false to lock the field when a lottery is already drawn.
 * @param numeric If true, changes the pop-up keyboard to a number pad. Defaults to false (standard text keyboard).
 * @param onChange A trigger function (callback) that runs every single time the user presses a key, sending the updated string back up to save into the form state.
 */
@Composable
private fun LotteryField(
    label: String,
    value: String,
    enabled: Boolean,
    numeric: Boolean = false,
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        enabled = enabled,
        singleLine = true,
        label = { Text(label) },
        colors = birchBarkFieldColors(),
        keyboardOptions = KeyboardOptions(keyboardType = if (numeric) KeyboardType.Number else KeyboardType.Text),
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
    )
}
