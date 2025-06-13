package com.example.tea3_blais.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private var api: TodoApi? = null

    fun getApi(baseUrl: String): TodoApi {
        if (api == null || baseUrl != currentBaseUrl) {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .build()

            api = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(TodoApi::class.java)

            currentBaseUrl = baseUrl
        }
        return api!!
    }

    private var currentBaseUrl: String = ""
} 