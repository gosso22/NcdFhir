package com.healthtracker.ncdcare.application

import android.app.Application
import android.content.Context
import android.util.Log
import com.google.android.fhir.DatabaseErrorStrategy
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.FhirEngineConfiguration
import com.google.android.fhir.FhirEngineProvider
import com.google.android.fhir.ServerConfiguration
import com.google.android.fhir.sync.remote.HttpLogger
import com.healthtracker.ncdcare.BuildConfig

class NcdFhirApplication: Application() {

    private val fhirEngine: FhirEngine by lazy { FhirEngineProvider.getInstance(this) }

    override fun onCreate() {
        super.onCreate()

        FhirEngineProvider.init(
            FhirEngineConfiguration(
                enableEncryptionIfSupported = true,
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
    }

    companion object {
        fun fhirEngine(context: Context): FhirEngine =
            (context.applicationContext as NcdFhirApplication).fhirEngine
    }
}