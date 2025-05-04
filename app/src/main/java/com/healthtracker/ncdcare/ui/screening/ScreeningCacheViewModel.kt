package com.healthtracker.ncdcare.ui.screening

import androidx.lifecycle.ViewModel
import com.google.android.fhir.datacapture.QuestionnaireFragment

class ScreeningCacheViewModel : ViewModel() {
    var cachedJson: String? = null
    var prebuiltFragment: QuestionnaireFragment? = null
}