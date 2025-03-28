package com.healthtracker.ncdcare

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import org.hl7.fhir.r4.model.ResourceType

private val Context.dataStorage: DataStore<Preferences> by preferencesDataStore("ncd_care_data_store")

class NcdAppDataStore(private val context: Context) {

    suspend fun saveLastUpdatedTimestamp(resourceType: ResourceType, timestamp: String) {
        context.dataStorage.edit { pref ->
            pref[stringPreferencesKey(resourceType.name)] = timestamp
        }
    }

    suspend fun getLastUpdatedTimestamp(resourceType: ResourceType): String? {
        return context.dataStorage.data.first()[stringPreferencesKey(resourceType.name)]
    }

}