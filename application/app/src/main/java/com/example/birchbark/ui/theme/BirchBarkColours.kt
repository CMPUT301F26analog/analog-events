package com.example.birchbark.ui.theme

import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// This file was initially created using [1] in LLM.md, then updated to match the screens.

/**
 * Centrally manages the complete color palette for the BirchBark application.
 *
 * This object functions as the single source of truth for raw hex color definitions
 * mapping to the application's user interface specifications, including backgrounds,
 * surface elements, text hierarchies, and placeholder asset gradients.
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
 * Adapts the custom palette into a Material Design 3 Dark Color Scheme.
 *
 * This mapping binds the specific brand tokens from [BirchBarkColors] to standard
 * Material component slots, ensuring semantic consistency across system elements
 * such as primary actions, body text, and structural container boundaries.
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

/**
 * Builds standard configuration profiles for outlined input components.
 *
 * This function provides uniform styling across interactive forms and dialog inputs,
 * maintaining high visibility for label states, borders, and input text fields
 * using variations of the primary cream color token.
 *
 * @return A [TextFieldColors] bundle configured for standard application form fields.
 */
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

/**
 * Generates custom button configurations conforming to brand specifications.
 *
 * This helper aligns primary action buttons with the established visual design system,
 * utilizing high-contrast gold containers for enabled interactions and dimming color
 * properties for unselectable UI components.
 *
 * @return A [ButtonColors] bundle configured for interactive control mechanisms.
 */
@Composable
fun birchBarkButtonColors() = ButtonDefaults.buttonColors(
    containerColor = BirchBarkColors.Gold,
    contentColor = BirchBarkColors.DarkGreen,
    disabledContainerColor = BirchBarkColors.Gold.copy(alpha = 0.3f),
    disabledContentColor = BirchBarkColors.DarkGreen.copy(alpha = 0.6f)
)