package com.example.tea3_blais.api

import com.google.gson.annotations.SerializedName

data class AuthResponse(
    @SerializedName("hash")
    val hash: String
)

data class TodoList(
    @SerializedName("id")
    val id: String,
    @SerializedName("label")
    val label: String
)

data class TodoItem(
    @SerializedName("id")
    val id: String,
    @SerializedName("label")
    val label: String,
    @SerializedName("url")
    val url: String?,
    @SerializedName("checked")
    val checked: String
)

data class ListsResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("status")
    val status: Int,
    @SerializedName("lists")
    val lists: List<TodoList>
)

data class ItemsResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("status")
    val status: Int,
    @SerializedName("items")
    val items: List<TodoItem>
)

data class ItemResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("status")
    val status: Int,
    @SerializedName("item")
    val item: TodoItem
) 