package com.example.arcanoid

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "arkanoid")

object ScoreManager {
    private val BEST_SCORE_KEY = intPreferencesKey("best_score")

    fun getBestScore(context: Context): Flow<Int> {
        return context.dataStore.data.map { preferences ->
            preferences[BEST_SCORE_KEY] ?: 0
        }
    }

    suspend fun saveBestScore(context: Context, score: Int) {
        context.dataStore.edit { preferences ->
            preferences[BEST_SCORE_KEY] = score
        }
    }
}