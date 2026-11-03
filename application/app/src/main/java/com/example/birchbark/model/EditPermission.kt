package com.example.birchbark.model

// LLM assistance: see doc/LLM.md, Prompt #002 (Akshat)

/** US 02.04.06: settings can't change after the draw. */
object EditPermission {
    fun canEdit(lottery: Lottery): Boolean {
        return !lottery.drawn
    }
}