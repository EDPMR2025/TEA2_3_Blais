package com.example.tea3_blais

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.example.tea3_blais.api.ApiClient
import com.example.tea3_blais.databinding.ActivityMainBinding
import com.example.tea3_blais.utils.PreferencesManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferencesManager = PreferencesManager(this)

        binding.pseudoEditText.setText(preferencesManager.getLastPseudo())

        val pseudoHistory = preferencesManager.getPseudoHistory().toList()
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, pseudoHistory)
        (binding.pseudoEditText as? AutoCompleteTextView)?.setAdapter(adapter)

        binding.okButton.isEnabled = true

        binding.okButton.setOnClickListener {
            val pseudo = binding.pseudoEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (pseudo.isBlank() || password.isBlank()) {
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val prefs = PreferenceManager.getDefaultSharedPreferences(this@MainActivity)
                    val baseUrl = prefs.getString("api_url", "http://tomnab.fr/todo-api/") ?: "http://tomnab.fr/todo-api/"
                    
                    if (isNetworkAvailable()) {
                        Log.d("MainActivity", "Tentative de connexion avec : $pseudo / $password à $baseUrl")
                        val response = ApiClient.getApi(baseUrl).authenticate(pseudo, password)

                        Log.d("MainActivity", "Code de réponse : ${response.code()}")
                        Log.d("MainActivity", "Corps de la réponse : ${response.body()}")
                        if (!response.isSuccessful) {
                            Log.d("MainActivity", "Message d'erreur : ${response.errorBody()?.string()}")
                        }

                        if (response.isSuccessful) {
                            val hash = response.body()?.hash
                            if (hash != null) {
                                Log.d("MainActivity", "Hash reçu : $hash")
                                preferencesManager.saveLastPseudo(pseudo)
                                preferencesManager.saveHash(hash)
                                showError("Hash: $hash")
                                navigateToChoixList(pseudo)
                            } else {
                                showError(getString(R.string.error_auth))
                            }
                        } else {
                            showError(getString(R.string.error_auth))
                        }
                    } else {
                        val storedHash = preferencesManager.getHash()
                        if (storedHash != null) {
                            Log.d("MainActivity", "Mode hors ligne : utilisation du hash stocké")
                            showError("Mode hors ligne : connexion avec les données locales")
                            navigateToChoixList(pseudo)
                        } else {
                            showError("Mode hors ligne : aucune donnée locale disponible")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("MainActivity", "Erreur lors de l'authentification", e)
                    if (isNetworkAvailable()) {
                        showError(getString(R.string.error_server))
                    } else {
                        showError("Mode hors ligne : erreur de connexion")
                    }
                }
            }
        }
    }

    private fun navigateToChoixList(pseudo: String) {
        val intent = Intent(this@MainActivity, ChoixListActivity::class.java)
        intent.putExtra("EXTRA_PSEUDO", pseudo)
        startActivity(intent)
        finish()
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
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