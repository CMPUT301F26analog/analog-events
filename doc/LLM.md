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

## Prompt #003 (Eliza)

|    Field     |  Details   |
|:-------------|:-----------|
|**Promptee**  | Eliza Lazar  |
| **Agent**    | Claude |
| **Model**    | Sonnet 5.5 |
| **Date**     | 2026-11-04   |

### Prompt

> "Write Kotlin code to parse and validate a string occupancy (must be an integer >= 1) and a string rating (must be a double between 1.0 and 5.0, replacing commas with periods). Just give me the variable assignments and error flags."

### Answer
> See FilterScreen.kt file in main folder for full answer.

## Prompt #004 (Eliza)

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

## Prompt #005 (Eliza)

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

## Prompt #006 (Eliza)

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
> See CampsiteFilterTest.kt file in Test folder for full answer.
