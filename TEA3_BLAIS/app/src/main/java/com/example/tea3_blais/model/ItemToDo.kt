package com.example.tea3_blais.model

import com.example.tea3_blais.api.TodoItem
import java.io.Serializable

data class ItemToDo(
    private var description: String,
    private var fait: Boolean = false,
    private var id: String? = null
) : Serializable {
    
    fun getDescription(): String = description
    
    fun setDescription(uneDescription: String) {
        description = uneDescription
    }
    
    fun getFait(): Boolean = fait
    
    fun setFait(fait: Boolean) {
        this.fait = fait
    }

    fun getId(): String? = id

    fun setId(id: String?) {
        this.id = id
    }

    companion object {
        fun fromTodoItem(item: TodoItem): ItemToDo {
            return ItemToDo(
                description = item.label,
                fait = item.checked == "1",
                id = item.id
            )
        }
    }
    
    override fun toString(): String {
        return description
    }
} 