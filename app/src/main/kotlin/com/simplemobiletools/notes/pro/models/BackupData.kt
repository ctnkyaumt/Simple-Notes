package com.simplemobiletools.notes.pro.models

import kotlinx.serialization.Serializable

@Serializable
data class BackupData(
    val notes: List<Note>,
    val notebooks: List<Notebook> = emptyList(),
    val settings: BackupSettings? = null
)

@Serializable
data class BackupSettings(
    val autosaveNotes: Boolean,
    val displaySuccess: Boolean,
    val clickableLinks: Boolean,
    val monospacedFont: Boolean,
    val showKeyboard: Boolean,
    val showNotePicker: Boolean,
    val showWordCount: Boolean,
    val gravity: Int,
    val placeCursorToEnd: Boolean,
    val enableLineWrap: Boolean,
    val useIncognitoMode: Boolean,
    val lastCreatedNoteType: Int,
    val moveDoneChecklistItems: Boolean,
    val fontSizePercentage: Int,
    val addNewChecklistItemsTop: Boolean,
    val notebookColumns: Int
)
