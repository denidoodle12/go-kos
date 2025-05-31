package com.myskripsi.gokos.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
class SettingPreferences (private val dataStore: DataStore<Preferences>){

    suspend fun setOnboarded(onboarded: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_ONBOARDED] = onboarded
        }
    }

    fun isOnboarded(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[IS_ONBOARDED] ?: false
        }
    }

    companion object {
        private val IS_ONBOARDED = booleanPreferencesKey("is_onboarded")
    }
}