package com.simplemobiletools.notes.pro.helpers

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.simplemobiletools.commons.helpers.ensureBackgroundThread
import com.simplemobiletools.notes.pro.extensions.notebooksDB
import com.simplemobiletools.notes.pro.models.Notebook

class NotebooksHelper(private val context: Context) {
    fun getNotebooks(callback: (notebooks: List<Notebook>) -> Unit) {
        ensureBackgroundThread {
            val notebooks = context.notebooksDB.getNotebooks()
            Handler(Looper.getMainLooper()).post {
                callback(notebooks)
            }
        }
    }

    fun getNotebookWithId(id: Long, callback: (notebook: Notebook?) -> Unit) {
        ensureBackgroundThread {
            val notebook = context.notebooksDB.getNotebookWithId(id)
            Handler(Looper.getMainLooper()).post {
                callback(notebook)
            }
        }
    }

    fun insertOrUpdateNotebook(notebook: Notebook, callback: ((newNotebookId: Long) -> Unit)? = null) {
        ensureBackgroundThread {
            if (notebook.id == null) {
                val maxSortOrder = context.notebooksDB.getMaxSortOrder(notebook.pinned) ?: 0
                notebook.sortOrder = maxSortOrder + 1
            }
            val notebookId = context.notebooksDB.insertOrUpdate(notebook)
            Handler(Looper.getMainLooper()).post {
                callback?.invoke(notebookId)
            }
        }
    }

    fun deleteNotebook(notebook: Notebook, callback: (() -> Unit)? = null) {
        ensureBackgroundThread {
            context.notebooksDB.deleteNotebook(notebook)
            Handler(Looper.getMainLooper()).post {
                callback?.invoke()
            }
        }
    }
}
