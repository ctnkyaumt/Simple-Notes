package com.simplemobiletools.notes.pro.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "notebooks")
data class Notebook(
    @PrimaryKey(autoGenerate = true) var id: Long?,
    @ColumnInfo(name = "title") var title: String,
    @ColumnInfo(name = "protection_type") var protectionType: Int,
    @ColumnInfo(name = "protection_hash") var protectionHash: String,
    @ColumnInfo(name = "pinned", defaultValue = "0") var pinned: Int = 0,
    @ColumnInfo(name = "sort_order", defaultValue = "0") var sortOrder: Int = 0
) {
    fun isLocked() = protectionHash.isNotEmpty()

    fun isPinned() = pinned != 0
}
