package com.healthtracker.ncdcare.ui.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.fhir.sync.CurrentSyncJobStatus
import com.google.android.material.search.SearchView
import com.healthtracker.ncdcare.MainActivity
import com.healthtracker.ncdcare.R
import com.healthtracker.ncdcare.databinding.FragmentPatientListViewBinding
import com.healthtracker.ncdcare.ui.profile.PatientProfileView
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.Patient
import kotlin.jvm.java

/**
 * A Fragment that displays a list of patients and provides search functionality.
 *
 * This fragment initializes and manages a RecyclerView to display patient data,
 * a SearchView for searching patients by name, and observes various LiveData
 * from the ViewModel to update the UI accordingly. It also handles menu actions
 * for syncing and updating patient data.
 */
class PatientListFragment : Fragment() {

    private lateinit var searchView: SearchView
    private lateinit var noResultsTextView: TextView
    private var _binding: FragmentPatientListViewBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val patientAdapter = PatientItemRecyclerViewAdapter()
    private val searchResultsAdapter = PatientItemRecyclerViewAdapter()
    private val viewModel: PatientListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentPatientListViewBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initSearchView()
        initMenu()

        binding.patientList.adapter = patientAdapter

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.loadingContainer.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.isSyncing.observe(viewLifecycleOwner) { isSyncing ->
            Toast.makeText(requireContext(), "Syncing with server ...", Toast.LENGTH_SHORT).show()
        }

        viewModel.liveSearchedPatients.observe(viewLifecycleOwner) { searchedPatients ->
            patientAdapter.submitList(searchedPatients)
            searchResultsAdapter.submitList(searchedPatients)

            if (this::noResultsTextView.isInitialized) {
                val searchTerm = viewModel.currentSearchTerm.value ?: ""
                val showNoResults = searchTerm.isNotEmpty() && searchedPatients.isEmpty()
                noResultsTextView.visibility = if (showNoResults) View.VISIBLE else View.GONE
            }
        }

        // Observe the current search term and update the search view
        viewModel.currentSearchTerm.observe(viewLifecycleOwner) { searchTerm ->
            if (this::searchView.isInitialized && searchView.editText.text.toString() != searchTerm) {
                searchView.editText.setText(searchTerm)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Show the user a message when the sync is finished and then refresh the list of patients
                // on the UI by sending a search patient request
                viewModel.pollState.collect { handleSyncJobStatus(it) }
            }
        }

        patientAdapter.setOnItemClickListener { patient ->
            navigateToScreening(patient)
        }

        searchResultsAdapter.setOnItemClickListener { patient ->
            requireActivity().runOnUiThread {
                navigateToScreening(patient)
            }
        }
    }

    private fun navigateToScreening(patient: Patient) {
        val navController = findNavController()
        val currentDestinationId = navController.currentDestination?.id

        Log.d("NavigationDebug", "Attempting to navigate from $currentDestinationId")

        if (currentDestinationId == R.id.patientProfileFragment || currentDestinationId == R.id.nav_home) {
            if (this::searchView.isInitialized) {
                searchView.hide()
                searchView.editText.setText("")
            }
            val action = PatientListFragmentDirections.actionPatientListFragmentToPatientProfileFragment(patient)
            navController.navigate(action)
        } else {
            Log.e("NavigationError", "Invalid navigation attempt from $currentDestinationId")
        }
    }


    private fun handleSyncJobStatus(syncJobStatus: CurrentSyncJobStatus) {
        // Add code to display Toast when sync job  is complete
        when (syncJobStatus) {
            is CurrentSyncJobStatus.Enqueued -> {
                Log.d("Sync", "Sync Job Enqueued")
            }

            is CurrentSyncJobStatus.Running -> {
                Log.d("Sync", "Sync Job Running")
            }

            is CurrentSyncJobStatus.Failed -> {
                Log.e("Sync", "Sync Job Failed")
            }

            is CurrentSyncJobStatus.Succeeded -> {
                Toast.makeText(requireContext(), "Sync Finished", Toast.LENGTH_SHORT).show()
                viewModel.searchPatientsByName("")
            }

            else -> {
                Log.wtf("Sync", "Unknown sync status")
            }
        }
    }

    private fun initMenu() {
        (requireActivity() as MenuHost).addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.main, menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    return when (menuItem.itemId) {
                        R.id.sync -> {
                            viewModel.triggerOneTimeSync()
                            true
                        }

                        R.id.update -> {
                            viewModel.triggerUpdate()
                            true
                        }

                        else -> false
                    }
                }
            },
            viewLifecycleOwner,
            Lifecycle.State.RESUMED,
        )
    }

    private fun initSearchView() {
        (requireActivity() as MainActivity).getSearchView { searchView ->
            this.searchView = searchView

            val searchResultsRecyclerView = RecyclerView(requireContext()).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )
                layoutManager = LinearLayoutManager(context)
                adapter = searchResultsAdapter
                setPadding(16, 16, 16, 16)
                clipToPadding = false
            }

            noResultsTextView = TextView(requireContext()).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )
                gravity = Gravity.CENTER_HORIZONTAL
                setTextAppearance(android.R.style.TextAppearance_Material_Body2)
                text = "No patients found"
                textSize = 16f
                visibility = View.GONE
                setPadding(16, 64, 16, 16)
            }

            val searchContentContainer =
                searchView.findViewById<ViewGroup>(com.google.android.material.R.id.open_search_view_content_container)
            searchContentContainer.addView(searchResultsRecyclerView)
            searchContentContainer.addView(noResultsTextView)

            // Set up the Material SearchView
            searchView.editText.setOnEditorActionListener { textView, actionId, keyEvent ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    val query = textView.text.toString()
                    viewModel.searchPatientsByName(query)
                    // Hide keyboard after search
                    val imm =
                        requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(textView.windowToken, 0)
                    return@setOnEditorActionListener true
                }
                false
            }

            // Initialize search view with current search term if available
            viewModel.currentSearchTerm.value?.let { currentTerm ->
                if (currentTerm.isNotEmpty() && searchView.editText.text.toString() != currentTerm) {
                    searchView.editText.setText(currentTerm)
                }
            }

            // Listen for text changes
            searchView.editText.doOnTextChanged { text, _, _, _ ->
                val newText = text?.toString() ?: ""
                if (viewModel.currentSearchTerm.value != newText) {
                    viewModel.searchPatientsByName(newText)
                }
            }

            // Handle search view state changes
            searchView.addTransitionListener { _, previousState, newState ->
                when (newState) {
                    SearchView.TransitionState.SHOWING -> {
                        // SearchView is being shown
                    }

                    SearchView.TransitionState.SHOWN -> {
                        // SearchView is fully visible
                    }

                    SearchView.TransitionState.HIDING -> {
                        // SearchView is being hidden
                    }

                    SearchView.TransitionState.HIDDEN -> {
                        // SearchView is fully hidden
                        viewModel.searchPatientsByName("")
                        noResultsTextView.visibility = View.GONE
                    }
                }
            }

            requireActivity()
                .onBackPressedDispatcher
                .addCallback(
                    viewLifecycleOwner,
                    object : OnBackPressedCallback(true) {
                        override fun handleOnBackPressed() {
                            if (searchView.editText.text.isNotEmpty()) {
                                searchView.editText.setText("")
                            } else {
                                isEnabled = false
                                activity?.onBackPressed()
                            }
                        }
                    },
                )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}