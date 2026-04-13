package com.simplemobiletools.notes.pro.adapters

import android.view.View
import android.view.ViewGroup
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.adapters.MyRecyclerViewAdapter
import com.simplemobiletools.commons.extensions.beVisibleIf
import com.simplemobiletools.commons.views.MyRecyclerView
import com.simplemobiletools.notes.pro.databinding.ItemMigrateNoteBinding
import com.simplemobiletools.notes.pro.models.Note
import com.simplemobiletools.notes.pro.models.Notebook

class MigrateNoteAdapter(
    activity: BaseSimpleActivity,
    private val notes: List<Note>,
    private val notebooks: List<Notebook>,
    recyclerView: MyRecyclerView,
    itemClick: (Any) -> Unit
) : MyRecyclerViewAdapter(activity, recyclerView, itemClick) {

    private val onItemClick = itemClick as (Note) -> Unit
    private val notebookMap = notebooks.associateBy { it.id }

    override fun getActionMenuId() = 0

    override fun actionItemPressed(id: Int) {}

    override fun getSelectableItemCount() = notes.size

    override fun getIsItemSelectable(position: Int) = false

    override fun getItemSelectionKey(position: Int) = notes[position].id?.toInt() ?: 0

    override fun getItemKeyPosition(key: Int) = notes.indexOfFirst { it.id?.toInt() == key }

    override fun onActionModeCreated() {}

    override fun onActionModeDestroyed() {}

    override fun prepareActionMode(menu: android.view.Menu) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return createViewHolder(ItemMigrateNoteBinding.inflate(layoutInflater, parent, false).root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = notes[position]
        holder.bindView(item, true, true) { itemView, _ ->
            setupView(itemView, item)
        }
        bindViewHolder(holder)
    }

    override fun getItemCount() = notes.size

    private fun setupView(view: View, note: Note) {
        ItemMigrateNoteBinding.bind(view).apply {
            migrateNoteTitle.text = note.title
            val notebook = notebookMap[note.notebookId]
            migrateNoteNotebook.text = notebook?.title ?: ""
            migrateNoteNotebook.beVisibleIf(notebook != null && notebook.id != 1L)
        }
    }
}
