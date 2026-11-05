package com.example.birchbark.ui.theme

import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/* Claude, Sonnet 5.5, 2026-11-04, "Provide me with a ui.theme kotlin file for the colours that
matches this UI (insert image of mockup)
 */
// This file was initially created using [] in LLM.md, then updated to match the Admin screens.
/**
 * ONE place for every colour in the app. These values were updated from the admin screens.
 * Import what you need:
 *   import com.example.birchbark.ui.theme.BirchBarkColors.DarkGreen
 */
object BirchBarkColors {
    val DarkGreen = Color(0xFF1B3232)   // screen background
    val TealCard = Color(0xFF244746)    // cards, dialogs, surfaces
    val Teal = Color(0xFF168F83)        // profile cards
    val Gold = Color(0xFFD6A266)        // accents, buttons, selected tab
    val Cream = Color(0xFFF5EADD)       // main text (was FFFBF5 in two screens, F5EADD in one)
    val MutedText = Color(0xFFB8C3BE)   // secondary text, unselected icons
    val AvatarGreen = Color(0xFF526B64) // initials circle in the header
    val Scrim = Color(0x990F2221)       // dark strip over campsite photos
    val PhotoTop = Color(0xFF547B75)    // campsite photo placeholder gradient
    val PhotoMid = Color(0xFF294B47)
    val PhotoBottom = Color(0xFF172D2C)
}

/**
 * Use this in your existing ui/theme/Theme.kt so default Material components also match:
 *   MaterialTheme(colorScheme = BirchBarkColorScheme, typography = Typography, content = content)
 */
val BirchBarkColorScheme = darkColorScheme(
    primary = BirchBarkColors.Gold,
    onPrimary = BirchBarkColors.DarkGreen,
    secondary = BirchBarkColors.Teal,
    onSecondary = BirchBarkColors.Cream,
    background = BirchBarkColors.DarkGreen,
    onBackground = BirchBarkColors.Cream,
    surface = BirchBarkColors.TealCard,
    onSurface = BirchBarkColors.Cream,
    surfaceVariant = BirchBarkColors.TealCard,
    onSurfaceVariant = BirchBarkColors.MutedText,
    outline = BirchBarkColors.Cream.copy(alpha = 0.5f),
    error = Color(0xFFE57373)
)

/** Cream text fields for dark dialogs and forms (was copy-pasted in two screens). */
@Composable
fun birchBarkFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = BirchBarkColors.Cream,
    unfocusedTextColor = BirchBarkColors.Cream,
    focusedLabelColor = BirchBarkColors.Cream,
    unfocusedLabelColor = BirchBarkColors.Cream.copy(alpha = 0.7f),
    cursorColor = BirchBarkColors.Cream,
    focusedBorderColor = BirchBarkColors.Cream,
    unfocusedBorderColor = BirchBarkColors.Cream.copy(alpha = 0.5f)
)

/** Gold buttons with dark text (the same look as the admin screens). */
@Composable
fun birchBarkButtonColors() = ButtonDefaults.buttonColors(
    containerColor = BirchBarkColors.Gold,
    contentColor = BirchBarkColors.DarkGreen,
    disabledContainerColor = BirchBarkColors.Gold.copy(alpha = 0.3f),
    disabledContentColor = BirchBarkColors.DarkGreen.copy(alpha = 0.6f)
)
