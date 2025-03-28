package com.healthtracker.ncdcare.application

import android.content.Context
import androidx.work.WorkerParameters
import com.google.android.fhir.sync.AcceptLocalConflictResolver
import com.google.android.fhir.sync.FhirSyncWorker
import com.google.android.fhir.sync.upload.HttpCreateMethod
import com.google.android.fhir.sync.upload.HttpUpdateMethod
import com.google.android.fhir.sync.upload.UploadStrategy

class AppFhirSyncWorker(appContext: Context, workerParams: WorkerParameters) :
    FhirSyncWorker(appContext, workerParams) {

    override fun getConflictResolver() = AcceptLocalConflictResolver

    override fun getDownloadWorkManager() = DownloadWorkManagerImpl(NcdFhirApplication.dataStore(applicationContext))

    override fun getFhirEngine() = NcdFhirApplication.fhirEngine(applicationContext)

    override fun getUploadStrategy() =
        UploadStrategy.forBundleRequest(
            methodForCreate = HttpCreateMethod.PUT,
            methodForUpdate = HttpUpdateMethod.PATCH,
            squash = true,
            bundleSize = 500
        )
}