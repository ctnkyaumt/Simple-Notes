package com.simplemobiletools.notes.pro.adapters

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.simplemobiletools.commons.extensions.applyColorFilter
import com.simplemobiletools.commons.extensions.getColoredDrawableWithColor
import com.simplemobiletools.commons.extensions.getProperPrimaryColor
import com.simplemobiletools.notes.pro.R
import com.simplemobiletools.notes.pro.databinding.ItemNotebookBinding
import com.simplemobiletools.notes.pro.models.Notebook
import java.util.Collections

class NotebooksAdapter(
    private var notebooks: MutableList<Notebook>,
    private val itemClick: (Notebook) -> Unit,
    private val itemLongClick: (Notebook) -> Unit,
    private val dragStart: (RecyclerView.ViewHolder) -> Unit,
    private val itemsReordered: (List<Notebook>) -> Unit,
) : RecyclerView.Adapter<NotebooksAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemNotebookBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNotebookBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notebook = notebooks[position]
        holder.binding.apply {
            notebookTitle.text = notebook.title
            notebookLockIcon.visibility = if (notebook.isLocked()) android.view.View.VISIBLE else android.view.View.GONE
            if (notebook.isLocked()) {
                notebookLockIcon.setImageDrawable(
                    root.resources.getColoredDrawableWithColor(
                        com.simplemobiletools.commons.R.drawable.ic_lock_vector,
                        root.context.getProperPrimaryColor()
                    )
                )
                notebookLockIcon.applyColorFilter(root.context.getProperPrimaryColor())
            }

            notebookPinnedIcon.visibility = if (notebook.isPinned()) View.VISIBLE else View.GONE
            if (notebook.isPinned()) {
                notebookPinnedIcon.applyColorFilter(root.context.getProperPrimaryColor())
            }

            notebookDragHandle.applyColorFilter(root.context.getProperPrimaryColor())
            notebookDragHandle.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    dragStart(holder)
                }
                false
            }

            root.setOnClickListener { itemClick(notebook) }
            root.setOnLongClickListener {
                itemLongClick(notebook)
                true
            }
        }
    }

    override fun getItemCount() = notebooks.size

    fun updateItems(newNotebooks: List<Notebook>) {
        notebooks = newNotebooks.toMutableList()
        notifyDataSetChanged()
    }

    fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        val from = notebooks.getOrNull(fromPosition) ?: return false
        val to = notebooks.getOrNull(toPosition) ?: return false
        if (from.pinned != to.pinned) {
            return false
        }

        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(notebooks, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(notebooks, i, i - 1)
            }
        }
        notifyItemMoved(fromPosition, toPosition)
        return true
    }

    fun onDragFinished() {
        itemsReordered(notebooks)
    }
}
