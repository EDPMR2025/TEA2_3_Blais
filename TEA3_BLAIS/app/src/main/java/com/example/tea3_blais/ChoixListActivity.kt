package com.example.tea3_blais

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.example.tea3_blais.api.ApiClient
import com.example.tea3_blais.databinding.ActivityChoixListBinding
import com.example.tea3_blais.model.ListeToDo
import com.example.tea3_blais.model.ProfilListeToDo
import com.example.tea3_blais.repository.TodoRepository
import com.example.tea3_blais.utils.PreferencesManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class ChoixListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChoixListBinding
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var profil: ProfilListeToDo
    private lateinit var adapter: ArrayAdapter<ListeToDo>
    private lateinit var repository: TodoRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChoixListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val pseudo = intent.getStringExtra("EXTRA_PSEUDO")
        if (pseudo == null) {
            showError("Erreur : pseudo invalide")
            return finish()
        }
        
        preferencesManager = PreferencesManager(this)
        repository = TodoRepository(this)
        profil = preferencesManager.getProfil(pseudo) ?: ProfilListeToDo(pseudo)

        adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            mutableListOf()
        )
        binding.listView.adapter = adapter

        refreshLists()

        binding.listView.setOnItemClickListener { _, _, position, _ ->
            val liste = adapter.getItem(position) ?: return@setOnItemClickListener
            val listeId = liste.getId()
            if (listeId == null) {
                showError("Erreur : ID de liste invalide")
                return@setOnItemClickListener
            }
            Log.d("ChoixListActivity", "Clic sur la liste en position $position - ID: $listeId, Titre: ${liste.getTitreListe()}")
            val intent = Intent(this, ShowListActivity::class.java)
            intent.putExtra("EXTRA_PSEUDO", pseudo)
            intent.putExtra("EXTRA_LISTE_ID", listeId)
            startActivity(intent)
        }

        binding.fab.setOnClickListener {
            showAddListDialog()
        }
    }

    private fun refreshLists() {
        val hash = preferencesManager.getHash()
        val pseudo = intent.getStringExtra("EXTRA_PSEUDO")
        if (hash != null && pseudo != null) {
            lifecycleScope.launch {
                try {
                    val prefs = PreferenceManager.getDefaultSharedPreferences(this@ChoixListActivity)
                    val baseUrl = prefs.getString("api_url", "http://tomnab.fr/todo-api/") ?: "http://tomnab.fr/todo-api/"
                    
                    val lists = repository.getLists(hash, pseudo, baseUrl)
                    profil.setMesListeToDo(lists)
                    preferencesManager.saveProfil(profil)
                    adapter.clear()
                    adapter.addAll(lists)

                    if (!repository.isNetworkAvailable()) {
                        showError("Mode hors ligne : les données affichées proviennent du cache local")
                    }
                } catch (e: Exception) {
                    Log.e("ChoixListActivity", "Erreur lors de la récupération des listes", e)
                    showError("Erreur lors de la récupération des listes")
                }
            }
        }
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun showAddListDialog() {
        val editText = TextInputEditText(this).apply {
            hint = getString(R.string.new_list)
        }

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.new_list)
            .setView(editText)
            .setPositiveButton(R.string.ok) { _, _ ->
                val titre = editText.text?.toString()
                if (!titre.isNullOrBlank()) {
                    val hash = preferencesManager.getHash()
                    val pseudo = intent.getStringExtra("EXTRA_PSEUDO")
                    if (hash != null && pseudo != null) {
                        lifecycleScope.launch {
                            try {
                                val prefs = PreferenceManager.getDefaultSharedPreferences(this@ChoixListActivity)
                                val baseUrl = prefs.getString("api_url", "http://tomnab.fr/todo-api/") ?: "http://tomnab.fr/todo-api/"
                                Log.d("ChoixListActivity", "Tentative de création de liste avec le titre: $titre")
                                
                                val listId = repository.createList(titre, pseudo, hash, baseUrl)
                                Log.d("ChoixListActivity", "Liste créée avec l'ID : $listId")
                                

                                refreshLists()

                                if (!repository.isNetworkAvailable()) {
                                    showError("Liste créée en mode hors ligne. Elle sera synchronisée lorsque le réseau sera disponible.")
                                }
                            } catch (e: Exception) {
                                Log.e("ChoixListActivity", "Erreur lors de la création de la liste", e)
                                showError("Erreur lors de la création de la liste")
                            }
                        }
                    }
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
} 