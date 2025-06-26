package com.healthtracker.ncdcare.ui.screening

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.google.android.fhir.datacapture.QuestionnaireFragment
import com.healthtracker.ncdcare.R
import com.healthtracker.ncdcare.application.ContribQuestionnaireItemViewHolderFactoryMatchersProviderFactory
import com.healthtracker.ncdcare.ui.slideshow.SlideshowFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.getValue
import androidx.core.net.toUri
import androidx.core.view.MenuProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.NavHostFragment
import com.google.android.fhir.datacapture.QuestionnaireFragment.Companion.SUBMIT_REQUEST_KEY

/** A fragment class to show screener questionnaire screen. */
class ScreeningFragment : Fragment() {

    private val args: ScreeningFragmentArgs by navArgs()
    private val viewModel: ScreeningViewModel by viewModels()
    private var progressBar: ProgressBar? = null

    companion object {
        const val QUESTIONNAIRE_FILE_PATH_KEY = "questionnaire-file-path-key"
        const val QUESTIONNAIRE_FRAGMENT_TAG = "questionnaire-fragment-tag"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_container_questionnaire, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        progressBar = view.findViewById<ProgressBar>(R.id.progress_bar_loading_questionnaire)
        progressBar?.visibility = View.VISIBLE

        onBackPressed()

        viewModel.isResourcesSaved.observe(viewLifecycleOwner) { saved ->
            if (saved) {
                NavHostFragment.findNavController(this).navigateUp()
            }
        }

        childFragmentManager.setFragmentResultListener(SUBMIT_REQUEST_KEY, viewLifecycleOwner) { _, _ ->
            onSubmitQuestionnaireClick()
        }

        if (savedInstanceState == null) {
            viewLifecycleOwner.lifecycleScope.launch {
                delay(5)
                val startTime = System.currentTimeMillis()
                addQuestionnaireFragment()
                view.post {
                    val endTime = System.currentTimeMillis()
                    progressBar?.visibility = View.GONE
                    Log.d("PerfDebug", "QuestionnaireFragment added in ${endTime - startTime} ms")
                }
            }
        }
    }
    private fun updateArguments() {
        requireArguments().putString(QUESTIONNAIRE_FILE_PATH_KEY, "screening-questionnaire.json")
    }

    private fun addQuestionnaireFragment() {
        viewLifecycleOwner.lifecycleScope.launch {
            if (childFragmentManager.findFragmentByTag(SlideshowFragment.Companion.QUESTIONNAIRE_FRAGMENT_TAG) == null) {
                childFragmentManager.commit {
                    setReorderingAllowed(true)
                    val questionnaireUri = args.questionnaireUriString!!.toUri()
                    val questionnaireFragment =
                        QuestionnaireFragment.builder()
                            .apply {
                                setCustomQuestionnaireItemViewHolderFactoryMatchersProvider(
                                    ContribQuestionnaireItemViewHolderFactoryMatchersProviderFactory
                                        .LOCATION_WIDGET_PROVIDER,
                                )
                                setQuestionnaire(questionnaireUri)
                            }
                            .build()
                    add(R.id.container, questionnaireFragment,
                        SlideshowFragment.Companion.QUESTIONNAIRE_FRAGMENT_TAG
                    )
                }
            }
        }
    }

    private fun onBackPressed() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // No menu inflation needed here, keep empty
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    android.R.id .home -> {
                        showCancelScreenerQuestionnaireAlertDialog()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun showCancelScreenerQuestionnaireAlertDialog() {
        val alertDialog: AlertDialog? =
            activity?.let {
                val builder = AlertDialog.Builder(it)
                builder.apply {
                    setMessage(getString(R.string.cancel_questionnaire_message))
                    setPositiveButton(getString(android.R.string.yes)) { _, _ ->
                        NavHostFragment.findNavController(this@ScreeningFragment).navigateUp()
                    }
                    setNegativeButton(getString(android.R.string.no)) { _, _ -> }
                }
                builder.create()
            }
        alertDialog?.show()
    }

    private fun setUpActionBar() {
        val activity = requireActivity()
        if (activity is AppCompatActivity) {
            activity.supportActionBar?.apply {
                setDisplayHomeAsUpEnabled(true)
                title = args.questionnaireTitleKey
            }
        }
        setHasOptionsMenu(true)
    }

    override fun onResume() {
        super.onResume()
        setUpActionBar()
    }
    private fun onSubmitQuestionnaireClick() {
        progressBar?.visibility = View.VISIBLE
        Toast.makeText(requireActivity(), "Submitting questionnaire, please wait ...", Toast.LENGTH_SHORT).show()
        lifecycleScope.launch {
            val questionnaireFragment =
                childFragmentManager.findFragmentByTag(QUESTIONNAIRE_FRAGMENT_TAG) as QuestionnaireFragment
            val questionnaireResponse = questionnaireFragment.getQuestionnaireResponse()
            viewModel.saveScreenerEncounter(questionnaireResponse, args.patientId!!)
        }
    }

}

