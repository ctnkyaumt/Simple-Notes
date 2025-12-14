package com.simplemobiletools.notes.pro.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import com.simplemobiletools.commons.extensions.*
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
import com.simplemobiletools.notes.pro.helpers.NOTEBOOK_ID
import com.simplemobiletools.notes.pro.helpers.NotebooksHelper
import com.simplemobiletools.notes.pro.helpers.NotesHelper
import com.simplemobiletools.notes.pro.models.Notebook

class NotebooksActivity : SimpleActivity() {
    private val binding by viewBinding(ActivityNotebooksBinding::inflate)
    private var adapter: NotebooksAdapter? = null

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

        binding.notebooksList.layoutManager = GridLayoutManager(this, 2)

        adapter = NotebooksAdapter(emptyList(), itemClick = { openNotebook(it) }, itemLongClick = { showNotebookActions(it) })
        binding.notebooksList.adapter = adapter

        binding.newNotebookFab.setOnClickListener {
            NewNotebookDialog(this) { notebook ->
                NotebooksHelper(this).insertOrUpdateNotebook(notebook) { id ->
                    notebook.id = id
                    createInitialNoteIfNeeded(id, notebook.title)
                    openNotebook(notebook)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setupToolbar(binding.notebooksToolbar)
        refreshNotebooks()
        updateTextColors(binding.notebooksCoordinator)
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

    private fun showNotebookActions(notebook: Notebook) {
        val options = if (notebook.isLocked()) {
            arrayOf(getString(R.string.rename_notebook), getString(R.string.unlock_notebook))
        } else {
            arrayOf(getString(R.string.rename_notebook), getString(R.string.lock_notebook))
        }

        AlertDialog.Builder(this)
            .setItems(options) { _, which ->
                when (options[which]) {
                    getString(R.string.rename_notebook) -> {
                        RenameNotebookDialog(this, notebook) {
                            refreshNotebooks()
                        }
                    }
                    getString(R.string.lock_notebook) -> lockNotebook(notebook)
                    getString(R.string.unlock_notebook) -> unlockNotebook(notebook)
                }
            }
            .show()
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

    private fun createInitialNoteIfNeeded(notebookId: Long, notebookTitle: String) {
        NotesHelper(this).getNotesInNotebook(notebookId) { }
    }

    private fun launchAbout() {
        val licenses = LICENSE_RTL

        val faqItems = arrayListOf(
            com.simplemobiletools.commons.models.FAQItem(com.simplemobiletools.commons.R.string.faq_1_title_commons, com.simplemobiletools.commons.R.string.faq_1_text_commons),
            com.simplemobiletools.commons.models.FAQItem(R.string.faq_1_title, R.string.faq_1_text)
        )

        if (!resources.getBoolean(com.simplemobiletools.commons.R.bool.hide_google_relations)) {
            faqItems.add(com.simplemobiletools.commons.models.FAQItem(com.simplemobiletools.commons.R.string.faq_2_title_commons, com.simplemobiletools.commons.R.string.faq_2_text_commons))
            faqItems.add(com.simplemobiletools.commons.models.FAQItem(com.simplemobiletools.commons.R.string.faq_6_title_commons, com.simplemobiletools.commons.R.string.faq_6_text_commons))
            faqItems.add(com.simplemobiletools.commons.models.FAQItem(com.simplemobiletools.commons.R.string.faq_7_title_commons, com.simplemobiletools.commons.R.string.faq_7_text_commons))
            faqItems.add(com.simplemobiletools.commons.models.FAQItem(com.simplemobiletools.commons.R.string.faq_10_title_commons, com.simplemobiletools.commons.R.string.faq_10_text_commons))
        }

        startAboutActivity(R.string.app_name, licenses, BuildConfig.VERSION_NAME, faqItems, true)
    }
}
