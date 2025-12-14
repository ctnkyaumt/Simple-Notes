package com.simplemobiletools.notes.pro.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.simplemobiletools.commons.extensions.applyColorFilter
import com.simplemobiletools.commons.extensions.getColoredDrawableWithColor
import com.simplemobiletools.commons.extensions.getProperPrimaryColor
import com.simplemobiletools.notes.pro.R
import com.simplemobiletools.notes.pro.databinding.ItemNotebookBinding
import com.simplemobiletools.notes.pro.models.Notebook

class NotebooksAdapter(
    private var notebooks: List<Notebook>,
    private val itemClick: (Notebook) -> Unit,
    private val itemLongClick: (Notebook) -> Unit,
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
            root.setOnClickListener { itemClick(notebook) }
            root.setOnLongClickListener {
                itemLongClick(notebook)
                true
            }
        }
    }

    override fun getItemCount() = notebooks.size

    fun updateItems(newNotebooks: List<Notebook>) {
        notebooks = newNotebooks
        notifyDataSetChanged()
    }
}
