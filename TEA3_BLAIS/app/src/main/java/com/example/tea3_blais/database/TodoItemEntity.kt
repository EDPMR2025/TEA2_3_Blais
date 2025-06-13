package com.example.tea3_blais.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "todo_items",
    indices = [Index("listId")],
    foreignKeys = [
        ForeignKey(
            entity = TodoListEntity::class,
            parentColumns = ["id"],
            childColumns = ["listId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TodoItemEntity(
    @PrimaryKey
    val id: String,
    val listId: String,
    val label: String,
    var checked: Boolean,
    var isDirty: Boolean = false
) 