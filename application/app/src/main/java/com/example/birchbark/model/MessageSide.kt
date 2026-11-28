package com.example.birchbark.model

/**
 * US 01.14.01 and US 02.12.01
 *
 * ENUM class to determine the owner and entrantId for messaging.
 *
 * @Note See #[RW-2-LLM] in LLM.md
 *
 * @property field Firestore participant field used to query that side's inbox.
 */
enum class MessageSide(val field: String) {
    OWNER("ownerId"), ENTRANT("entrantId")
}
