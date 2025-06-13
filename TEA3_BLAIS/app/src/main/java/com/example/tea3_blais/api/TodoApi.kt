package com.example.tea3_blais.api

import retrofit2.Response
import retrofit2.http.*

interface TodoApi {
    @FormUrlEncoded
    @POST("authenticate")
    suspend fun authenticate(
        @Field("user") user: String,
        @Field("password") password: String
    ): Response<AuthResponse>

    @GET("lists")
    suspend fun getLists(
        @Header("hash") hash: String
    ): Response<ListsResponse>

    @GET("lists/{id}/items")
    suspend fun getItems(
        @Header("hash") hash: String,
        @Path("id") listId: String
    ): Response<ItemsResponse>

    @FormUrlEncoded
    @POST("lists/{id}/items")
    suspend fun addItem(
        @Header("hash") hash: String,
        @Path("id") listId: String,
        @Field("label") label: String,
        @Field("url") url: String? = null
    ): Response<ItemResponse>

    @PUT("lists/{listId}/items/{itemId}")
    suspend fun updateItem(
        @Header("hash") hash: String,
        @Path("listId") listId: String,
        @Path("itemId") itemId: Int,
        @Query("check") checked: String
    ): Response<TodoItem>

    @FormUrlEncoded
    @POST("lists")
    suspend fun createList(
        @Header("hash") hash: String,
        @Field("label") label: String
    ): Response<TodoList>
} 