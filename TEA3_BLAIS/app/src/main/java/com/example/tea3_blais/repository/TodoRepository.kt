package com.example.tea3_blais.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.example.tea3_blais.api.ApiClient
import com.example.tea3_blais.database.TodoDatabase
import com.example.tea3_blais.database.TodoItemEntity
import com.example.tea3_blais.database.TodoListEntity
import com.example.tea3_blais.model.ItemToDo
import com.example.tea3_blais.model.ListeToDo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.UUID

class TodoRepository(private val context: Context) {
    private val database = TodoDatabase.getDatabase(context)
    private val todoListDao = database.todoListDao()
    private val todoItemDao = database.todoItemDao()

    fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    suspend fun getLists(hash: String, userLogin: String, baseUrl: String): List<ListeToDo> {
        return withContext(Dispatchers.IO) {
            if (isNetworkAvailable()) {
                try {
                    val response = ApiClient.getApi(baseUrl).getLists(hash)
                    if (response.isSuccessful) {
                        val lists = response.body()?.lists ?: emptyList()
                        val entities = lists.map { list ->
                            TodoListEntity(
                                id = list.id,
                                label = list.label,
                                userLogin = userLogin,
                                isDirty = false
                            )
                        }
                        todoListDao.deleteAllListsForUser(userLogin)
                        todoListDao.insertLists(entities)
                        entities.map { ListeToDo(it.label).apply { setId(it.id) } }
                    } else {
                        todoListDao.getListsForUser(userLogin).first().map { 
                            ListeToDo(it.label).apply { setId(it.id) }
                        }
                    }
                } catch (e: Exception) {
                    todoListDao.getListsForUser(userLogin).first().map { 
                        ListeToDo(it.label).apply { setId(it.id) }
                    }
                }
            } else {
                todoListDao.getListsForUser(userLogin).first().map { 
                    ListeToDo(it.label).apply { setId(it.id) }
                }
            }
        }
    }

    suspend fun getItems(hash: String, listId: String, baseUrl: String): List<ItemToDo> {
        return withContext(Dispatchers.IO) {

            val cachedItems = todoItemDao.getItemsForList(listId).first()
            Log.d("TodoRepository", "Items en cache pour la liste $listId : ${cachedItems.size}")

            if (isNetworkAvailable()) {
                try {
                    Log.d("TodoRepository", "Mode en ligne : récupération des items pour la liste $listId")
                    val response = ApiClient.getApi(baseUrl).getItems(hash, listId)
                    if (response.isSuccessful) {
                        val items = response.body()?.items ?: emptyList()
                        Log.d("TodoRepository", "Items reçus de l'API : ${items.size}")
                        
                        // On sauvegarde les items dans le cache
                        val entities = items.map { item ->
                            TodoItemEntity(
                                id = item.id,
                                listId = listId,
                                label = item.label,
                                checked = item.checked == "1",
                                isDirty = false
                            )
                        }


                        if (cachedItems.isEmpty()) {
                            todoItemDao.deleteAllItemsInList(listId)
                        }
                        todoItemDao.insertItems(entities)
                        
                        Log.d("TodoRepository", "Items sauvegardés en base : ${entities.size}")
                        entities.map { entity ->
                            ItemToDo(entity.label, entity.checked).apply { 
                                setId(entity.id)
                            }
                        }
                    } else {
                        Log.d("TodoRepository", "Erreur API, utilisation du cache")
                        if (cachedItems.isNotEmpty()) {
                            cachedItems.map { entity ->
                                ItemToDo(entity.label, entity.checked).apply { 
                                    setId(entity.id)
                                }
                            }
                        } else {
                            emptyList()
                        }
                    }
                } catch (e: Exception) {
                    Log.e("TodoRepository", "Erreur lors de la récupération des items", e)
                    if (cachedItems.isNotEmpty()) {
                        cachedItems.map { entity ->
                            ItemToDo(entity.label, entity.checked).apply { 
                                setId(entity.id)
                            }
                        }
                    } else {
                        emptyList()
                    }
                }
            } else {
                Log.d("TodoRepository", "Mode hors ligne : lecture depuis la base locale pour la liste $listId")
                if (cachedItems.isNotEmpty()) {
                    cachedItems.map { entity ->
                        ItemToDo(entity.label, entity.checked).apply { 
                            setId(entity.id)
                        }
                    }
                } else {
                    emptyList()
                }
            }
        }
    }

    suspend fun createList(label: String, userLogin: String, hash: String, baseUrl: String): String {
        return withContext(Dispatchers.IO) {
            if (isNetworkAvailable()) {
                try {
                    val response = ApiClient.getApi(baseUrl).createList(hash, label)
                    if (response.isSuccessful) {
                        val listId = response.body()?.id ?: throw Exception("List ID not returned")
                        val entity = TodoListEntity(
                            id = listId,
                            label = label,
                            userLogin = userLogin,
                            isDirty = false
                        )
                        todoListDao.insertList(entity)
                        listId
                    } else {
                        throw Exception("Failed to create list")
                    }
                } catch (e: Exception) {
                    // En mode hors ligne, on génère un ID temporaire
                    val tempId = "temp_${UUID.randomUUID()}"
                    val entity = TodoListEntity(
                        id = tempId,
                        label = label,
                        userLogin = userLogin,
                        isDirty = true
                    )
                    todoListDao.insertList(entity)
                    tempId
                }
            } else {
                val tempId = "temp_${UUID.randomUUID()}"
                val entity = TodoListEntity(
                    id = tempId,
                    label = label,
                    userLogin = userLogin,
                    isDirty = true
                )
                todoListDao.insertList(entity)
                tempId
            }
        }
    }

    suspend fun createItem(listId: String, label: String, hash: String, baseUrl: String): String {
        return withContext(Dispatchers.IO) {
            val listExists = todoListDao.getListsForUser(listId).first().isNotEmpty()
            Log.d("TodoRepository", "Liste $listId existe dans le cache : $listExists")

            if (isNetworkAvailable()) {
                try {
                    Log.d("TodoRepository", "Mode en ligne : création d'un item pour la liste $listId")
                    val response = ApiClient.getApi(baseUrl).addItem(hash, listId, label)
                    if (response.isSuccessful) {
                        val itemId = response.body()?.item?.id ?: throw Exception("Item ID not returned")
                        Log.d("TodoRepository", "Item créé avec l'ID : $itemId")
                        val entity = TodoItemEntity(
                            id = itemId,
                            listId = listId,
                            label = label,
                            checked = false,
                            isDirty = false
                        )
                        todoItemDao.insertItem(entity)
                        Log.d("TodoRepository", "Item sauvegardé en base avec succès")
                        itemId
                    } else {
                        throw Exception("Failed to create item")
                    }
                } catch (e: Exception) {
                    Log.e("TodoRepository", "Erreur lors de la création de l'item en ligne", e)
                    val tempId = "temp_${UUID.randomUUID()}"
                    Log.d("TodoRepository", "Création d'un item temporaire avec l'ID : $tempId")
                    val entity = TodoItemEntity(
                        id = tempId,
                        listId = listId,
                        label = label,
                        checked = false,
                        isDirty = true
                    )
                    todoItemDao.insertItem(entity)
                    Log.d("TodoRepository", "Item temporaire sauvegardé en base avec succès")
                    tempId
                }
            } else {
                val tempId = "temp_${UUID.randomUUID()}"
                Log.d("TodoRepository", "Mode hors ligne : création d'un item temporaire avec l'ID : $tempId")
                val entity = TodoItemEntity(
                    id = tempId,
                    listId = listId,
                    label = label,
                    checked = false,
                    isDirty = true
                )
                todoItemDao.insertItem(entity)
                Log.d("TodoRepository", "Item temporaire sauvegardé en base avec succès")
                tempId
            }
        }
    }

    suspend fun updateItemState(
        hash: String,
        listId: String,
        itemId: String,
        checked: Boolean,
        baseUrl: String
    ): Boolean {
        return withContext(Dispatchers.IO) {
            if (isNetworkAvailable()) {
                try {
                    val response = ApiClient.getApi(baseUrl).updateItem(
                        hash = hash,
                        listId = listId,
                        itemId = itemId.toInt(),
                        checked = if (checked) "1" else "0"
                    )
                    if (response.isSuccessful) {
                        val items = todoItemDao.getItemsForList(listId).first()
                        val item = items.find { it.id == itemId }
                        if (item != null) {
                            val updatedItem = item.copy(checked = checked, isDirty = false)
                            todoItemDao.updateItem(updatedItem)
                        }
                        true
                    } else {
                        markItemAsDirty(itemId, listId, checked)
                        false
                    }
                } catch (e: Exception) {
                    markItemAsDirty(itemId, listId, checked)
                    false
                }
            } else {
                markItemAsDirty(itemId, listId, checked)
                true
            }
        }
    }

    private suspend fun markItemAsDirty(itemId: String, listId: String, checked: Boolean) {
        val items = todoItemDao.getItemsForList(listId).first()
        val item = items.find { it.id == itemId }
        if (item != null) {
            val updatedItem = item.copy(checked = checked, isDirty = true)
            todoItemDao.updateItem(updatedItem)
        }
    }

    suspend fun syncDirtyItems(hash: String, baseUrl: String) {
        withContext(Dispatchers.IO) {
            if (!isNetworkAvailable()) return@withContext


            val dirtyItems = todoItemDao.getDirtyItems().first()
            for (item in dirtyItems) {
                try {
                    val response = ApiClient.getApi(baseUrl).updateItem(
                        hash = hash,
                        listId = item.listId,
                        itemId = item.id.toInt(),
                        checked = if (item.checked) "1" else "0"
                    )
                    if (response.isSuccessful) {
                        todoItemDao.clearDirtyFlag(item.id)
                    }
                } catch (e: Exception) {

                }
            }


            val dirtyLists = todoListDao.getDirtyLists().first()
            for (list in dirtyLists) {
                try {
                    val response = ApiClient.getApi(baseUrl).createList(hash, list.label)
                    if (response.isSuccessful) {
                        val newId = response.body()?.id ?: continue

                        val items = todoItemDao.getItemsForList(list.id).first()
                        for (item in items) {
                            val newItem = item.copy(listId = newId, isDirty = true)
                            todoItemDao.insertItem(newItem)
                            todoItemDao.deleteAllItemsInList(list.id)
                        }
                        todoListDao.clearDirtyFlag(list.id)
                    }
                } catch (e: Exception) {
                }
            }
        }
    }
} 