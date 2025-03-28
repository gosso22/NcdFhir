package com.healthtracker.ncdcare.application

import android.net.Uri
import com.google.android.fhir.sync.DownloadWorkManager
import com.google.android.fhir.sync.download.DownloadRequest
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Resource
import org.hl7.fhir.r4.model.ResourceType
import java.util.LinkedList
import androidx.core.net.toUri
import com.healthtracker.ncdcare.NcdAppDataStore
import org.hl7.fhir.exceptions.FHIRException
import org.hl7.fhir.r4.model.OperationOutcome
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

class DownloadWorkManagerImpl(private val dataStore: NcdAppDataStore) : DownloadWorkManager {
    private val resourceTypeList = ResourceType.entries.map { it.name }
    private val urls = LinkedList(listOf("Patient"))

    override suspend fun getNextRequest(): DownloadRequest? {
        var url = urls.poll() ?: return null

        val resourceTypeToDownload =
            ResourceType.fromCode(url.findAnyOf(resourceTypeList, ignoreCase = true)!!.second)
        dataStore.getLastUpdatedTimestamp(resourceTypeToDownload)?.let {
            url = appendLastUpdated(url, it)
        }
        return DownloadRequest.of(url)
    }

    override suspend fun getSummaryRequestUrls() = mapOf<ResourceType, String>()

    override suspend fun processResponse(response: Resource): Collection<Resource> {

        if (response is OperationOutcome) {
            throw FHIRException(response.issueFirstRep.diagnostics)
        }

        if (response is Bundle) {
            val nextUrl = response.link.firstOrNull { it.relation == "next" }?.url
            if (nextUrl != null) {
                urls.add(nextUrl)
            }
        }

        var bundleCollection: Collection<Resource> = mutableListOf()
        if (response is Bundle && response.type == Bundle.BundleType.SEARCHSET) {
            bundleCollection = response.entry.map { it.resource }.also { extractAndSaveLastUpdated(it) }
        }
        return bundleCollection
    }

    private fun appendLastUpdated(url: String, lastUpdated: String): String {
        var downloadUrl = url

        if (!downloadUrl.contains("\$everything")) {
            downloadUrl =
                downloadUrl.toUri().query?.let { "$it&_lastUpdated=gt$lastUpdated" }
                    ?: "$downloadUrl?_lastUpdated=gt$lastUpdated"
        }

        if (downloadUrl.contains("&page_token")) {
            downloadUrl = url
        }

        return downloadUrl
    }

    private suspend fun extractAndSaveLastUpdated(resources: List<Resource>) {
        resources
            .groupBy { it.resourceType }
            .entries
            .map { map ->
                dataStore.saveLastUpdatedTimestamp(
                    map.key,
                    map.value.maxOfOrNull { it.meta.lastUpdated }?.toTimeZoneString() ?: ""
                )
            }
    }
}

private fun Date.toTimeZoneString(): String {
    val simpleDateFormat =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault())
            .withZone(ZoneId.systemDefault())
    return simpleDateFormat.format(this.toInstant())
}
