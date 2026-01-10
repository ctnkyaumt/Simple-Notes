package com.simplemobiletools.notes.pro.dialogs

import android.app.Activity
import android.content.DialogInterface.BUTTON_POSITIVE
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.appcompat.widget.AppCompatEditText
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.DARK_GREY
import com.simplemobiletools.notes.pro.R
import com.simplemobiletools.notes.pro.databinding.DialogNewChecklistItemBinding
import com.simplemobiletools.notes.pro.databinding.ItemAddChecklistBinding

class NewCounterItemDialog(val activity: Activity, callback: (titles: ArrayList<String>) -> Unit) {
    private val titles = mutableListOf<AppCompatEditText>()
    private val binding = DialogNewChecklistItemBinding.inflate(activity.layoutInflater)
    private val view = binding.root

    init {
        addNewEditText()
        val plusTextColor = if (activity.isWhiteTheme()) {
            DARK_GREY
        } else {
            activity.getProperPrimaryColor().getContrastColor()
        }

        binding.apply {
            addItem.applyColorFilter(plusTextColor)
            addItem.setOnClickListener {
                addNewEditText()
            }
            settingsAddChecklistTop.beGone()
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(com.simplemobiletools.commons.R.string.ok, null)
            .setNegativeButton(com.simplemobiletools.commons.R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(view, this, R.string.add_new_counter_items) { alertDialog ->
                    alertDialog.showKeyboard(titles.first())
                    alertDialog.getButton(BUTTON_POSITIVE).setOnClickListener {
                        when {
                            titles.all { it.text!!.isEmpty() } -> activity.toast(com.simplemobiletools.commons.R.string.empty_name)
                            else -> {
                                val titles = titles.map { it.text.toString() }.filter { it.isNotEmpty() }.toMutableList() as ArrayList<String>
                                callback(titles)
                                alertDialog.dismiss()
                            }
                        }
                    }
                }
            }
    }

    private fun addNewEditText() {
        ItemAddChecklistBinding.inflate(activity.layoutInflater).apply {
            titleEditText.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_DONE || actionId == KeyEvent.KEYCODE_ENTER) {
                    addNewEditText()
                    true
                } else {
                    false
                }
            }
            titles.add(titleEditText)
            binding.checklistHolder.addView(this.root)
            activity.updateTextColors(binding.checklistHolder)
            binding.dialogHolder.post {
                binding.dialogHolder.fullScroll(View.FOCUS_DOWN)
                activity.showKeyboard(titleEditText)
            }
        }
    }
}
