package com.healthtracker.ncdcare.ui.slideshow

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import ca.uhn.fhir.context.FhirContext
import com.google.android.fhir.datacapture.QuestionnaireFragment
import com.google.android.fhir.datacapture.QuestionnaireFragment.Companion.SUBMIT_REQUEST_KEY
import com.healthtracker.ncdcare.R
import com.healthtracker.ncdcare.application.ContribQuestionnaireItemViewHolderFactoryMatchersProviderFactory
import com.healthtracker.ncdcare.databinding.FragmentSlideshowBinding
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.Patient
import kotlin.getValue

class SlideshowFragment : Fragment() {

    private var _binding: FragmentSlideshowBinding? = null
    private var isErrorState = false
    private val args: SlideshowFragmentArgs by navArgs()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return inflater.inflate(R.layout.fragment_demo_questionnaire, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setFragmentResultListener(REQUEST_ERROR_KEY) { _, bundle ->
            isErrorState = bundle.getBoolean(BUNDLE_ERROR_KEY)
            replaceQuestionnaireFragmentWithQuestionnaireJson()
        }
        childFragmentManager.setFragmentResultListener(SUBMIT_REQUEST_KEY, viewLifecycleOwner) { _, _ ->
            onSubmitQuestionnaireClick()
        }
        if (savedInstanceState == null) {
            addQuestionnaireFragment()
        }
        //(activity as? MainActivity)?.showOpenQuestionnaireMenu(false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun addQuestionnaireFragment() {
        viewLifecycleOwner.lifecycleScope.launch {
            if (childFragmentManager.findFragmentByTag(QUESTIONNAIRE_FRAGMENT_TAG) == null) {
                childFragmentManager.commit {
                    setReorderingAllowed(true)
                    val questionnaireFragment =
                        QuestionnaireFragment.builder()
                            .apply {
                                setCustomQuestionnaireItemViewHolderFactoryMatchersProvider(
                                    ContribQuestionnaireItemViewHolderFactoryMatchersProviderFactory
                                        .LOCATION_WIDGET_PROVIDER,
                                )
                                setQuestionnaire(args.questionnaireJsonStringKey!!)
                            }
                            .build()
                    add(R.id.container, questionnaireFragment,
                        QUESTIONNAIRE_FRAGMENT_TAG
                    )
                }
            }
        }
    }

    private fun replaceQuestionnaireFragmentWithQuestionnaireJson() {
        // TODO: remove check once all files are added
        if (args.questionnaireWithValidationJsonStringKey.isNullOrEmpty()) {
            return
        }
        viewLifecycleOwner.lifecycleScope.launch {
            val questionnaireJsonString =
                if (isErrorState) {
                    args.questionnaireWithValidationJsonStringKey!!
                } else {
                    args.questionnaireJsonStringKey!!
                }
            childFragmentManager.commit {
                setReorderingAllowed(true)
                replace(
                    R.id.container,
                    QuestionnaireFragment.builder()
                        .setQuestionnaire(questionnaireJsonString)
                        .setQuestionnaireLaunchContextMap(
                            FhirContext.forR4Cached()
                                .newJsonParser()
                                .encodeResourceToString(Patient().apply { id = "P1" })
                                .let { mapOf("patient" to it) },
                        )
                        .setSubmitButtonText(
                            getString(R.string.submit),
                        )
                        .build(),
                    QUESTIONNAIRE_FRAGMENT_TAG,
                )
            }
        }
    }

    private fun onSubmitQuestionnaireClick() {
        lifecycleScope.launch {
            val questionnaireFragment =
                childFragmentManager.findFragmentByTag(QUESTIONNAIRE_FRAGMENT_TAG) as QuestionnaireFragment
/*            launchQuestionnaireResponseFragment(
                viewModel.getQuestionnaireResponseJson(questionnaireFragment.getQuestionnaireResponse()),
            )*/
        }
    }

    companion object {
        const val REQUEST_ERROR_KEY = "errorRequestKey"
        const val BUNDLE_ERROR_KEY = "errorBundleKey"
        const val QUESTIONNAIRE_FRAGMENT_TAG = "questionnaire-fragment-tag"
    }
}