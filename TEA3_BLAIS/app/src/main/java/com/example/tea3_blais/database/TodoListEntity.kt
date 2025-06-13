package com.example.tea3_blais.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todo_lists")
data class TodoListEntity(
    @PrimaryKey
    val id: String,
    val label: String,
    val userLogin: String,
    var isDirty: Boolean = false
) 