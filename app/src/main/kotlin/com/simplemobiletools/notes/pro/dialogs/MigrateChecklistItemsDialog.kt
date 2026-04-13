package com.simplemobiletools.notes.pro.dialogs

import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.extensions.getAlertDialogBuilder
import com.simplemobiletools.commons.extensions.setupDialogStuff
import com.simplemobiletools.notes.pro.R
import com.simplemobiletools.notes.pro.adapters.MigrateNoteAdapter
import com.simplemobiletools.notes.pro.databinding.DialogMigrateNoteBinding
import com.simplemobiletools.notes.pro.helpers.NotesHelper
import com.simplemobiletools.notes.pro.helpers.NotebooksHelper
import com.simplemobiletools.notes.pro.models.Note
import com.simplemobiletools.notes.pro.models.Notebook
import com.simplemobiletools.notes.pro.models.NoteType

class MigrateChecklistItemsDialog(
    val activity: BaseSimpleActivity,
    private val currentNoteId: Long,
    val callback: (targetNoteId: Long) -> Unit
) {
    private var dialog: AlertDialog? = null
    private var allNotes = mutableListOf<Note>()
    private var allNotebooks = mutableListOf<Notebook>()

    init {
        NotebooksHelper(activity).getNotebooks { notebooks ->
            allNotebooks = notebooks.toMutableList()
            NotesHelper(activity).getNotes { notes ->
                allNotes = notes.filter { it.id != currentNoteId && it.type == NoteType.TYPE_CHECKLIST }.toMutableList()
                initDialog()
            }
        }
    }

    private fun initDialog() {
        val binding = DialogMigrateNoteBinding.inflate(activity.layoutInflater)

        if (allNotes.isEmpty()) {
            binding.migrateNoteList.beGone()
            activity.toast(R.string.only_checklist_notes)
        }

        binding.migrateNoteList.layoutManager = LinearLayoutManager(activity)
        binding.migrateNoteList.adapter = MigrateNoteAdapter(
            activity = activity,
            notes = allNotes,
            notebooks = allNotebooks,
            recyclerView = binding.migrateNoteList,
            itemClick = { note ->
                callback((note as Note).id!!)
                dialog?.dismiss()
            }
        )

        activity.getAlertDialogBuilder()
            .setNegativeButton(com.simplemobiletools.commons.R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(binding.root, this, R.string.select_target_note) { alertDialog ->
                    dialog = alertDialog
                }
            }
    }
}
