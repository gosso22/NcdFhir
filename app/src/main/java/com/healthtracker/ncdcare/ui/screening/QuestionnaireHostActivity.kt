package com.healthtracker.ncdcare.ui.screening

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import ca.uhn.fhir.context.FhirContext
import com.google.android.fhir.datacapture.QuestionnaireFragment
import com.healthtracker.ncdcare.R
import com.healthtracker.ncdcare.ui.screening.ScreeningFragment.Companion.QUESTIONNAIRE_FRAGMENT_TAG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class QuestionnaireHostActivity : AppCompatActivity(R.layout.activity_questionnaire_host) {

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Warm-up FhirContext
        lifecycleScope.launch(Dispatchers.Default) {
            FhirContext.forR4Cached()
        }

        // First-time creation
 /*       if (savedInstanceState == null) {
            val patientId = intent.getStringExtra("patientId")
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                replace(
                    R.id.fragment_container,
                    ScreeningFragment.newInstance(patientId)
                )
            }
        }*/

        // Handle questionnaire submission
        supportFragmentManager.setFragmentResultListener(
            com.google.android.fhir.datacapture.QuestionnaireFragment.SUBMIT_REQUEST_KEY,
            this
        ) { _, result ->

            val questionnaireFragment =
                supportFragmentManager.findFragmentByTag(QUESTIONNAIRE_FRAGMENT_TAG) as QuestionnaireFragment
            lifecycleScope.launch {
                val questionnaireResponse = questionnaireFragment.getQuestionnaireResponse()
                Log.d("QuestionnaireHostActivity", "Questionnaire submitted: $questionnaireResponse")
            }
            finish() // or navigate, or save, etc.
        }
    }
}
