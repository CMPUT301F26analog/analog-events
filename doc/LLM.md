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
| **Date**     | 2026-11-05   |

### Prompt

> "Can you provide me with unit tests in android studio based on this function structure [INSERT CODE], in kotlin?"

### Answer
> See CampsiteSummaryTest.kt file in Test folder for full answer.
