package com.example.tea3_blais

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.example.tea3_blais.api.ApiClient
import com.example.tea3_blais.databinding.ActivityShowListBinding
import com.example.tea3_blais.model.ItemToDo
import com.example.tea3_blais.repository.TodoRepository
import com.example.tea3_blais.utils.PreferencesManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class ShowListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityShowListBinding
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var adapter: ArrayAdapter<ItemToDo>
    private lateinit var repository: TodoRepository
    private var listeId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferencesManager = PreferencesManager(this)
        repository = TodoRepository(this)
        
        listeId = intent.getStringExtra("EXTRA_LISTE_ID")
        Log.d("ShowListActivity", "ID de la liste reçu : $listeId")
        if (listeId == null) {
            Log.e("ShowListActivity", "Erreur : ID de liste invalide")
            showError("Erreur : ID de liste invalide")
            finish()
            return
        }

        adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_multiple_choice,
            mutableListOf()
        )
        binding.listView.adapter = adapter
        binding.listView.choiceMode = android.widget.ListView.CHOICE_MODE_MULTIPLE

        refreshItems()

        binding.listView.setOnItemClickListener { _, _, position, _ ->
            val item = adapter.getItem(position) ?: return@setOnItemClickListener
            val isChecked = binding.listView.isItemChecked(position)
            Log.d("ShowListActivity", "Clic sur l'item en position $position - ID: ${item.getId()}, Description: ${item.getDescription()}, État actuel: ${item.getFait()}, Nouvel état: $isChecked")
            updateItemState(item, isChecked, position)
        }

        binding.fab.setOnClickListener {
            showAddItemDialog()
        }
    }

    private fun refreshItems() {
        val hash = preferencesManager.getHash()
        val currentListId = listeId
        if (hash != null && currentListId != null) {
            lifecycleScope.launch {
                try {
                    val prefs = PreferenceManager.getDefaultSharedPreferences(this@ShowListActivity)
                    val baseUrl = prefs.getString("api_url", "http://tomnab.fr/todo-api/") ?: "http://tomnab.fr/todo-api/"
                    
                    Log.d("ShowListActivity", "Récupération des items pour la liste $currentListId")
                    val items = repository.getItems(hash, currentListId, baseUrl)
                    Log.d("ShowListActivity", "Nombre d'items reçus : ${items.size}")
                    
                    adapter.clear()
                    adapter.addAll(items)
                    
                    // Mettre à jour les cases cochées
                    items.forEachIndexed { index, item ->
                        binding.listView.setItemChecked(index, item.getFait())
                    }

                    if (!repository.isNetworkAvailable()) {
                        showError("Mode hors ligne : les données affichées proviennent du cache local")
                    }
                } catch (e: Exception) {
                    Log.e("ShowListActivity", "Erreur lors de la récupération des items", e)
                    showError("Erreur lors de la récupération des items")
                }
            }
        }
    }

    private fun updateItemState(item: ItemToDo, isChecked: Boolean, position: Int) {
        val hash = preferencesManager.getHash()
        val currentListId = listeId
        val itemId = item.getId()
        
        if (hash == null || currentListId == null || itemId == null) {
            Log.e("ShowListActivity", "Données manquantes - hash: $hash, listId: $currentListId, itemId: $itemId")
            showError("Erreur : données invalides")
            binding.listView.setItemChecked(position, !isChecked)
            return
        }

        lifecycleScope.launch {
            try {
                val prefs = PreferenceManager.getDefaultSharedPreferences(this@ShowListActivity)
                val baseUrl = prefs.getString("api_url", "http://tomnab.fr/todo-api/") ?: "http://tomnab.fr/todo-api/"
                
                val success = repository.updateItemState(hash, currentListId, itemId, isChecked, baseUrl)
                if (!success && repository.isNetworkAvailable()) {
                    showError("Erreur lors de la mise à jour de l'item")
                    binding.listView.setItemChecked(position, !isChecked)
                    item.setFait(!isChecked)
                } else {
                    item.setFait(isChecked)
                    if (!repository.isNetworkAvailable()) {
                        showError("Mode hors ligne : la modification sera synchronisée ultérieurement")
                    }
                }
            } catch (e: Exception) {
                Log.e("ShowListActivity", "Erreur lors de la mise à jour de l'item", e)
                showError("Erreur serveur")
                binding.listView.setItemChecked(position, !isChecked)
                item.setFait(!isChecked)
            }
        }
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun showAddItemDialog() {
        val editText = TextInputEditText(this).apply {
            hint = getString(R.string.new_item)
        }

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.new_item)
            .setView(editText)
            .setPositiveButton(R.string.ok) { _, _ ->
                val description = editText.text?.toString()
                if (!description.isNullOrBlank()) {
                    val hash = preferencesManager.getHash()
                    val currentListId = listeId
                    if (hash != null && currentListId != null) {
                        lifecycleScope.launch {
                            try {
                                val prefs = PreferenceManager.getDefaultSharedPreferences(this@ShowListActivity)
                                val baseUrl = prefs.getString("api_url", "http://tomnab.fr/todo-api/") ?: "http://tomnab.fr/todo-api/"
                                Log.d("ShowListActivity", "Tentative de création d'item dans la liste $currentListId")
                                
                                val itemId = repository.createItem(currentListId, description, hash, baseUrl)
                                Log.d("ShowListActivity", "Item créé avec l'ID : $itemId")

                                refreshItems()

                                if (!repository.isNetworkAvailable()) {
                                    showError("Item créé en mode hors ligne. Il sera synchronisé lorsque le réseau sera disponible.")
                                }
                            } catch (e: Exception) {
                                Log.e("ShowListActivity", "Erreur lors de la création de l'item", e)
                                showError("Erreur lors de la création de l'item")
                            }
                        }
                    }
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        val hash = preferencesManager.getHash()
        if (hash != null) {
            lifecycleScope.launch {
                val prefs = PreferenceManager.getDefaultSharedPreferences(this@ShowListActivity)
                val baseUrl = prefs.getString("api_url", "http://tomnab.fr/todo-api/") ?: "http://tomnab.fr/todo-api/"
                repository.syncDirtyItems(hash, baseUrl)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
} 