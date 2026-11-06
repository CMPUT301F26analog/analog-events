
# LLM Reference Document

> Records interactions such as prompts and responses from Large-Language Models (LLMs).

---

## Prompt #001 (Akshat)

| Field | Details |
|:------|:--------|
| **Promptee** | Akshat Singh Chana |
| **Agent** | Claude |
| **Model** | Claude Opus 5.5 |
| **Date** | 10-26-2026 |

### Prompt

> Help me plan the Lottery model for US 02.04.03, 02.04.04, 02.04.05, 02.04.06 and 02.04.07: figure out the fields and types, structure the validation and status functions, and write unit tests for them.

### Answer

> Suggested the Lottery fields and types (Long epoch-millis dates, nullable waitlistCapacity, drawn flag), the function signatures for validation and status, and explained the logic of each. I wrote the validation functions myself with review. Claude provided the code for status(), isRegistrationOpen(), isWaitlistFull() and canEdit(), and the full LotteryTest.kt file.

---

## Prompt #002 (Akshat)

| Field | Details |
|:------|:--------|
| **Promptee** | Akshat Singh Chana |
| **Agent** | Claude |
| **Model** | Claude Opus 5.5 |
| **Date** | 10-27-2026 to 11-04-2026 |

### Prompt

> Help me build a Firestore repository for lotteries (US 02.04.01, 02.04.06), restructure the lottery logic based on teammate feedback, and add a Time class using java.time.

### Answer

> Explained Firestore callbacks and provided getLottery and the class structure; reviewed and corrected my createLottery and updateLottery. Provided the split into one class per rule (LotteryRule, the four rule objects, LotteryValidator, and the checker objects), the updated LotteryTest.kt, the desugaring build change, Time.kt and TimeTest.kt, and the full KDoc comments (@param/@return) for every model and util file.

## Prompt #006 (Eliza)

|    Field     |  Details   |
|:-------------|:-----------|
|**Promptee**  | Eliza Lazar  |
| **Agent**    | Gemini |
| **Model**    | N/A |
| **Date**     | 2026-11-04   |

### Prompt

> "Can you give me a rough idea, through empty functions and comments, of what I have to do to complete the UI in Jetpack Compose kotlin for the lottery listing? Here's some guidelines of lottery [INSERT CODE]"

### Answer
> Here is a rough structure using empty functions and descriptive comments to map out how you can build the Jetpack Compose user interface for updating a lottery listing based on your data models and repository hooks.

    package com.example.birchbark.ui.screens
    
    import androidx.compose.runtime.Composable
    import androidx.compose.ui.Modifier
    import com.example.birchbark.model.Lottery
    import java.time.LocalDate
    
    /**
     * US 02.04.06 Update lottery listing
     * Skeleton layout mapped cleanly to form state and repository validations.
     */
    
    data class LotteryFormResult(val lottery: Lottery?, val errors: List<String>)
    
    data class LotteryForm(
        val registrationStart: String = "",
        val registrationEnd: String = "",
        val bookingStart: String = "",
        val bookingEnd: String = "",
        val numWinners: String = "",
        val waitlistCapacity: String = ""
    ) {
        /**
         * TODO: Implement form conversion and front-end validation bounds mapping.
         * 1. Extract and trim raw text strings for dates and integers.
         * 2. Leverage readDate helper for LocalDate parsing exceptions.
         * 3. Handle integer casting exceptions safely via toIntOrNull().
         * 4. Package return payload matching Akshat's LotteryValidator guidelines.
         */
        fun applyTo(lottery: Lottery): LotteryFormResult {
            // TODO: Assemble error collection mutable list
            // TODO: Invoke readDate parsing pipelines
            // TODO: Enforce strict whole-number validations for numbers and capacities
            // TODO: Build your copy payload mapping variables onto Time utils if valid
            return LotteryFormResult(null, emptyList())
        }
    
        // TODO: Implement parsing pipeline wrapping java.time.format.DateTimeParseException
        private fun readDate(text: String, label: String, errors: MutableList<String>): LocalDate? {
            return null
        }
    }
    
    /** TODO: Implement translation map mapping business domains into human readable text inputs */
    fun Lottery.toForm(): LotteryForm = LotteryForm()
    
    // ============================================================================
    // JETPACK COMPOSE UI LAYOUT & ARCHITECTURE STRUCTURE
    // ============================================================================
    
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
        import androidx.compose.runtime.LaunchedEffect
        import androidx.compose.runtime.getValue
        import androidx.compose.runtime.mutableStateOf
        import androidx.compose.runtime.remember
        import androidx.compose.runtime.setValue
        import androidx.compose.ui.Modifier
        import androidx.compose.ui.text.font.FontWeight
        import androidx.compose.ui.unit.dp
        import androidx.compose.ui.unit.sp
        import com.example.birchbark.model.Campsite
        import com.example.birchbark.model.EditPermission
        import com.example.birchbark.model.Lottery
        import com.example.birchbark.model.LotteryRepository
        import com.example.birchbark.ui.theme.BirchBarkColors.Gold
        import com.example.birchbark.ui.theme.birchBarkButtonColors
        import com.example.birchbark.ui.theme.birchBarkFieldColors
        import androidx.compose.foundation.layout.Spacer
        import androidx.compose.foundation.layout.height
        import androidx.compose.foundation.text.KeyboardOptions
        import androidx.compose.ui.text.input.KeyboardType
        import androidx.compose.ui.tooling.preview.Preview
        
        
        /**
         * US 02.04.06: the owner's "Update lottery" form. Registration period, booking period, number of winners and
         * waiting list limit. Everything is read-only once the draw has happened (EditPermission).
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
            // TODO: Track internal text form state, derived dynamically from incoming lottery data overrides
            // TODO: Maintain a distinct state list containing current UI side formatting error diagnostics
            // TODO: Resolve 'canEdit' configuration permissions from structural system models
        
            // DESIGN SUGGESTIONS:
            // - Use a main Column structured with vertical scrolling via `rememberScrollState` to handle screen sizes.
            // - Ensure all conditional branches (loading, null safety, editable forms) are fully isolated inside the tree.
            // - Merge layout validation arrays (`formErrors + errors`) to present unified feedback messages to the user.
            Column(modifier = modifier) {
                // TODO: Construct standard navigation anchor element triggering onBack callback pipelines
                // TODO: Build out contextual header presenting the current target campsite asset identifier name
                
                // TODO: Implement conditional router handling screen states layout structures cleanly:
                //       1. When state matches 'loading' -> Render loading placeholder component indicators
                //       2. When state matches 'lottery == null' -> Render missing configuration notice
                //       3. Else branch -> Present the structural form options blocks
                
                // TODO: Inside the active Form branch, render standard sectional areas with structured fields:
                //       - Registration Block (Registration Start Field, Registration End Field)
                //       - Booking Block (Booking Start Field, Booking End Field)
                //       - Draw Constraints Block (Winners Capacity, Waiting List Limitations configuration fields)
                
                // TODO: Wire Save Changes action trigger to check local form processing parameters before firing backend updates
                //       Example: Apply input data transformations (`form.applyTo()`), updates local form validation collections, 
                //       and execute onSave when execution configurations evaluate safely.
                
                // TODO: Append loop blocks iterating collections to display layout status messages contextually 
                //       (e.g. printing system errors or standard success confirmation indicators underneath fields)
            }
        }
        
        /**
         * Section Title UI Node Component
         *
         * DESIGN SUGGESTIONS:
         * - Utilize MaterialTheme styling configurations mapped directly to custom color design palettes.
         * - Establish unified padding spaces to prevent design crowding across varying form blocks.
         */
        @Composable
        private fun SectionTitle(text: String) {
            // TODO: Set up localized typography block detailing section context
        }
        
        /**
         * Generalized input capture component configuration.
         *
         * DESIGN SUGGESTIONS:
         * - Implement robust `OutlinedTextField` architectures pulling styling components from `birchBarkFieldColors()`.
         * - Safely inject explicit keyboard configurations mapped selectively by numeric parameters.
         * - Enforce explicit singleLine boundaries to cleanly manage text rendering space allocations.
         */
        @Composable
        private fun LotteryField(
            label: String,
            value: String,
            enabled: Boolean,
            numeric: Boolean = false,
            onChange: (String) -> Unit
        ) {
            // TODO: Instantiate generic custom text field component listening directly to field transformations
        }

## Prompt #007 (Eliza)

|    Field     |  Details   |
|:-------------|:-----------|
|**Promptee**  | Eliza Lazar  |
| **Agent**    | Claude |
| **Model**    | Sonnet 5.5 |
| **Date**     | 2026-11-05   |

### Prompt

> "Can you provide me with unit tests in android studio based on this function structure [INSERT CODE], in kotlin?"

### Answer
> See EditLotteryScreenTest.kt file in Test folder for full answer
---

## Prompt #003 (Eliza)

| Field | Details |
|:------|:--------|
| **Promptee** | Eliza Lazar |
| **Agent** | Gemini |
| **Model** | N/A |
| **Date** | 11-04-2026 |

### Prompt

> Please write a preview function for the following composable [INSERT CODE]

### Answer

```kotlin
@Preview(showBackground = true, name = "Campsite Summary Preview")
@Composable
fun CampsiteSummaryPreview() {
    // 1. Create a dummy or mock campsite object to feed into your UI
    // Note: Replace these parameters with whatever fields your actual
    // com.example.birchbark.model.Campsite class requires.
    val mockCampsite = Campsite(
        name = "Whispering Pines",
        location = "Banff National Park, AB",
        maxOccupancy = 6,

        // Add any other properties your Campsite model requires (e.g., rating = 4.8)
    )

    // 2. Wrap it inside your app's theme so colors like TealCard render properly
    // Replace 'MaterialTheme' with 'BirchBarkTheme' if you have a custom theme file!
    MaterialTheme {
        CampsiteSummary(
            campsite = mockCampsite,
            onClick = { /* Do nothing in preview */ }
        )
    }
}
```

---

## Prompt #004 (Eliza)

| Field | Details |
|:------|:--------|
| **Promptee** | Eliza Lazar |
| **Agent** | Claude |
| **Model** | Sonnet 5.5 |
| **Date** | 2026-11-04 |

### Prompt

> Please provide me with a ui.theme kotlin file for the colours thatmatches this UI (insert image of mockup)

### Answer

> Here is the Jetpack Compose ui.theme implementation tailored to your BirchBark admin screen color palette and Material Design 3 specifications:

```kotlin
package com.example.birchbark.ui.theme

import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object BirchBarkColors {
    val DarkGreen = Color(---)   // Screen background
    val TealCard = Color(---)    // Cards, dialogs, surfaces
    val Teal = Color(---)        // Profile cards
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
```

---

## Prompt #005 (Eliza)

| Field | Details |
|:------|:--------|
| **Promptee** | Eliza Lazar |
| **Agent** | Claude |
| **Model** | Sonnet 5.5 |
| **Date** | 2026-11-05 |

### Prompt

> "Can you provide me with unit tests in android studio based on this function structure [INSERT CODE], in kotlin?"

### Answer

> See CampsiteSummaryTest.kt file in Test folder for full answer.


