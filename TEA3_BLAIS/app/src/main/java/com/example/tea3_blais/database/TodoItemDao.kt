package com.example.tea3_blais.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoItemDao {
    @Query("SELECT * FROM todo_items WHERE listId = :listId ORDER BY id ASC")
    fun getItemsForList(listId: String): Flow<List<TodoItemEntity>>

    @Query("SELECT * FROM todo_items WHERE isDirty = 1")
    fun getDirtyItems(): Flow<List<TodoItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: TodoItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<TodoItemEntity>): List<Long>

    @Update
    suspend fun updateItem(item: TodoItemEntity): Int

    @Query("UPDATE todo_items SET isDirty = 0 WHERE id = :itemId")
    suspend fun clearDirtyFlag(itemId: String): Int

    @Query("DELETE FROM todo_items WHERE listId = :listId")
    suspend fun deleteAllItemsInList(listId: String): Int

    @Query("SELECT COUNT(*) FROM todo_items WHERE listId = :listId")
    suspend fun getItemCountForList(listId: String): Int
} 