# LLM Reference Document

> Records interactions such as prompts and responses from Large-Language Models (LLMs).

---

<<<<<<< HEAD
## Prompt #001 (Akshat)

|    Field     |  Details   |
|:-------------|:-----------|
|**Promptee**  | Akshat Singh Chana |
| **Agent**    | Claude |
| **Model**    | Claude Opus 5.5 |
| **Date**     | 10-26-2026 |

### Prompt

> Help me plan the Lottery model for US 02.04.03, 02.04.04, 02.04.05, 02.04.06 and 02.04.07: figure out the fields and types, structure the validation and status functions, and write unit tests for them.

### Answer

> Suggested the Lottery fields and types (Long epoch-millis dates, nullable waitlistCapacity, drawn flag), the function signatures for validation and status, and explained the logic of each. I wrote the validation functions myself with review. Claude provided the code for status(), isRegistrationOpen(), isWaitlistFull() and canEdit(), and the full LotteryTest.kt file.



---

## Prompt #002 (Akshat)

|    Field     |  Details   |
|:-------------|:-----------|
|**Promptee**  | Akshat Singh Chana |
| **Agent**    | Claude |
| **Model**    | Claude Opus 5.5 |
| **Date**     | 10-27-2026 to 11-04-2026 |

### Prompt

> Help me build a Firestore repository for lotteries (US 02.04.01, 02.04.06), restructure the lottery logic based on teammate feedback, and add a Time class using java.time.

### Answer

> Explained Firestore callbacks and provided getLottery and the class structure; reviewed and corrected my createLottery and updateLottery. Provided the split into one class per rule (LotteryRule, the four rule objects, LotteryValidator, and the checker objects), the updated LotteryTest.kt, the desugaring build change, Time.kt and TimeTest.kt, and the full KDoc comments (@param/@return) for every model and util file.
=======
<!-- Copy the section beginnning at "Prompt #001..." for each new interaction. Increment as we go on -->

## Prompt #003

|    Field     |  Details   |
|:-------------|:-----------|
|**Promptee**  | Eliza Lazar  |
| **Agent**    | Claude |
| **Model**    | Sonnet 5.5 |
| **Date**     | 2026-11-04   |

### Prompt

> "Please provide me with a ui.theme kotlin file for the colours thatmatches this UI (insert image of mockup)"

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
        val DarkGreen = Color(---)   // Screen background
        val TealCard = Color---    // Cards, dialogs, surfaces
        val Teal = Color(---)         // Profile cards
        val Gold = Color(---)        // Accents, buttons, selected tab
        val Cream = Color(---)       // Main text hierarchy
        val MutedText = Color(---)   // Secondary text, unselected icons
        val AvatarGreen = Color(---) // Initials circle in the header
        val Scrim = Color(---)       // Dark strip over campsite photos
        val PhotoTop = Color(---)    // Campsite photo placeholder gradient
        val PhotoMid = Color(---)
        val PhotoBottom = Color(---)
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
        error = Color(---)
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
