package com.example.data.local

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.HistoryEntry
import com.example.data.model.SavedItem
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "streambert_prefs")

object Prefs {
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

    // Moshi types for lists & maps
    private val historyListType = Types.newParameterizedType(List::class.java, HistoryEntry::class.java)
    private val historyAdapter = moshi.adapter<List<HistoryEntry>>(historyListType)

    private val savedItemMapType = Types.newParameterizedType(Map::class.java, String::class.java, SavedItem::class.java)
    private val savedItemMapAdapter = moshi.adapter<Map<String, SavedItem>>(savedItemMapType)

    private val stringListType = Types.newParameterizedType(List::class.java, String::class.java)
    private val stringListAdapter = moshi.adapter<List<String>>(stringListType)

    private val floatMapType = Types.newParameterizedType(Map::class.java, String::class.java, java.lang.Double::class.java)
    private val floatMapAdapter = moshi.adapter<Map<String, Double>>(floatMapType)

    private val booleanMapType = Types.newParameterizedType(Map::class.java, String::class.java, java.lang.Boolean::class.java)
    private val booleanMapAdapter = moshi.adapter<Map<String, Boolean>>(booleanMapType)

    // Preference Keys
    val TMDB_KEY = stringPreferencesKey("tmdb_key")
    val IS_TV_MODE = booleanPreferencesKey("is_tv_mode")
    val SETUP_DONE = booleanPreferencesKey("setup_done")
    val THEME = stringPreferencesKey("theme")
    val ACCENT_COLOR = stringPreferencesKey("accent_color")
    val TMDB_LANG = stringPreferencesKey("tmdb_lang")
    val AGE_LIMIT = stringPreferencesKey("age_limit")
    val RATING_COUNTRY = stringPreferencesKey("rating_country")
    val WATCHED_THRESHOLD = intPreferencesKey("watched_threshold")
    val HISTORY_ENABLED = booleanPreferencesKey("history_enabled")
    val PLAYER_SOURCE = stringPreferencesKey("player_source")
    val AUTOPLAY_NEXT_ENABLED = booleanPreferencesKey("autoplay_next_enabled")
    val AUTOPLAY_NEXT_DURATION = intPreferencesKey("autoplay_next_duration")
    val INTRO_SKIP_MODE = stringPreferencesKey("intro_skip_mode")
    val COMPACT_MODE = booleanPreferencesKey("compact_mode")
    val HOME_VIEW_MODE = stringPreferencesKey("home_view_mode")
    val HOME_ROW_ORDER = stringPreferencesKey("home_row_order")
    val HOME_ROW_VISIBLE = stringPreferencesKey("home_row_visible")
    val SUBTITLE_ENABLED = booleanPreferencesKey("subtitle_enabled")
    val SUBTITLE_LANG = stringPreferencesKey("subtitle_lang")
    val INVIDIOUS_BASE = stringPreferencesKey("invidious_base")
    val WATCH_PROGRESS = stringPreferencesKey("watch_progress")
    val WATCHED = stringPreferencesKey("watched")
    val WATCH_HISTORY = stringPreferencesKey("watch_history")
    val SAVED = stringPreferencesKey("saved")
    val SAVED_ORDER = stringPreferencesKey("saved_order")
    val SEARCH_HISTORY = stringPreferencesKey("search_history")

    // General Helper methods
    suspend fun <T> get(context: Context, key: Preferences.Key<T>, default: T): T {
        return context.dataStore.data.map { it[key] }.first() ?: default
    }

    fun <T> getFlow(context: Context, key: Preferences.Key<T>, default: T): Flow<T> {
        return context.dataStore.data.map { it[key] ?: default }
    }

    suspend fun <T> set(context: Context, key: Preferences.Key<T>, value: T) {
        context.dataStore.edit { it[key] = value }
    }

    // Specific Getters and Setters
    suspend fun getTmdbKey(context: Context): String? = get(context, TMDB_KEY, "")
    suspend fun setTmdbKey(context: Context, key: String) = set(context, TMDB_KEY, key)

    suspend fun isTvMode(context: Context): Boolean = get(context, IS_TV_MODE, false)
    suspend fun setTvMode(context: Context, tvMode: Boolean) = set(context, IS_TV_MODE, tvMode)

    suspend fun isSetupDone(context: Context): Boolean = get(context, SETUP_DONE, false)
    suspend fun setSetupDone(context: Context, done: Boolean) = set(context, SETUP_DONE, done)

    suspend fun getTheme(context: Context): String = get(context, THEME, "dark")
    suspend fun setTheme(context: Context, theme: String) = set(context, THEME, theme)

    suspend fun getAccentColor(context: Context): String = get(context, ACCENT_COLOR, "#e50914")
    suspend fun setAccentColor(context: Context, color: String) = set(context, ACCENT_COLOR, color)

    suspend fun getTmdbLang(context: Context): String = get(context, TMDB_LANG, "en-US")
    suspend fun setTmdbLang(context: Context, lang: String) = set(context, TMDB_LANG, lang)

    suspend fun getAgeLimit(context: Context): String = get(context, AGE_LIMIT, "")
    suspend fun setAgeLimit(context: Context, limit: String) = set(context, AGE_LIMIT, limit)

    suspend fun getRatingCountry(context: Context): String = get(context, RATING_COUNTRY, "US")
    suspend fun setRatingCountry(context: Context, country: String) = set(context, RATING_COUNTRY, country)

    suspend fun getWatchedThreshold(context: Context): Int = get(context, WATCHED_THRESHOLD, 20)
    suspend fun setWatchedThreshold(context: Context, threshold: Int) = set(context, WATCHED_THRESHOLD, threshold)

    suspend fun isHistoryEnabled(context: Context): Boolean = get(context, HISTORY_ENABLED, true)
    suspend fun setHistoryEnabled(context: Context, enabled: Boolean) = set(context, HISTORY_ENABLED, enabled)

    suspend fun getPlayerSource(context: Context): String = get(context, PLAYER_SOURCE, "")
    suspend fun setPlayerSource(context: Context, source: String) = set(context, PLAYER_SOURCE, source)

    suspend fun isAutoplayNextEnabled(context: Context): Boolean = get(context, AUTOPLAY_NEXT_ENABLED, true)
    suspend fun setAutoplayNextEnabled(context: Context, enabled: Boolean) = set(context, AUTOPLAY_NEXT_ENABLED, enabled)

    suspend fun getAutoplayNextDuration(context: Context): Int = get(context, AUTOPLAY_NEXT_DURATION, 5)
    suspend fun setAutoplayNextDuration(context: Context, duration: Int) = set(context, AUTOPLAY_NEXT_DURATION, duration)

    suspend fun getIntroSkipMode(context: Context): String = get(context, INTRO_SKIP_MODE, "off")
    suspend fun setIntroSkipMode(context: Context, mode: String) = set(context, INTRO_SKIP_MODE, mode)

    suspend fun isCompactMode(context: Context): Boolean = get(context, COMPACT_MODE, false)
    suspend fun setCompactMode(context: Context, compact: Boolean) = set(context, COMPACT_MODE, compact)

    suspend fun getHomeViewMode(context: Context): String = get(context, HOME_VIEW_MODE, "carousel")
    suspend fun setHomeViewMode(context: Context, mode: String) = set(context, HOME_VIEW_MODE, mode)

    // Serialization helper for maps & lists
    suspend fun getWatchProgress(context: Context): Map<String, Float> {
        val json = get(context, WATCH_PROGRESS, "{}")
        return try {
            val rawMap = floatMapAdapter.fromJson(json) ?: emptyMap()
            rawMap.mapValues { it.value.toFloat() }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    suspend fun setWatchProgress(context: Context, progress: Map<String, Float>) {
        val doubleMap = progress.mapValues { it.value.toDouble() }
        val json = floatMapAdapter.toJson(doubleMap)
        set(context, WATCH_PROGRESS, json)
    }

    suspend fun getWatched(context: Context): Map<String, Boolean> {
        val json = get(context, WATCHED, "{}")
        return try {
            booleanMapAdapter.fromJson(json) ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }

    suspend fun setWatched(context: Context, watched: Map<String, Boolean>) {
        val json = booleanMapAdapter.toJson(watched)
        set(context, WATCHED, json)
    }

    suspend fun getWatchHistory(context: Context): List<HistoryEntry> {
        val json = get(context, WATCH_HISTORY, "[]")
        return try {
            historyAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun setWatchHistory(context: Context, history: List<HistoryEntry>) {
        val json = historyAdapter.toJson(history.take(100))
        set(context, WATCH_HISTORY, json)
    }

    suspend fun getSaved(context: Context): Map<String, SavedItem> {
        val json = get(context, SAVED, "{}")
        return try {
            savedItemMapAdapter.fromJson(json) ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }

    suspend fun setSaved(context: Context, saved: Map<String, SavedItem>) {
        val json = savedItemMapAdapter.toJson(saved)
        set(context, SAVED, json)
    }

    suspend fun getSavedOrder(context: Context): List<String> {
        val json = get(context, SAVED_ORDER, "[]")
        return try {
            stringListAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun setSavedOrder(context: Context, order: List<String>) {
        val json = stringListAdapter.toJson(order)
        set(context, SAVED_ORDER, json)
    }

    suspend fun getSearchHistory(context: Context): List<String> {
        val json = get(context, SEARCH_HISTORY, "[]")
        return try {
            stringListAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun setSearchHistory(context: Context, history: List<String>) {
        val json = stringListAdapter.toJson(history.take(12))
        set(context, SEARCH_HISTORY, json)
    }

    // Export/Import all for Backup/Restore
    suspend fun exportAll(context: Context): String {
        val map = mutableMapOf<String, String>()
        val prefs = context.dataStore.data.first()
        prefs.asMap().forEach { (k, v) ->
            if (k.name != IS_TV_MODE.name && k.name != SETUP_DONE.name) {
                map[k.name] = v.toString()
            }
        }
        val type = Types.newParameterizedType(Map::class.java, String::class.java, String::class.java)
        return moshi.adapter<Map<String, String>>(type).toJson(map)
    }

    suspend fun importAll(context: Context, json: String): Boolean {
        return try {
            val type = Types.newParameterizedType(Map::class.java, String::class.java, String::class.java)
            val map = moshi.adapter<Map<String, String>>(type).fromJson(json) ?: return false
            context.dataStore.edit { editPrefs ->
                map.forEach { (k, v) ->
                    when (k) {
                        TMDB_KEY.name -> editPrefs[TMDB_KEY] = v
                        THEME.name -> editPrefs[THEME] = v
                        ACCENT_COLOR.name -> editPrefs[ACCENT_COLOR] = v
                        TMDB_LANG.name -> editPrefs[TMDB_LANG] = v
                        AGE_LIMIT.name -> editPrefs[AGE_LIMIT] = v
                        RATING_COUNTRY.name -> editPrefs[RATING_COUNTRY] = v
                        WATCHED_THRESHOLD.name -> editPrefs[WATCHED_THRESHOLD] = v.toIntOrNull() ?: 20
                        HISTORY_ENABLED.name -> editPrefs[HISTORY_ENABLED] = v.toBoolean()
                        PLAYER_SOURCE.name -> editPrefs[PLAYER_SOURCE] = v
                        AUTOPLAY_NEXT_ENABLED.name -> editPrefs[AUTOPLAY_NEXT_ENABLED] = v.toBoolean()
                        AUTOPLAY_NEXT_DURATION.name -> editPrefs[AUTOPLAY_NEXT_DURATION] = v.toIntOrNull() ?: 5
                        INTRO_SKIP_MODE.name -> editPrefs[INTRO_SKIP_MODE] = v
                        COMPACT_MODE.name -> editPrefs[COMPACT_MODE] = v.toBoolean()
                        HOME_VIEW_MODE.name -> editPrefs[HOME_VIEW_MODE] = v
                        SUBTITLE_ENABLED.name -> editPrefs[SUBTITLE_ENABLED] = v.toBoolean()
                        SUBTITLE_LANG.name -> editPrefs[SUBTITLE_LANG] = v
                        INVIDIOUS_BASE.name -> editPrefs[INVIDIOUS_BASE] = v
                        WATCH_PROGRESS.name -> editPrefs[WATCH_PROGRESS] = v
                        WATCHED.name -> editPrefs[WATCHED] = v
                        WATCH_HISTORY.name -> editPrefs[WATCH_HISTORY] = v
                        SAVED.name -> editPrefs[SAVED] = v
                        SAVED_ORDER.name -> editPrefs[SAVED_ORDER] = v
                        SEARCH_HISTORY.name -> editPrefs[SEARCH_HISTORY] = v
                    }
                }
            }
            true
        } catch (e: Exception) {
            Log.e("Prefs", "Failed to import settings from JSON", e)
            false
        }
    }
}
