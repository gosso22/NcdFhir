package com.healthtracker.ncdcare.ui.home

import android.app.Application
import android.text.format.DateFormat
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.fhir.search.Order
import com.google.android.fhir.search.StringFilterModifier
import com.google.android.fhir.search.search
import com.google.android.fhir.sync.CurrentSyncJobStatus
import com.google.android.fhir.sync.Sync
import com.healthtracker.ncdcare.application.AppFhirSyncWorker
import com.healthtracker.ncdcare.application.NcdFhirApplication
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.Patient
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class PatientListViewModel(application: Application) : AndroidViewModel(application) {

    private val _pollState = MutableSharedFlow<CurrentSyncJobStatus>()
    val pollState: Flow<CurrentSyncJobStatus> get() = _pollState

    // Loading state for patient list
    private val _isLoading = MutableLiveData<Boolean>(true)
    val isLoading: MutableLiveData<Boolean> get() = _isLoading

    // Loading state for sync operations
    private val _isSyncing = MutableLiveData<Boolean>(false)
    val isSyncing: MutableLiveData<Boolean> get() = _isSyncing

    // Error state
    private val _error = MutableLiveData<String?>(null)
    val error: MutableLiveData<String?> get() = _error

    // Track current search term
    private val _currentSearchTerm = MutableLiveData<String>("")
    val currentSearchTerm: LiveData<String> get() = _currentSearchTerm

    val liveSearchedPatients = MutableLiveData<List<Patient>>()

    private val _lastSyncTimestampLiveData = MutableLiveData<String>()
    val lastSyncTimestampLiveData: LiveData<String>
        get() = _lastSyncTimestampLiveData

    init {
        updatePatientList { getSearchResults() }
    }

    fun triggerOneTimeSync() {
        viewModelScope.launch {
            try {
                _isSyncing.value = true
                _error.value = null
                Sync.oneTimeSync<AppFhirSyncWorker>(getApplication())
                    .shareIn(this, SharingStarted.Eagerly, 10)
                    .collect { status ->
                        _pollState.emit(status)
                        when (status) {
                            is CurrentSyncJobStatus.Enqueued -> {
                                Log.d("Sync", "Sync job enqueued")
                            }

                            is CurrentSyncJobStatus.Running -> {
                                Log.d("Sync", "Sync job running: ${status.inProgressSyncJob}")
                            }

                            is CurrentSyncJobStatus.Succeeded -> {
                                _isSyncing.value = false
                                // Refresh patient list after sync completes
                                updatePatientList { getSearchResults() }
                                updateLastSyncTimestamp(status.timestamp)
                            }

                            is CurrentSyncJobStatus.Failed -> {
                                _error.value = "Sync failed"
                            }

                            else -> {
                                Log.d("Sync", "Sync job status: $status")
                            }
                        }
                    }
            } catch (e: Exception) {
                _error.value = "Sync failed: ${e.message}"
                _isSyncing.value = false
            }
        }
    }

    fun triggerUpdate() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val fhirEngine = NcdFhirApplication.fhirEngine(getApplication())
                // Implement city update logic here

                // After update is complete, trigger sync
                _isLoading.value = false
                triggerOneTimeSync()
            } catch (e: Exception) {
                _error.value = "Update failed: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun searchPatientsByName(nameQuery: String) {
        // Update the current search term
        _currentSearchTerm.value = nameQuery

        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val fhirEngine = NcdFhirApplication.fhirEngine(getApplication())
                if (nameQuery.isNotEmpty()) {
                    val searchResults = fhirEngine.search<Patient> {
                        filter(
                            Patient.NAME,
                            {
                                modifier = StringFilterModifier.CONTAINS
                                value = nameQuery
                            },
                        )
                        count = 10
                    }
                        .filter { patient ->
                            !patient.resource.name.any { it.family?.contains("Family") == true }
                        }
                    liveSearchedPatients.value = searchResults.map { it.resource }
                } else {
                    // If search term is empty, load all patients
                    updatePatientList { getSearchResults() }
                }
            } catch (e: Exception) {
                _error.value = "Search failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchPatientsPaged(offset: Int, pageSize: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val fhirEngine = NcdFhirApplication.fhirEngine(getApplication())
                val results = fhirEngine.search<Patient> {
                    sort(Patient.GIVEN, Order.ASCENDING)
                    from = offset
                    count = pageSize
                }.filterNot { patient ->
                    patient.resource.name.any {
                        it.family?.contains("Family") == true || it.family?.contains("0") == true
                    }
                }.map { it.resource }

                liveSearchedPatients.value = results
            } catch (e: Exception) {
                _error.value = "Failed to load paged patients: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * [updatePatientList] calls the search and count lambda and updates the live data values
     * accordingly. It is initially called when this [ViewModel] is created. Later its called by the
     * client every time search query changes or data-sync is completed.
     */
    private fun updatePatientList(
        search: suspend () -> List<Patient>,
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                liveSearchedPatients.value = search()
            } catch (e: Exception) {
                _error.value = "Failed to load patients: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun getSearchResults(): List<Patient> {
        val patients: MutableList<Patient> = mutableListOf()
        NcdFhirApplication.fhirEngine(this.getApplication())
            .search<Patient> { sort(Patient.GIVEN, Order.ASCENDING) }
            .filterNot { patient ->
                patient.resource.name.any { name ->
                    name.family?.contains("Family") == true || name.family?.contains("0") == true
                }
            }
            .let { patients.addAll(it.map { it.resource }) }
        return patients
    }

    // Clear any ongoing operations when ViewModel is cleared
    override fun onCleared() {
        super.onCleared()
        _isLoading.value = false
        _isSyncing.value = false
    }

    fun updateLastSyncTimestamp(lastSync: OffsetDateTime? = null) {
        val formatter =
            DateTimeFormatter.ofPattern(
                if (DateFormat.is24HourFormat(getApplication())) formatString24 else formatString12,
            )
        _lastSyncTimestampLiveData.value =
            lastSync?.let { it.toLocalDateTime()?.format(formatter) ?: "" }
                ?: Sync.getLastSyncTimestamp(getApplication())?.toLocalDateTime()?.format(formatter)
                        ?: ""
    }

    companion object {
        private const val formatString24 = "yyyy-MM-dd HH:mm:ss"
        private const val formatString12 = "yyyy-MM-dd hh:mm:ss a"
    }
}