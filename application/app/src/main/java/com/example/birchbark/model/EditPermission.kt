package com.example.birchbark.model

/** US 02.04.06: settings can't change after the draw. */
object EditPermission {
    fun canEdit(lottery: Lottery): Boolean {
        return !lottery.drawn
    }
}