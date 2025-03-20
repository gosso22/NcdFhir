package com.healthtracker.ncdcare.ui.home

import android.content.Context
import android.os.Bundle
import android.util.Log
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
import com.google.android.fhir.sync.CurrentSyncJobStatus
import com.google.android.material.search.SearchView
import com.healthtracker.ncdcare.MainActivity
import com.healthtracker.ncdcare.R
import com.healthtracker.ncdcare.databinding.FragmentHomeBinding
import com.healthtracker.ncdcare.databinding.FragmentPatientListViewBinding
import kotlinx.coroutines.launch

class PatientListFragment : Fragment() {

    private lateinit var searchView: SearchView
    private var _binding: FragmentPatientListViewBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val patientAdapter = PatientItemRecyclerViewAdapter()
    private val viewModel: PatientListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
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
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Show the user a message when the sync is finished and then refresh the list of patients
                // on the UI by sending a search patient request
                viewModel.pollState.collect { handleSyncJobStatus(it) }
            }
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
            // Set up the Material SearchView
//            searchView.editText.setOnEditorActionListener { textView, actionId, keyEvent ->
//                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
//                    val query = textView.text.toString()
//                    viewModel.searchPatientsByName(query)
//                    return@setOnEditorActionListener true
//                }
//                false
//            }

            // Listen for text changes
            searchView.editText.doOnTextChanged { text, _, _, _ ->
                viewModel.searchPatientsByName(text?.toString() ?: "")
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