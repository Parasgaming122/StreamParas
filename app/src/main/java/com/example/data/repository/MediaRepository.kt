package com.example.data.repository

import android.content.Context
import com.example.data.api.AgeRating
import com.example.data.api.TmdbApi
import com.example.data.local.Prefs
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object MediaRepository {

    suspend fun configureApi(context: Context) {
        var key = Prefs.getTmdbKey(context) ?: ""
        if (key.isBlank()) {
            key = try {
                com.example.BuildConfig.TMDB_KEY
            } catch (e: Throwable) {
                ""
            }
            if (key.isBlank()) {
                key = System.getenv("TMDB_KEY") ?: ""
            }
            if (key.isNotBlank()) {
                Prefs.setTmdbKey(context, key)
            }
        }
        val lang = Prefs.getTmdbLang(context)
        TmdbApi.configure(key, lang)
    }

    suspend fun getTrendingMovies(): List<MediaItem> {
        return if (!TmdbApi.hasKey()) emptyList() else TmdbApi.getTrendingMovies()
    }

    suspend fun getTrendingTv(): List<MediaItem> {
        return if (!TmdbApi.hasKey()) emptyList() else TmdbApi.getTrendingTv()
    }

    suspend fun getTopRated(): List<MediaItem> {
        if (!TmdbApi.hasKey()) return emptyList()
        return try {
            val movies = TmdbApi.getTopRatedMovies()
            val tvShows = TmdbApi.getTopRatedTv()
            val result = mutableListOf<MediaItem>()
            val maxLen = maxOf(movies.size, tvShows.size)
            for (i in 0 until maxLen) {
                if (i < movies.size) result.add(movies[i])
                if (i < tvShows.size) result.add(tvShows[i])
            }
            result
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getRecommended(context: Context): List<MediaItem> {
        if (!TmdbApi.hasKey()) return emptyList()
        val history = Prefs.getWatchHistory(context)
        if (history.isEmpty()) {
            // Fallback to interleaved top rated
            return getTopRated().take(12)
        }

        val lastFive = history.take(5)
        val list = mutableListOf<MediaItem>()
        val seen = mutableSetOf<Int>()

        for (entry in lastFive) {
            try {
                val recs = if (entry.mediaType == "tv") {
                    TmdbApi.getTvRecommendations(entry.id)
                } else {
                    TmdbApi.getMovieRecommendations(entry.id)
                }
                recs.forEach { item ->
                    if (!seen.contains(item.id)) {
                        list.add(item)
                        seen.add(item.id)
                    }
                }
            } catch (e: Exception) {
                // Ignore errors for individual items
            }
        }

        // If list is small, add top rated as filler
        if (list.size < 8) {
            getTopRated().forEach { item ->
                if (!seen.contains(item.id)) {
                    list.add(item)
                    seen.add(item.id)
                }
            }
        }

        return list.take(20)
    }

    suspend fun getContinueWatching(context: Context): List<HistoryEntry> {
        val history = Prefs.getWatchHistory(context)
        val progressMap = Prefs.getWatchProgress(context)
        val watchedMap = Prefs.getWatched(context)

        return history.filter { entry ->
            val key = if (entry.mediaType == "tv") {
                "tv_${entry.id}_s${entry.season ?: 1}_e${entry.episode ?: 1}"
            } else {
                "movie_${entry.id}"
            }
            val progressVal = progressMap[key] ?: 0f
            val isWatched = watchedMap[key] ?: false
            
            // Progress between 2% and 98% and not fully watched
            progressVal in 0.02f..0.98f && !isWatched
        }
    }

    suspend fun search(query: String): List<MediaItem> {
        if (!TmdbApi.hasKey() || query.isBlank()) return emptyList()
        return TmdbApi.search(query)
    }

    suspend fun getMovieDetail(id: Int): MovieDetail {
        return TmdbApi.getMovieDetail(id)
    }

    suspend fun getTvDetail(id: Int): TvDetail {
        return TmdbApi.getTvDetail(id)
    }

    suspend fun getTvSeasonDetail(showId: Int, season: Int): TvSeason {
        return TmdbApi.getTvSeason(showId, season)
    }

    suspend fun saveProgress(context: Context, key: String, percentage: Float) {
        val progress = Prefs.getWatchProgress(context).toMutableMap()
        progress[key] = percentage
        Prefs.setWatchProgress(context, progress)
    }

    suspend fun markWatched(context: Context, key: String) {
        val watched = Prefs.getWatched(context).toMutableMap()
        watched[key] = true
        Prefs.setWatched(context, watched)
    }

    suspend fun addHistoryEntry(context: Context, entry: HistoryEntry) {
        if (!Prefs.isHistoryEnabled(context)) return
        val history = Prefs.getWatchHistory(context).toMutableList()
        
        // Remove existing item to put new one on top
        history.removeAll { it.id == entry.id && it.mediaType == entry.mediaType && it.season == entry.season && it.episode == entry.episode }
        history.add(0, entry)
        
        Prefs.setWatchHistory(context, history)
    }

    suspend fun toggleSaved(context: Context, item: SavedItem) {
        val saved = Prefs.getSaved(context).toMutableMap()
        val order = Prefs.getSavedOrder(context).toMutableList()
        val key = "${item.mediaType}_${item.id}"

        if (saved.containsKey(key)) {
            saved.remove(key)
            order.remove(key)
        } else {
            saved[key] = item
            order.add(0, key)
        }

        Prefs.setSaved(context, saved)
        Prefs.setSavedOrder(context, order)
    }

    suspend fun isSaved(context: Context, id: Int, mediaType: String): Boolean {
        val saved = Prefs.getSaved(context)
        return saved.containsKey("${mediaType}_$id")
    }
}
