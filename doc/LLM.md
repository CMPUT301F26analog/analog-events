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


---


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


## Prompt #008 (Eliza)

|    Field     |  Details   |
|:-------------|:-----------|
|**Promptee**  | Eliza Lazar  |
| **Agent**    | Claude |
| **Model**    | Sonnet 5.5 |
| **Date**     | 2026-11-05   |

### Prompt

> "Can you provide me with unit tests in android studio based on this function structure [INSERT CODE], in kotlin?"

### Answer
> See CampsiteFilterTest.kt file in Test folder for full answer.


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

## Prompt #009 (Eliza)

|    Field     |  Details   |
|:-------------|:-----------|
|**Promptee**  | Eliza Lazar  |
| **Agent**    | Claude |
| **Model**    | Sonnet 5.5 |
| **Date**     | 2026-11-04   |

### Prompt

> "I am working with a DatePicker in Android that returns dates as a Long timestamp in UTC milliseconds, but the rest of my app uses Java LocalDate. Can you give me two simple helper functions to convert back and forth between UTC milliseconds and a LocalDate?"

### Answer
> See FilterScreen.kt file in main folder for full answer.

---

## Prompt #010 (Eliza)

|    Field     |  Details   |
|:-------------|:-----------|
|**Promptee**  | Eliza Lazar  |
| **Agent**    | Claude |
| **Model**    | Sonnet 5.5 |
| **Date**     | 2026-11-04   |

### Prompt

> ""I'm building a screen in Jetpack Compose and I want to add a button that opens a calendar/date picker when the user clicks it. How do I handle showing and hiding the pop-up dialog in Compose?"

### Answer
> In Jetpack Compose, you handle showing and hiding a pop-up dialog by using a boolean state variable. Because Compose is declarative, you don't "open" or "close" a dialog with a command. Instead, you conditionally show it based on whether that state variable is true or false.
> Here is exactly how you handle this pattern:
> 1. Define the Visibility State
> At the top of your composable, create a state variable (often named something like showPicker). Initialize it to false so the calendar is hidden by default.
  
    kotlin
    var showPicker by remember { mutableStateOf(false) }
> 2. Trigger the State with a Button

> When the user clicks your button, you simply flip that state variable to true. This tells Compose to redraw the screen and include the dialog.

    kotlin
    OutlinedButton(
        onClick = { showPicker = true }, // Turns the dialog ON
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Choose dates")
    }

> 3. Conditionally Display the Dialog

> Right below your button, use a standard Kotlin if statement. If showPicker is true, the dialog is rendered. To hide it, you just set showPicker = false inside the dialog's dismiss or confirm actions.

    kotlin
    if (showPicker) {
        DatePickerDialog(
            onDismissRequest = { showPicker = false }, // Turns it OFF if user taps outside
            confirmButton = {
                TextButton(onClick = { showPicker = false }) { // Turns it OFF when they click OK
                    Text("OK")
                }
            }
        ) {
            // Your actual DatePicker calendar content goes here
        }
    }

## Prompt #011 (Eliza)

|    Field     |  Details   |
|:-------------|:-----------|
|**Promptee**  | Eliza Lazar  |
| **Agent**    | Claude |
| **Model**    | Sonnet 5.5 |
| **Date**     | 2026-11-04   |

### Prompt

> ""I'm making a form field in Jetpack Compose for entering the number of guests. I want it to look red and show a message saying 'Enter a whole number, 1 or more' if the user types something invalid like letters or a number less than 1. How do I build that kind of validation layout?""

### Answer
> See FilterScreen.kt file in main folder for full answer.

---

## Prompt #012

|    Field     |  Details   |
|:-------------|:-----------|
|**Promptee**  | Riley Whitford  |
| **Agent**    | ChatGPT  |
| **Model**    | GPT-6 |
| **Date**     | 10-26-2026   |

### Prompt

> Hey, I am currently developing an android app in Android Studio using Kotlin. I am creating a small authenticator app, where I followed the majority of the code from the Firebase Installations Page. I don't know how one would test a function like this using Junit in android studio since I am working with a live database. What would be an example of this be like? Please provide something well explained (and with resources), so that for future tests, I can semi-follow this approach. Here is the current code: **INSTERTED CODE**

### Answer

You can test your Authenticator class using JUnit 4, just like your other Android tests. However, since your class communicates with Firebase Installations and Cloud Firestore, I recommend an instrumented JUnit test rather than a regular local unit test.

This allows you to run the test on your Android emulator and communicate with your actual Firebase project. 
Android Developers


For your Birchbark proof-of-concept, I would start with two tests:
1. FID consistency: Verify that Firebase returns the same installation ID on repeated requests.
2. Account lookup: Verify that checkForAccount() invokes the correct callback depending on whether that FID exists in Firestore.
You can do both without modifying your existing Authenticator class.
1. Create your test class
In Android Studio, create:

app/src/androidTest/java/com/example/birchbark/AuthenticatorTest.kt

Make sure the file is in androidTest, not test.

package com.example.birchbark

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.installations.FirebaseInstallations
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class AuthenticatorTest {

    // Test 1: Ensure the FID remains the same
    @Test
    fun fidIsConsistent() {
        val installations = FirebaseInstallations.getInstance()

        val fid1 = Tasks.await(
            installations.id, 15, TimeUnit.SECONDS
        )

        val fid2 = Tasks.await(
            installations.id, 15, TimeUnit.SECONDS
        )

        Log.d("AuthenticatorTest", "FID: $fid1")

        assertTrue(fid1.isNotBlank())
        assertEquals(fid1, fid2)
    }

    // Test 2: Check account existence in Firestore
    @Test
    fun checkForAccountWorks() {
        val fid = Tasks.await(
            FirebaseInstallations.getInstance().id,
            15, TimeUnit.SECONDS
        )

        // Independently check the expected result
        val document = Tasks.await(
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(fid)
                .get(),
            15, TimeUnit.SECONDS
        )

        val expectedExists = document.exists()
        val latch = CountDownLatch(1)

        var returnedFid: String? = null
        var accountFound: Boolean? = null
        var failure: Exception? = null

        val authenticator = Authenticator()

        authenticator.checkForAccount(
            object : Authenticator.Callback {

                override fun onAccountFound(fid: String) {
                    returnedFid = fid
                    accountFound = true
                    Log.d("AuthenticatorTest", "Account found")
                    latch.countDown()
                }

                override fun onAccountGone(fid: String) {
                    returnedFid = fid
                    accountFound = false
                    Log.d("AuthenticatorTest", "Account missing")
                    latch.countDown()
                }

                override fun onError(error: Exception) {
                    failure = error
                    latch.countDown()
                }
            }
        )

        // Wait for the asynchronous Firebase callback
        assertTrue(
            "Firebase callback timed out",
            latch.await(20, TimeUnit.SECONDS)
        )

        failure?.let {
            throw AssertionError("Firebase lookup failed", it)
        }

        assertEquals(fid, returnedFid)
        assertEquals(expectedExists, accountFound)
    }
}



Why use CountDownLatch?
Your checkForAccount() method is asynchronous. JUnit cannot simply call it and immediately check the result because Firebase might still be processing the request.

CountDownLatch(1) lets the test wait until one of your callbacks executes.
latch.countDown()


signals that the Firebase operation has completed, while:
latch.await(20, TimeUnit.SECONDS)


waits for the result, with a timeout to prevent the test from hanging indefinitely.
2. Verify your Gradle dependencies
In your application module's build.gradle.kts, make sure you have the Android test dependencies:

android {
    defaultConfig {
        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test:runner:1.7.0")
}



These are the published AndroidX test libraries. If your project already declares them through libs.versions.toml, you don't need to add duplicate entries. 
Android Developers

3. Run the tests
In Android Studio:
1. Start your Android emulator.
2. Right-click AuthenticatorTest.kt.
3. Select Run 'AuthenticatorTest'.
4. Open the test results and Logcat.
For the first test, you should see something similar to:
fidIsConsistent — Passed

D/AuthenticatorTest: FID: cXyz123ABC...

The second test will check the document with that FID in Firestore and verify that your callback agrees with the database result.
4. Test both account outcomes manually
To exercise both callback paths, you can use the Firestore Console.
1

Test a missing account

Run the tests on a fresh emulator installation with no matching document in the users collection.

Expected Logcat output:
D/AuthenticatorTest: Account missing


The checkForAccountWorks test should pass.



2

Test an existing account

Open the Firebase Console and navigate to Firestore Database.

Create a document in the users collection with the exact FID printed in Logcat as its document ID. Add a sample field, such as username: "testUser".

Rerun the test.

Expected output:
D/AuthenticatorTest: Account found


Both tests should pass.



This approach doesn't delete or overwrite any existing user profiles.
5. What the tests actually verify
Test	Verification
fidIsConsistent()	FID is nonempty and identical between requests
checkForAccountWorks()	The callback corresponds to Firestore document existence
Manual missing-account test	onAccountGone() executes
Manual existing-account test	onAccountFound() executes
Firestore error	Test fails instead of incorrectly reporting a missing account

One minor correction to the documentation in your Authenticator class: the FID is generated and managed by Firebase Installations, not by Cloud Firestore. Firestore simply stores your user profile under that identifier. 
Firebase


I would start with this instrumented test before introducing mocks or dependency injection. For your current proof-of-concept, it verifies the actual Firebase communication and your class's callback behavior while preserving the class structure you've already implemented.


---
