package com.simplemobiletools.notes.pro.activities

import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.ensureBackgroundThread
import com.simplemobiletools.commons.helpers.LICENSE_RTL
import com.simplemobiletools.commons.helpers.PROTECTION_NONE
import com.simplemobiletools.notes.pro.BuildConfig
import com.simplemobiletools.notes.pro.R
import com.simplemobiletools.notes.pro.adapters.NotebooksAdapter
import com.simplemobiletools.notes.pro.databinding.ActivityNotebooksBinding
import com.simplemobiletools.notes.pro.dialogs.NewNotebookDialog
import com.simplemobiletools.notes.pro.dialogs.RenameNotebookDialog
import com.simplemobiletools.notes.pro.dialogs.SetNotebookPasswordDialog
import com.simplemobiletools.notes.pro.dialogs.UnlockNotebookPasswordDialog
import com.simplemobiletools.notes.pro.extensions.config
import com.simplemobiletools.notes.pro.extensions.notesDB
import com.simplemobiletools.notes.pro.extensions.notebooksDB
import com.simplemobiletools.notes.pro.extensions.updateWidgets
import com.simplemobiletools.notes.pro.extensions.widgetsDB
import com.simplemobiletools.notes.pro.helpers.OPEN_NEW_NOTE_DIALOG
import com.simplemobiletools.notes.pro.helpers.NOTEBOOK_ID
import com.simplemobiletools.notes.pro.helpers.NotebooksHelper
import com.simplemobiletools.notes.pro.helpers.NotesHelper
import com.simplemobiletools.notes.pro.models.Note
import com.simplemobiletools.notes.pro.models.Notebook
import com.simplemobiletools.notes.pro.models.NoteType

class NotebooksActivity : SimpleActivity() {
    private val binding by viewBinding(ActivityNotebooksBinding::inflate)
    private var adapter: NotebooksAdapter? = null
    private var itemTouchHelper: ItemTouchHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        isMaterialActivity = true
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        updateMaterialActivityViews(binding.notebooksCoordinator, null, useTransparentNavigation = false, useTopSearchMenu = false)

        binding.notebooksToolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                R.id.about -> {
                    launchAbout()
                    true
                }
                else -> false
            }
        }

        binding.notebooksList.layoutManager = GridLayoutManager(this, config.notebookColumns)

        adapter = NotebooksAdapter(
            mutableListOf(),
            itemClick = { openNotebook(it) },
            itemLongClick = { showNotebookActions(it) },
            dragStart = { viewHolder -> itemTouchHelper?.startDrag(viewHolder) },
            itemsReordered = { persistNotebookOrder(it) }
        )
        binding.notebooksList.adapter = adapter

        itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT,
            0
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPosition = viewHolder.bindingAdapterPosition
                val toPosition = target.bindingAdapterPosition
                return adapter?.onItemMove(fromPosition, toPosition) == true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            }

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)
                adapter?.onDragFinished()
            }
        })
        itemTouchHelper?.attachToRecyclerView(binding.notebooksList)

        binding.newNotebookFab.setOnClickListener {
            NewNotebookDialog(this) { notebook ->
                NotebooksHelper(this).insertOrUpdateNotebook(notebook) { id ->
                    notebook.id = id
                    createInitialNoteIfNeeded(id) {
                        openNotebookUnlocked(notebook, promptForFirstNote = true)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setupToolbar(binding.notebooksToolbar)

        (binding.notebooksList.layoutManager as? GridLayoutManager)?.spanCount = config.notebookColumns
        ensureDefaultNotebookExists {
            refreshNotebooks()
        }
        updateTextColors(binding.notebooksCoordinator)
    }

    private fun ensureDefaultNotebookExists(callback: () -> Unit) {
        val generalNoteTitle = getString(R.string.general_note)
        ensureBackgroundThread {
            val existingNotebook = notebooksDB.getNotebookWithId(1L)
            when {
                existingNotebook == null -> {
                    notebooksDB.insertOrUpdate(Notebook(id = 1L, title = generalNoteTitle, protectionType = PROTECTION_NONE, protectionHash = ""))
                }

                existingNotebook.title != generalNoteTitle -> {
                    existingNotebook.title = generalNoteTitle
                    notebooksDB.insertOrUpdate(existingNotebook)
                }
            }

            notesDB.insertNoteIfNotebookEmpty(
                notebookId = 1L,
                title = generalNoteTitle,
                value = "",
                type = NoteType.TYPE_TEXT.value,
                path = "",
                protectionType = PROTECTION_NONE,
                protectionHash = ""
            )
            notesDB.deleteDuplicateEmptyNotesInNotebook(
                notebookId = 1L,
                title = generalNoteTitle,
                type = NoteType.TYPE_TEXT.value
            )

            runOnUiThread(callback)
        }
    }

    private fun refreshNotebooks() {
        NotebooksHelper(this).getNotebooks { notebooks ->
            adapter?.updateItems(notebooks)
        }
    }

    private fun openNotebook(notebook: Notebook) {
        if (notebook.isLocked()) {
            UnlockNotebookPasswordDialog(this, notebook.protectionHash) {
                openNotebookUnlocked(notebook)
            }
        } else {
            openNotebookUnlocked(notebook)
        }
    }

    private fun openNotebookUnlocked(notebook: Notebook) {
        config.currentNotebookId = notebook.id ?: 1L
        Intent(this, MainActivity::class.java).apply {
            putExtra(NOTEBOOK_ID, config.currentNotebookId)
            startActivity(this)
        }
    }

    private fun openNotebookUnlocked(notebook: Notebook, promptForFirstNote: Boolean) {
        config.currentNotebookId = notebook.id ?: 1L
        Intent(this, MainActivity::class.java).apply {
            putExtra(NOTEBOOK_ID, config.currentNotebookId)
            if (promptForFirstNote) {
                putExtra(OPEN_NEW_NOTE_DIALOG, true)
            }
            startActivity(this)
        }
    }

    private fun showNotebookActions(notebook: Notebook) {
        val options = mutableListOf<String>().apply {
            add(getString(R.string.rename_notebook))
            add(if (notebook.isPinned()) getString(R.string.unpin_notebook) else getString(R.string.pin_notebook))
            add(if (notebook.isLocked()) getString(R.string.unlock_notebook) else getString(R.string.lock_notebook))
            if (notebook.id != 1L) {
                add(getString(R.string.delete_notebook))
            }
        }.toTypedArray()

        AlertDialog.Builder(this)
            .setItems(options) { _, which ->
                when (options[which]) {
                    getString(R.string.rename_notebook) -> {
                        RenameNotebookDialog(this, notebook) {
                            refreshNotebooks()
                        }
                    }
                    getString(R.string.pin_notebook),
                    getString(R.string.unpin_notebook) -> togglePinned(notebook)
                    getString(R.string.lock_notebook) -> lockNotebook(notebook)
                    getString(R.string.unlock_notebook) -> unlockNotebook(notebook)
                    getString(R.string.delete_notebook) -> deleteNotebook(notebook)
                }
            }
            .show()
    }

    private fun togglePinned(notebook: Notebook) {
        val notebookId = notebook.id ?: return
        ensureBackgroundThread {
            val newPinned = if (notebook.isPinned()) 0 else 1
            val maxSortOrder = notebooksDB.getMaxSortOrder(newPinned) ?: 0
            val newSortOrder = maxSortOrder + 1
            notebooksDB.updatePinned(notebookId, newPinned)
            notebooksDB.updateSortOrder(notebookId, newSortOrder)
            runOnUiThread {
                refreshNotebooks()
            }
        }
    }

    private fun persistNotebookOrder(notebooks: List<Notebook>) {
        ensureBackgroundThread {
            val pinnedNotebooks = notebooks.filter { it.isPinned() }
            val unpinnedNotebooks = notebooks.filterNot { it.isPinned() }

            pinnedNotebooks.forEachIndexed { index, item ->
                val id = item.id ?: return@forEachIndexed
                val newOrder = index + 1
                if (item.sortOrder != newOrder) {
                    item.sortOrder = newOrder
                    notebooksDB.updateSortOrder(id, newOrder)
                }
            }

            unpinnedNotebooks.forEachIndexed { index, item ->
                val id = item.id ?: return@forEachIndexed
                val newOrder = index + 1
                if (item.sortOrder != newOrder) {
                    item.sortOrder = newOrder
                    notebooksDB.updateSortOrder(id, newOrder)
                }
            }
        }
    }

    private fun deleteNotebook(notebook: Notebook) {
        if (notebook.id == 1L) {
            toast(R.string.cannot_delete_default_notebook)
            return
        }

        val notebookTitle = notebook.title
        val message = String.format(getString(R.string.delete_notebook_prompt_message), notebookTitle)
        com.simplemobiletools.commons.dialogs.ConfirmationDialog(
            this,
            message,
            0,
            com.simplemobiletools.commons.R.string.ok,
            com.simplemobiletools.commons.R.string.cancel
        ) {
            val notebookId = notebook.id ?: return@ConfirmationDialog
            ensureBackgroundThread {
                val notesToDelete = notesDB.getNotesInNotebook(notebookId)
                val noteIdsToDelete = notesToDelete.mapNotNull { it.id }.toSet()
                notesToDelete.forEach { note ->
                    note.id?.let { widgetsDB.deleteNoteWidgets(it) }
                    notesDB.deleteNote(note)
                }

                if (noteIdsToDelete.contains(config.widgetNoteId)) {
                    config.widgetNoteId = 1L
                    updateWidgets()
                }

                NotebooksHelper(this).deleteNotebook(notebook) {
                    if (config.currentNotebookId == notebookId) {
                        config.currentNotebookId = 1L
                    }
                    refreshNotebooks()
                }
            }
        }
    }

    private fun lockNotebook(notebook: Notebook) {
        com.simplemobiletools.commons.dialogs.ConfirmationDialog(
            this,
            "",
            R.string.locking_warning,
            com.simplemobiletools.commons.R.string.ok,
            com.simplemobiletools.commons.R.string.cancel
        ) {
            SetNotebookPasswordDialog(this) { hash ->
                notebook.protectionHash = hash
                notebook.protectionType = 1
                NotebooksHelper(this).insertOrUpdateNotebook(notebook) {
                    refreshNotebooks()
                }
            }
        }
    }

    private fun unlockNotebook(notebook: Notebook) {
        UnlockNotebookPasswordDialog(this, notebook.protectionHash) {
            removeProtection(notebook)
        }
    }

    private fun removeProtection(notebook: Notebook) {
        notebook.protectionHash = ""
        notebook.protectionType = PROTECTION_NONE
        NotebooksHelper(this).insertOrUpdateNotebook(notebook) {
            refreshNotebooks()
        }
    }

    private fun createInitialNoteIfNeeded(notebookId: Long, callback: () -> Unit) {
        val note = Note(
            id = null,
            notebookId = notebookId,
            title = getString(R.string.general_note),
            value = "",
            type = NoteType.TYPE_TEXT,
            path = "",
            protectionType = PROTECTION_NONE,
            protectionHash = ""
        )

        NotesHelper(this).insertOrUpdateNote(note) {
            callback()
        }
    }

    private fun launchAbout() {
        val message = "${getString(R.string.app_name)}\n${BuildConfig.VERSION_NAME}"
        AlertDialog.Builder(this)
            .setTitle(com.simplemobiletools.commons.R.string.about)
            .setMessage(message)
            .setPositiveButton(com.simplemobiletools.commons.R.string.ok, null)
            .show()
    }
}
