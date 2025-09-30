package com.example.arcanoid

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "arkanoid")

object PreferencesManager {
    private val BEST_SCORE_KEY = intPreferencesKey("best_score")
    private val MUSIC_VOLUME_KEY = floatPreferencesKey("music_volume")
    private val SFX_VOLUME_KEY = floatPreferencesKey("sfx_volume")
    private val BALL_SPEED_KEY = floatPreferencesKey("ball_speed")
    private val CHANCE_EXTRA_BALL_BLOCK_KEY = floatPreferencesKey("chance_extra_ball")
    private val CHANCE_WIDER_PADDLE_BLOCK_KEY = floatPreferencesKey("chance_wider_paddle")

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

    fun getMusicVolume(context: Context): Flow<Float> {
        return context.dataStore.data.map { preferences ->
            preferences[MUSIC_VOLUME_KEY] ?: 1f
        }
    }

    suspend fun saveMusicVolume(context: Context, volume: Float) {
        context.dataStore.edit { preferences ->
            preferences[MUSIC_VOLUME_KEY] = volume
        }
    }

    fun getSfxVolume(context: Context): Flow<Float> =
        context.dataStore.data.map { it[SFX_VOLUME_KEY] ?: 1f }

    suspend fun saveSfxVolume(context: Context, value: Float) {
        context.dataStore.edit { it[SFX_VOLUME_KEY] = value }
    }

    fun getBallSpeed(context: Context): Flow<Float> =
        context.dataStore.data.map { it[BALL_SPEED_KEY] ?: 12f }

    suspend fun saveBallSpeed(context: Context, value: Float) {
        context.dataStore.edit { it[BALL_SPEED_KEY] = value }
    }

    fun getChanceExtraBallBlock(context: Context): Flow<Float> =
        context.dataStore.data.map { it[CHANCE_EXTRA_BALL_BLOCK_KEY] ?: 0.15f }

    suspend fun saveChanceExtraBallBlock(context: Context, value: Float) {
        context.dataStore.edit { it[CHANCE_EXTRA_BALL_BLOCK_KEY] = value }
    }

    fun getChanceWiderPaddleBlock(context: Context): Flow<Float> =
        context.dataStore.data.map { it[CHANCE_WIDER_PADDLE_BLOCK_KEY] ?: 0.1f }

    suspend fun saveChanceWiderPaddleBlock(context: Context, value: Float) {
        context.dataStore.edit { it[CHANCE_WIDER_PADDLE_BLOCK_KEY] = value }
    }
}