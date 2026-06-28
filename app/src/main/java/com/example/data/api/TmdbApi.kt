package com.example.data.api

import com.example.data.model.*
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import java.util.concurrent.ConcurrentHashMap

@JsonClass(generateAdapter = true)
data class MovieReleaseDatesResponse(val results: List<MovieReleaseResult> = emptyList())

@JsonClass(generateAdapter = true)
data class MovieReleaseResult(
    @retrofit2.http.Header("iso_3166_1") val iso_3166_1: String = "",
    val release_dates: List<MovieReleaseDateInfo> = emptyList()
)

@JsonClass(generateAdapter = true)
data class MovieReleaseDateInfo(val certification: String = "", val type: Int = 0)

@JsonClass(generateAdapter = true)
data class TvContentRatingsResponse(val results: List<TvContentRatingResult> = emptyList())

@JsonClass(generateAdapter = true)
data class TvContentRatingResult(
    @retrofit2.http.Header("iso_3166_1") val iso_3166_1: String = "",
    val rating: String = ""
)

interface TmdbService {
    @GET("trending/movie/week")
    suspend fun getTrendingMovies(
        @Header("Authorization") authHeader: String,
        @Query("language") lang: String
    ): TmdbResponse<MediaItem>

    @GET("trending/tv/week")
    suspend fun getTrendingTv(
        @Header("Authorization") authHeader: String,
        @Query("language") lang: String
    ): TmdbResponse<MediaItem>

    @GET("search/multi")
    suspend fun search(
        @Header("Authorization") authHeader: String,
        @Query("query") query: String,
        @Query("language") lang: String
    ): TmdbResponse<MediaItem>

    @GET("movie/{id}")
    suspend fun getMovieDetail(
        @Header("Authorization") authHeader: String,
        @Path("id") id: Int,
        @Query("language") lang: String,
        @Query("append_to_response") appendToResponse: String = "videos,credits,reviews"
    ): MovieDetail

    @GET("tv/{id}")
    suspend fun getTvDetail(
        @Header("Authorization") authHeader: String,
        @Path("id") id: Int,
        @Query("language") lang: String,
        @Query("append_to_response") appendToResponse: String = "videos,credits,reviews"
    ): TvDetail

    @GET("tv/{id}/season/{season}")
    suspend fun getTvSeason(
        @Header("Authorization") authHeader: String,
        @Path("id") id: Int,
        @Path("season") season: Int,
        @Query("language") lang: String
    ): TvSeason

    @GET("movie/{id}/release_dates")
    suspend fun getMovieReleaseDates(
        @Header("Authorization") authHeader: String,
        @Path("id") id: Int
    ): MovieReleaseDatesResponse

    @GET("tv/{id}/content_ratings")
    suspend fun getTvContentRatings(
        @Header("Authorization") authHeader: String,
        @Path("id") id: Int
    ): TvContentRatingsResponse

    @GET("movie/{id}/recommendations")
    suspend fun getMovieRecommendations(
        @Header("Authorization") authHeader: String,
        @Path("id") id: Int,
        @Query("language") lang: String
    ): TmdbResponse<MediaItem>

    @GET("tv/{id}/recommendations")
    suspend fun getTvRecommendations(
        @Header("Authorization") authHeader: String,
        @Path("id") id: Int,
        @Query("language") lang: String
    ): TmdbResponse<MediaItem>

    @GET("movie/{id}/similar")
    suspend fun getMovieSimilar(
        @Header("Authorization") authHeader: String,
        @Path("id") id: Int,
        @Query("language") lang: String
    ): TmdbResponse<MediaItem>

    @GET("tv/{id}/similar")
    suspend fun getTvSimilar(
        @Header("Authorization") authHeader: String,
        @Path("id") id: Int,
        @Query("language") lang: String
    ): TmdbResponse<MediaItem>

    @GET("movie/top_rated")
    suspend fun getTopRatedMovies(
        @Header("Authorization") authHeader: String,
        @Query("language") lang: String
    ): TmdbResponse<MediaItem>

    @GET("tv/top_rated")
    suspend fun getTopRatedTv(
        @Header("Authorization") authHeader: String,
        @Query("language") lang: String
    ): TmdbResponse<MediaItem>

    @GET("discover/movie")
    suspend fun discoverMovies(
        @Header("Authorization") authHeader: String,
        @Query("language") lang: String,
        @Query("with_genres") withGenres: String? = null,
        @Query("with_original_language") withOriginalLanguage: String? = null,
        @Query("region") region: String? = null,
        @Query("sort_by") sortBy: String = "popularity.desc"
    ): TmdbResponse<MediaItem>

    @GET("discover/tv")
    suspend fun discoverTv(
        @Header("Authorization") authHeader: String,
        @Query("language") lang: String,
        @Query("with_genres") withGenres: String? = null,
        @Query("with_original_language") withOriginalLanguage: String? = null,
        @Query("sort_by") sortBy: String = "popularity.desc"
    ): TmdbResponse<MediaItem>
}

object TmdbApi {
    private var currentApiKey: String = ""
    private var currentLang: String = "en-US"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.themoviedb.org/3/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val service = retrofit.create(TmdbService::class.java)

    // In-memory cache implementation
    private data class CacheEntry<T>(val data: T, val timestamp: Long)
    private val cache = ConcurrentHashMap<String, CacheEntry<Any>>()
    private const val CACHE_TTL = 5 * 60 * 1000L // 5 minutes
    private const val MAX_CACHE_SIZE = 80

    // Concurrency control: max 4 requests at a time
    private val semaphore = Semaphore(4)

    fun configure(apiKey: String, lang: String) {
        currentApiKey = apiKey
        currentLang = lang.ifEmpty { "en-US" }
        // Clear cache on reconfiguration
        cache.clear()
    }

    private fun getAuthHeader(): String {
        // Stored token is expected to be a Bearer Token (TMDB Read Access Token)
        return if (currentApiKey.startsWith("Bearer ", ignoreCase = true)) {
            currentApiKey
        } else {
            "Bearer $currentApiKey"
        }
    }

    fun hasKey(): Boolean {
        return currentApiKey.isNotBlank()
    }

    fun imgUrl(path: String?, size: String = "w500"): String? {
        if (path.isNullOrEmpty()) return null
        if (path.startsWith("http://") || path.startsWith("https://")) {
            return path
        }
        return "https://image.tmdb.org/t/p/$size$path"
    }

    private suspend fun <T> withThrottleAndCache(cacheKey: String, call: suspend () -> T): T {
        // Check cache
        val cached = cache[cacheKey]
        val now = System.currentTimeMillis()
        if (cached != null && now - cached.timestamp < CACHE_TTL) {
            @Suppress("UNCHECKED_CAST")
            return cached.data as T
        }

        // Throttle request
        val result = semaphore.withPermit {
            call()
        }

        // Evict if cache exceeds max size
        if (cache.size >= MAX_CACHE_SIZE) {
            val oldest = cache.minByOrNull { it.value.timestamp }
            if (oldest != null) {
                cache.remove(oldest.key)
            }
        }

        // Cache result
        if (result != null) {
            cache[cacheKey] = CacheEntry(result as Any, now)
        }

        return result
    }

    suspend fun getTrendingMovies(): List<MediaItem> {
        val cacheKey = "trending_movies_$currentLang"
        return withThrottleAndCache(cacheKey) {
            service.getTrendingMovies(getAuthHeader(), currentLang).results.map {
                it.copy(mediaType = "movie")
            }
        }
    }

    suspend fun getTrendingTv(): List<MediaItem> {
        val cacheKey = "trending_tv_$currentLang"
        return withThrottleAndCache(cacheKey) {
            service.getTrendingTv(getAuthHeader(), currentLang).results.map {
                it.copy(mediaType = "tv")
            }
        }
    }

    suspend fun search(query: String): List<MediaItem> {
        val cacheKey = "search_${query}_$currentLang"
        return withThrottleAndCache(cacheKey) {
            service.search(getAuthHeader(), query, currentLang).results
                .filter { it.mediaType == "movie" || it.mediaType == "tv" }
                .take(12)
        }
    }

    suspend fun getMovieDetail(id: Int): MovieDetail {
        val cacheKey = "movie_detail_${id}_$currentLang"
        return withThrottleAndCache(cacheKey) {
            service.getMovieDetail(getAuthHeader(), id, currentLang)
        }
    }

    suspend fun getTvDetail(id: Int): TvDetail {
        val cacheKey = "tv_detail_${id}_$currentLang"
        return withThrottleAndCache(cacheKey) {
            service.getTvDetail(getAuthHeader(), id, currentLang)
        }
    }

    suspend fun getTvSeason(showId: Int, season: Int): TvSeason {
        val cacheKey = "tv_season_${showId}_${season}_$currentLang"
        return withThrottleAndCache(cacheKey) {
            service.getTvSeason(getAuthHeader(), showId, season, currentLang)
        }
    }

    suspend fun getMovieReleaseDates(id: Int): MovieReleaseDatesResponse {
        val cacheKey = "movie_release_dates_$id"
        return withThrottleAndCache(cacheKey) {
            service.getMovieReleaseDates(getAuthHeader(), id)
        }
    }

    suspend fun getTvContentRatings(id: Int): TvContentRatingsResponse {
        val cacheKey = "tv_content_ratings_$id"
        return withThrottleAndCache(cacheKey) {
            service.getTvContentRatings(getAuthHeader(), id)
        }
    }

    suspend fun getMovieRecommendations(id: Int): List<MediaItem> {
        val cacheKey = "movie_recommendations_${id}_$currentLang"
        return withThrottleAndCache(cacheKey) {
            service.getMovieRecommendations(getAuthHeader(), id, currentLang).results.map {
                it.copy(mediaType = "movie")
            }
        }
    }

    suspend fun getTvRecommendations(id: Int): List<MediaItem> {
        val cacheKey = "tv_recommendations_${id}_$currentLang"
        return withThrottleAndCache(cacheKey) {
            service.getTvRecommendations(getAuthHeader(), id, currentLang).results.map {
                it.copy(mediaType = "tv")
            }
        }
    }

    suspend fun getMovieSimilar(id: Int): List<MediaItem> {
        val cacheKey = "movie_similar_${id}_$currentLang"
        return withThrottleAndCache(cacheKey) {
            service.getMovieSimilar(getAuthHeader(), id, currentLang).results.map {
                it.copy(mediaType = "movie")
            }
        }
    }

    suspend fun getTvSimilar(id: Int): List<MediaItem> {
        val cacheKey = "tv_similar_${id}_$currentLang"
        return withThrottleAndCache(cacheKey) {
            service.getTvSimilar(getAuthHeader(), id, currentLang).results.map {
                it.copy(mediaType = "tv")
            }
        }
    }

    suspend fun getTopRatedMovies(): List<MediaItem> {
        val cacheKey = "top_rated_movies_$currentLang"
        return withThrottleAndCache(cacheKey) {
            service.getTopRatedMovies(getAuthHeader(), currentLang).results.map {
                it.copy(mediaType = "movie")
            }.take(8)
        }
    }

    suspend fun getTopRatedTv(): List<MediaItem> {
        val cacheKey = "top_rated_tv_$currentLang"
        return withThrottleAndCache(cacheKey) {
            service.getTopRatedTv(getAuthHeader(), currentLang).results.map {
                it.copy(mediaType = "tv")
            }.take(8)
        }
    }

    suspend fun discoverAnime(): List<MediaItem> {
        val cacheKey = "discover_anime_$currentLang"
        return withThrottleAndCache(cacheKey) {
            val tvList = try {
                service.discoverTv(getAuthHeader(), currentLang, withGenres = "16", withOriginalLanguage = "ja").results.map {
                    it.copy(mediaType = "tv")
                }
            } catch (e: Exception) {
                emptyList()
            }
            val movieList = try {
                service.discoverMovies(getAuthHeader(), currentLang, withGenres = "16", withOriginalLanguage = "ja").results.map {
                    it.copy(mediaType = "movie")
                }
            } catch (e: Exception) {
                emptyList()
            }
            (tvList + movieList).shuffled().take(12)
        }
    }

    suspend fun discoverPunjabiMovies(): List<MediaItem> {
        val cacheKey = "discover_punjabi_$currentLang"
        return withThrottleAndCache(cacheKey) {
            try {
                service.discoverMovies(getAuthHeader(), currentLang, withOriginalLanguage = "pa").results.map {
                    it.copy(mediaType = "movie")
                }.take(12)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    suspend fun discoverIndianMovies(): List<MediaItem> {
        val cacheKey = "discover_indian_$currentLang"
        return withThrottleAndCache(cacheKey) {
            try {
                service.discoverMovies(getAuthHeader(), currentLang, withOriginalLanguage = "hi", region = "IN").results.map {
                    it.copy(mediaType = "movie")
                }.take(12)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}
