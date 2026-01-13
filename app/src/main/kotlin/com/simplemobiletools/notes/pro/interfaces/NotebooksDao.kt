package com.simplemobiletools.notes.pro.interfaces

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.simplemobiletools.notes.pro.models.Notebook

@Dao
interface NotebooksDao {
    @Query("SELECT * FROM notebooks ORDER BY pinned DESC, sort_order ASC, title COLLATE UNICODE ASC")
    fun getNotebooks(): List<Notebook>

    @Query("SELECT MAX(sort_order) FROM notebooks WHERE pinned = :pinned")
    fun getMaxSortOrder(pinned: Int): Int?

    @Query("SELECT * FROM notebooks WHERE id = :id")
    fun getNotebookWithId(id: Long): Notebook?

    @Query("SELECT id FROM notebooks WHERE title = :title COLLATE NOCASE")
    fun getNotebookIdWithTitle(title: String): Long?

    @Query("SELECT id FROM notebooks WHERE title = :title")
    fun getNotebookIdWithTitleCaseSensitive(title: String): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdate(notebook: Notebook): Long

    @Query("UPDATE notebooks SET pinned = :pinned WHERE id = :id")
    fun updatePinned(id: Long, pinned: Int)

    @Query("UPDATE notebooks SET sort_order = :sortOrder WHERE id = :id")
    fun updateSortOrder(id: Long, sortOrder: Int)

    @Delete
    fun deleteNotebook(notebook: Notebook)
}
