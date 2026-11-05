# LLM Reference Document

> Records interactions such as prompts and responses from Large-Language Models (LLMs).

---

<!-- Copy the section beginnning at "Prompt #001..." for each new interaction. Increment as we go on -->

## Prompt #001

|    Field     |  Details   |
|:-------------|:-----------|
|**Promptee**  | Eliza Lazar  |
| **Agent**    | Claude |
| **Model**    | Sonnet 5.5|
| **Date**     | 2026-11-04   |

### Prompt

> "Provide me with a ui.theme kotlin file for the colours that
matches this UI (insert image of mockup)"

### Answer

> Here is the Jetpack Compose ui.theme implementation tailored to your BirchBark admin screen color palette and Material Design 3 specifications:

Kotlin
package com.example.birchbark.ui.theme

import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


object BirchBarkColors {
    val DarkGreen = Color(0xFF1B3232)   // Screen background
    val TealCard = Color(0xFF244746)    // Cards, dialogs, surfaces
    val Teal = Color(0xFF168F83)        // Profile cards
    val Gold = Color(0xFFD6A266)        // Accents, buttons, selected tab
    val Cream = Color(0xFFF5EADD)       // Main text hierarchy
    val MutedText = Color(0xFFB8C3BE)   // Secondary text, unselected icons
    val AvatarGreen = Color(0xFF526B64) // Initials circle in the header
    val Scrim = Color(0x990F2221)       // Dark strip over campsite photos
    val PhotoTop = Color(0xFF547B75)    // Campsite photo placeholder gradient
    val PhotoMid = Color(0xFF294B47)
    val PhotoBottom = Color(0xFF172D2C)
}


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


@Composable
fun birchBarkButtonColors() = ButtonDefaults.buttonColors(
    containerColor = BirchBarkColors.Gold,
    contentColor = BirchBarkColors.DarkGreen,
    disabledContainerColor = BirchBarkColors.Gold.copy(alpha = 0.3f),
    disabledContentColor = BirchBarkColors.DarkGreen.copy(alpha = 0.6f)
)
