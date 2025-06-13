package com.example.tea3_blais.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoListDao {
    @Query("SELECT * FROM todo_lists WHERE userLogin = :userLogin")
    fun getListsForUser(userLogin: String): Flow<List<TodoListEntity>>

    @Query("SELECT * FROM todo_lists WHERE isDirty = 1")
    fun getDirtyLists(): Flow<List<TodoListEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList(list: TodoListEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLists(lists: List<TodoListEntity>): List<Long>

    @Update
    suspend fun updateList(list: TodoListEntity): Int

    @Query("UPDATE todo_lists SET isDirty = 0 WHERE id = :listId")
    suspend fun clearDirtyFlag(listId: String): Int

    @Query("DELETE FROM todo_lists WHERE userLogin = :userLogin")
    suspend fun deleteAllListsForUser(userLogin: String): Int
} 