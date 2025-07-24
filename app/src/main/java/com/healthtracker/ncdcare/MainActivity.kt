package com.healthtracker.ncdcare

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import ca.uhn.fhir.context.FhirContext
import com.google.android.fhir.datacapture.QuestionnaireFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.search.SearchBar
import com.google.android.material.search.SearchView
import com.healthtracker.ncdcare.databinding.ActivityMainBinding
import com.healthtracker.ncdcare.utils.getQuestionnaireJsonStringFromAssets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private lateinit var searchBar: SearchBar
    private lateinit var _searchView: SearchView

    private val searchViewReadyCallbacks = mutableListOf<(SearchView) -> Unit>()

    fun getSearchView(callback: (SearchView) -> Unit) {
        if (::_searchView.isInitialized) {
            callback(_searchView)
        } else {
            searchViewReadyCallbacks.add(callback)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch(Dispatchers.IO) {
            FhirContext.forR4Cached()
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        try {
            searchBar = binding.appBarMain.searchBar
            _searchView = binding.appBarMain.searchView

            searchBar.setOnClickListener {
                _searchView.show()
            }

            searchViewReadyCallbacks.forEach { it(_searchView) }
            searchViewReadyCallbacks.clear()
        } catch (e: Exception) {
            Log.e("MainActivity", "Error setting up searchView", e)
        }

        binding.appBarMain.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .setAnchorView(R.id.fab).show()
        }
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val appBar = binding.appBarMain.toolbar
            val fab = binding.appBarMain.fab

            when (destination.id) {
                R.id.nav_patient_screening -> {
                    searchBar.visibility = View.GONE
                    fab.visibility = View.GONE
                    appBar.visibility = View.VISIBLE
                }
                R.id.patientProfileFragment -> {
                    appBar.visibility = View.GONE
                }
                else -> {
                    searchBar.visibility = View.VISIBLE
                    fab.visibility = View.VISIBLE
                    appBar.visibility = View.VISIBLE
                }
            }

        }
        setupNavigationNew()
    }

    private fun setupNavigationNew() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment

        val navController = navHostFragment.navController
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        NavigationUI.setupWithNavController(bottomNav, navController)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}