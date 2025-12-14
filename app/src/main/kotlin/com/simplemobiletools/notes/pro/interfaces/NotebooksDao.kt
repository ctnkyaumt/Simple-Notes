package com.simplemobiletools.notes.pro.interfaces

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.simplemobiletools.notes.pro.models.Notebook

@Dao
interface NotebooksDao {
    @Query("SELECT * FROM notebooks ORDER BY title COLLATE UNICODE ASC")
    fun getNotebooks(): List<Notebook>

    @Query("SELECT * FROM notebooks WHERE id = :id")
    fun getNotebookWithId(id: Long): Notebook?

    @Query("SELECT id FROM notebooks WHERE title = :title COLLATE NOCASE")
    fun getNotebookIdWithTitle(title: String): Long?

    @Query("SELECT id FROM notebooks WHERE title = :title")
    fun getNotebookIdWithTitleCaseSensitive(title: String): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdate(notebook: Notebook): Long

    @Delete
    fun deleteNotebook(notebook: Notebook)
}
