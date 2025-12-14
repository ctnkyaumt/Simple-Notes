package com.simplemobiletools.notes.pro.dialogs

import android.content.DialogInterface.BUTTON_POSITIVE
import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.commons.extensions.getAlertDialogBuilder
import com.simplemobiletools.commons.extensions.setupDialogStuff
import com.simplemobiletools.commons.extensions.showKeyboard
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.helpers.ensureBackgroundThread
import com.simplemobiletools.notes.pro.R
import com.simplemobiletools.notes.pro.activities.SimpleActivity
import com.simplemobiletools.notes.pro.databinding.DialogRenameNotebookBinding
import com.simplemobiletools.notes.pro.extensions.notebooksDB
import com.simplemobiletools.notes.pro.models.Notebook

class RenameNotebookDialog(val activity: SimpleActivity, val notebook: Notebook, val callback: (notebook: Notebook) -> Unit) {
    init {
        val binding = DialogRenameNotebookBinding.inflate(activity.layoutInflater)
        val view = binding.root
        binding.notebookTitle.setText(notebook.title)

        activity.getAlertDialogBuilder()
            .setPositiveButton(com.simplemobiletools.commons.R.string.ok, null)
            .setNegativeButton(com.simplemobiletools.commons.R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(view, this, R.string.rename_notebook) { alertDialog ->
                    alertDialog.showKeyboard(binding.notebookTitle)
                    alertDialog.getButton(BUTTON_POSITIVE).setOnClickListener {
                        val title = binding.notebookTitle.text?.toString()?.trim().orEmpty()
                        ensureBackgroundThread {
                            when {
                                title.isEmpty() -> activity.toast(R.string.no_title)
                                activity.notebooksDB.getNotebookIdWithTitleCaseSensitive(title) != null -> activity.toast(R.string.title_taken)
                                else -> {
                                    notebook.title = title
                                    activity.notebooksDB.insertOrUpdate(notebook)
                                    activity.runOnUiThread {
                                        alertDialog.dismiss()
                                        callback(notebook)
                                    }
                                }
                            }
                        }
                    }
                }
            }
    }
}
