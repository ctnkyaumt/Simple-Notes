package com.simplemobiletools.notes.pro.dialogs

import android.app.Activity
import android.content.DialogInterface.BUTTON_POSITIVE
import com.simplemobiletools.commons.extensions.getAlertDialogBuilder
import com.simplemobiletools.commons.extensions.setupDialogStuff
import com.simplemobiletools.commons.extensions.showKeyboard
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.notes.pro.R
import com.simplemobiletools.notes.pro.databinding.DialogUnlockNotebookPasswordBinding
import com.simplemobiletools.notes.pro.helpers.NotebookPasswordHasher

class UnlockNotebookPasswordDialog(
    val activity: Activity,
    private val storedHash: String,
    callback: () -> Unit
) {
    init {
        val binding = DialogUnlockNotebookPasswordBinding.inflate(activity.layoutInflater)
        val view = binding.root

        activity.getAlertDialogBuilder()
            .setPositiveButton(com.simplemobiletools.commons.R.string.ok, null)
            .setNegativeButton(com.simplemobiletools.commons.R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(view, this, R.string.unlock_notebook) { alertDialog ->
                    alertDialog.showKeyboard(binding.password)
                    alertDialog.getButton(BUTTON_POSITIVE).setOnClickListener {
                        val password = binding.password.text?.toString().orEmpty()
                        if (NotebookPasswordHasher.verify(password, storedHash)) {
                            callback()
                            alertDialog.dismiss()
                        } else {
                            activity.toast(R.string.wrong_password)
                        }
                    }
                }
            }
    }
}
