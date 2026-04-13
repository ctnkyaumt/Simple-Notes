package com.simplemobiletools.notes.pro.interfaces

interface ChecklistItemsListener {
    fun refreshItems()

    fun saveChecklist(callback: () -> Unit = {})

    fun migrateChecklistItems(itemIds: List<Int>)
}
