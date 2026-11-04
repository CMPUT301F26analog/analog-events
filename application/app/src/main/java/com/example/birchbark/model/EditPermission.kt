package com.example.birchbark.model

// LLM assistance: see doc/LLM.md, Prompt #002 (Akshat)

/**
 * US 02.04.06: decides whether an owner may still change a lottery's settings.
 */
object EditPermission {
    /**
     * Checks whether a lottery can be edited.
     *
     * @param lottery the lottery to check.
     * @return true if winners have not been drawn yet, false once [Lottery.drawn] is true.
     */
    fun canEdit(lottery: Lottery): Boolean {
        return !lottery.drawn
    }
}
