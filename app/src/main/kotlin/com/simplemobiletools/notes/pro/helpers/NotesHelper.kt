package com.simplemobiletools.notes.pro.helpers

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.helpers.ExportResult
import com.simplemobiletools.commons.helpers.PROTECTION_NONE
import com.simplemobiletools.commons.helpers.ensureBackgroundThread
import com.simplemobiletools.notes.pro.R
import com.simplemobiletools.notes.pro.extensions.config
import com.simplemobiletools.notes.pro.extensions.notesDB
import com.simplemobiletools.notes.pro.extensions.notebooksDB
import com.simplemobiletools.notes.pro.models.Note
import com.simplemobiletools.notes.pro.models.Notebook
import com.simplemobiletools.notes.pro.models.NoteType
import com.simplemobiletools.notes.pro.models.BackupData
import com.simplemobiletools.notes.pro.models.BackupSettings
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.OutputStream

class NotesHelper(val context: Context) {
    fun getNotes(callback: (notes: List<Note>) -> Unit) {
        ensureBackgroundThread {
            // make sure the initial note has enough time to be precreated
            if (context.config.appRunCount <= 1) {
                context.notesDB.getNotes()
                Thread.sleep(200)
            }

            val notes = context.notesDB.getNotes().toMutableList()
            val notesToDelete = mutableListOf<Note>()
            notes.forEach {
                if (it.path.isNotEmpty()) {
                    if (!it.path.startsWith("content://") && !File(it.path).exists()) {
                        context.notesDB.deleteNote(it)
                        notesToDelete.add(it)
                    }
                }
            }

            notes.removeAll(notesToDelete)

            if (notes.isEmpty()) {
                val generalNote = context.resources.getString(R.string.general_note)
                context.notesDB.insertNoteIfNotebookEmpty(
                    notebookId = 1L,
                    title = generalNote,
                    value = "",
                    type = NoteType.TYPE_TEXT.value,
                    path = "",
                    protectionType = PROTECTION_NONE,
                    protectionHash = ""
                )
                context.notesDB.deleteDuplicateEmptyNotesInNotebook(
                    notebookId = 1L,
                    title = generalNote,
                    type = NoteType.TYPE_TEXT.value
                )
                notes.addAll(context.notesDB.getNotes())
            }

            Handler(Looper.getMainLooper()).post {
                callback(notes)
            }
        }
    }

    fun getNotesInNotebook(notebookId: Long, callback: (notes: List<Note>) -> Unit) {
        ensureBackgroundThread {
            val notes = context.notesDB.getNotesInNotebook(notebookId).toMutableList()

            if (notes.isEmpty() && notebookId == 1L) {
                val generalNote = context.resources.getString(R.string.general_note)
                context.notesDB.insertNoteIfNotebookEmpty(
                    notebookId = notebookId,
                    title = generalNote,
                    value = "",
                    type = NoteType.TYPE_TEXT.value,
                    path = "",
                    protectionType = PROTECTION_NONE,
                    protectionHash = ""
                )
                context.notesDB.deleteDuplicateEmptyNotesInNotebook(
                    notebookId = notebookId,
                    title = generalNote,
                    type = NoteType.TYPE_TEXT.value
                )
                notes.addAll(context.notesDB.getNotesInNotebook(notebookId))
            }

            Handler(Looper.getMainLooper()).post {
                callback(notes)
            }
        }
    }

    fun getNoteWithId(id: Long, callback: (note: Note?) -> Unit) {
        ensureBackgroundThread {
            val note = context.notesDB.getNoteWithId(id)
            Handler(Looper.getMainLooper()).post {
                callback(note)
            }
        }
    }

    fun getNoteIdWithPath(path: String, callback: (id: Long?) -> Unit) {
        ensureBackgroundThread {
            val id = context.notesDB.getNoteIdWithPath(path)
            Handler(Looper.getMainLooper()).post {
                callback(id)
            }
        }
    }

    fun insertOrUpdateNote(note: Note, callback: ((newNoteId: Long) -> Unit)? = null) {
        ensureBackgroundThread {
            val noteId = context.notesDB.insertOrUpdate(note)
            Handler(Looper.getMainLooper()).post {
                callback?.invoke(noteId)
            }
        }
    }

    fun insertOrUpdateNotes(notes: List<Note>, callback: ((newNoteIds: List<Long>) -> Unit)? = null) {
        ensureBackgroundThread {
            val noteIds = context.notesDB.insertOrUpdate(notes)
            Handler(Looper.getMainLooper()).post {
                callback?.invoke(noteIds)
            }
        }
    }

    fun importNotes(activity: BaseSimpleActivity, notes: List<Note>, notebooks: List<Notebook> = emptyList(), callback: (ImportResult) -> Unit) {
        ensureBackgroundThread {
            // Update or create notebooks from backup to restore pinned status and sort order
            if (notebooks.isNotEmpty()) {
                val existingNotebooks = activity.notebooksDB.getNotebooks().associateBy { it.title }
                notebooks.forEach { importedNotebook ->
                    if (existingNotebooks.containsKey(importedNotebook.title)) {
                        val existing = existingNotebooks[importedNotebook.title]!!
                        // Update properties if they differ
                        if (existing.pinned != importedNotebook.pinned || existing.sortOrder != importedNotebook.sortOrder) {
                            existing.pinned = importedNotebook.pinned
                            existing.sortOrder = importedNotebook.sortOrder
                            activity.notebooksDB.insertOrUpdate(existing)
                        }
                    } else {
                        // Create new notebook with imported properties
                        val newNotebook = importedNotebook.copy(id = null)
                        activity.notebooksDB.insertOrUpdate(newNotebook)
                    }
                }
            }

            val currentNotebooks = activity.notebooksDB.getNotebooks().associateBy { it.title }
            val notebooksMap = currentNotebooks.toMutableMap()

            var imported = 0
            var skipped = 0

            notes.forEach { note ->
                // we need to reset the ID to avoid overwriting existing notes with the same ID
                note.id = null

                // Determine target notebook
                var targetNotebookId = 1L
                val notebookTitle = note.notebookTitle
                if (notebookTitle != null) {
                    if (notebooksMap.containsKey(notebookTitle)) {
                        targetNotebookId = notebooksMap[notebookTitle]!!.id!!
                    } else {
                        val newNotebook = Notebook(null, notebookTitle, PROTECTION_NONE, "")
                        targetNotebookId = activity.notebooksDB.insertOrUpdate(newNotebook)
                        notebooksMap[notebookTitle] = newNotebook.copy(id = targetNotebookId)
                    }
                }

                note.notebookId = targetNotebookId

                val existingNoteId = activity.notesDB.getNoteIdWithTitleInNotebook(note.title, targetNotebookId)
                if (existingNoteId != null) {
                    val existingNote = activity.notesDB.getNoteWithId(existingNoteId)
                    if (existingNote != null && existingNote.value == note.value) {
                        skipped++
                    } else {
                        // Duplicate title but different content -> Rename and import
                        var newTitle = note.title
                        var i = 1
                        while (activity.notesDB.getNoteIdWithTitleInNotebook(newTitle, targetNotebookId) != null) {
                            newTitle = "${note.title} ($i)"
                            i++
                        }
                        note.title = newTitle
                        activity.notesDB.insertOrUpdate(note)
                        imported++
                    }
                } else {
                    activity.notesDB.insertOrUpdate(note)
                    imported++
                }
            }

            val result = when {
                imported == 0 && skipped > 0 -> ImportResult.IMPORT_NOTHING_NEW
                imported > 0 -> ImportResult.IMPORT_OK
                else -> ImportResult.IMPORT_FAIL
            }
            callback(result)
        }
    }

    fun exportNotes(notesToBackup: List<Note>, outputStream: OutputStream): ExportResult {
        return try {
            val notebooks = context.notebooksDB.getNotebooks()
            val notebooksById = notebooks.associateBy { it.id }
            notesToBackup.forEach {
                it.notebookTitle = notebooksById[it.notebookId]?.title
            }

            val config = context.config
            val settings = BackupSettings(
                autosaveNotes = config.autosaveNotes,
                displaySuccess = config.displaySuccess,
                clickableLinks = config.clickableLinks,
                monospacedFont = config.monospacedFont,
                showKeyboard = config.showKeyboard,
                showNotePicker = config.showNotePicker,
                showWordCount = config.showWordCount,
                gravity = config.gravity,
                placeCursorToEnd = config.placeCursorToEnd,
                enableLineWrap = config.enableLineWrap,
                useIncognitoMode = config.useIncognitoMode,
                lastCreatedNoteType = config.lastCreatedNoteType,
                moveDoneChecklistItems = config.moveDoneChecklistItems,
                fontSizePercentage = config.fontSizePercentage,
                addNewChecklistItemsTop = config.addNewChecklistItemsTop,
                notebookColumns = config.notebookColumns
            )
            val backupData = BackupData(notesToBackup, notebooks, settings)

            val jsonString = Json.encodeToString(backupData)
            outputStream.use {
                it.write(jsonString.toByteArray())
            }
            ExportResult.EXPORT_OK
        } catch (e: Exception) {
            ExportResult.EXPORT_FAIL
        }
    }

    enum class ImportResult {
        IMPORT_FAIL, IMPORT_OK, IMPORT_PARTIAL, IMPORT_NOTHING_NEW
    }
}
