package com.simplemobiletools.notes.pro.dialogs

import android.app.Activity
import android.content.DialogInterface.BUTTON_POSITIVE
import com.simplemobiletools.commons.extensions.getAlertDialogBuilder
import com.simplemobiletools.commons.extensions.setupDialogStuff
import com.simplemobiletools.commons.extensions.showKeyboard
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.notes.pro.R
import com.simplemobiletools.notes.pro.databinding.DialogSetNotebookPasswordBinding
import com.simplemobiletools.notes.pro.helpers.NotebookPasswordHasher

class SetNotebookPasswordDialog(val activity: Activity, callback: (hash: String) -> Unit) {
    init {
        val binding = DialogSetNotebookPasswordBinding.inflate(activity.layoutInflater)
        val view = binding.root

        activity.getAlertDialogBuilder()
            .setPositiveButton(com.simplemobiletools.commons.R.string.ok, null)
            .setNegativeButton(com.simplemobiletools.commons.R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(view, this, R.string.lock_notebook) { alertDialog ->
                    alertDialog.showKeyboard(binding.password)
                    alertDialog.getButton(BUTTON_POSITIVE).setOnClickListener {
                        val password = binding.password.text?.toString().orEmpty()
                        val confirm = binding.confirmPassword.text?.toString().orEmpty()
                        when {
                            password.isBlank() -> activity.toast(R.string.empty_password)
                            password != confirm -> activity.toast(R.string.passwords_do_not_match)
                            else -> {
                                callback(NotebookPasswordHasher.createHash(password))
                                alertDialog.dismiss()
                            }
                        }
                    }
                }
            }
    }
}
