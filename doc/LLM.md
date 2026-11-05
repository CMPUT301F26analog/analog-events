# LLM Reference Document

> Records interactions such as prompts and responses from Large-Language Models (LLMs).

---

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

---

## Prompt #003

|    Field     |  Details   |
|:-------------|:-----------|
|**Promptee**  | Eliza Lazar |
| **Agent**    | Gemini |
| **Model**    | N/A |
| **Date**     | 11-04-2026 |

### Prompt

> Please write a preview function for the following composable [INSERT CODE]

### Answer

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
