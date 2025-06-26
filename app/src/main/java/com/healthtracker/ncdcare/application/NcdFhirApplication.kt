package com.healthtracker.ncdcare.application

import android.app.Application
import android.content.Context
import android.util.Log
import ca.uhn.fhir.context.FhirContext
import com.google.android.fhir.DatabaseErrorStrategy
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.FhirEngineConfiguration
import com.google.android.fhir.FhirEngineProvider
import com.google.android.fhir.ServerConfiguration
import com.google.android.fhir.datacapture.DataCaptureConfig
import com.google.android.fhir.search.search
import com.google.android.fhir.sync.remote.HttpLogger
import com.healthtracker.ncdcare.BuildConfig
import com.healthtracker.ncdcare.NcdAppDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.Questionnaire

class NcdFhirApplication: Application() {

    private val fhirEngine: FhirEngine by lazy { FhirEngineProvider.getInstance(this) }

    private val dataStore by lazy { NcdAppDataStore(this) }
    private lateinit var dataCaptureConfig: DataCaptureConfig


    override fun onCreate() {
        super.onCreate()

        CoroutineScope(Dispatchers.Default).launch {
            FhirContext.forR4Cached()
        }

        FhirEngineProvider.init(
            FhirEngineConfiguration(
                enableEncryptionIfSupported = false,
                DatabaseErrorStrategy.RECREATE_AT_OPEN,
                ServerConfiguration(
                    baseUrl = "http://9.169.79.253/fhir/",
                    httpLogger =
                        HttpLogger(
                            HttpLogger.Configuration(
                                if (BuildConfig.DEBUG) HttpLogger.Level.BODY else HttpLogger.Level.BASIC,
                            ),
                        ) {
                            Log.d("App-HttpLog", it)
                        }
                ),
            ),
        )

        dataCaptureConfig =
            DataCaptureConfig(
                xFhirQueryResolver = { fhirEngine.search(it).map { it.resource } },
                questionnaireItemViewHolderFactoryMatchersProviderFactory =
                    ContribQuestionnaireItemViewHolderFactoryMatchersProviderFactory,
            )
    }

    companion object {
        fun fhirEngine(context: Context): FhirEngine =
            (context.applicationContext as NcdFhirApplication).fhirEngine

        fun dataStore(context: Context) =
            (context.applicationContext as NcdFhirApplication).dataStore
    }
}