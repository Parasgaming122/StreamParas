package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MediaItem(
    val id: Int,
    val title: String = "",
    val name: String = "",           // Used for TV shows
    @Json(name = "poster_path") val posterPath: String? = null,
    @Json(name = "backdrop_path") val backdropPath: String? = null,
    val overview: String = "",
    @Json(name = "release_date") val releaseDate: String? = null,  // Movies
    @Json(name = "first_air_date") val firstAirDate: String? = null, // TV
    @Json(name = "vote_average") val voteAverage: Float = 0f,
    @Json(name = "genre_ids") val genreIds: List<Int> = emptyList(),
    @Json(name = "original_language") val originalLanguage: String = "",
    @Json(name = "origin_country") val originCountry: List<String> = emptyList(),
    @Json(name = "media_type") val mediaType: String = "movie",  // "movie" or "tv"
    
    // Extended fields (used in history/continue-watching)
    val year: String = "",
    val season: Int? = null,
    val episode: Int? = null,
    val episodeName: String? = null,
    val watchedAt: Long? = null
) {
    val displayTitle: String get() = title.ifEmpty { name }
    val displayYear: String get() = (releaseDate ?: firstAirDate ?: "").take(4)
    val isTv: Boolean get() = mediaType == "tv"
    val isUnreleased: Boolean get() {
        if (releaseDate.isNullOrEmpty()) return false
        return try {
            val now = System.currentTimeMillis()
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            val date = sdf.parse(releaseDate)
            date != null && date.time > now
        } catch (e: Exception) {
            false
        }
    }
}

interface DetailCommon {
    val id: Int
    val displayTitle: String
    val displayYear: String
    val backdropPath: String?
    val posterPath: String?
    val overview: String
    val voteAverage: Float
    val genres: List<Genre>
    val originalLanguage: String
    val originCountry: List<String>
    val mediaType: String
}

@JsonClass(generateAdapter = true)
data class Genre(
    val id: Int,
    val name: String = ""
)

@JsonClass(generateAdapter = true)
data class SpokenLanguage(
    @Json(name = "english_name") val englishName: String = "",
    val name: String = ""
)

@JsonClass(generateAdapter = true)
data class CollectionInfo(
    val id: Int,
    val name: String = "",
    @Json(name = "poster_path") val posterPath: String? = null,
    @Json(name = "backdrop_path") val backdropPath: String? = null
)

@JsonClass(generateAdapter = true)
data class TmdbVideo(
    val id: String,
    val key: String = "",
    val name: String = "",
    val site: String = "",
    val type: String = "",
    val official: Boolean = false
)

@JsonClass(generateAdapter = true)
data class VideoResults(
    val results: List<TmdbVideo> = emptyList()
)

@JsonClass(generateAdapter = true)
data class CastMember(
    val id: Int,
    val name: String = "",
    val character: String = "",
    @Json(name = "profile_path") val profilePath: String? = null
)

@JsonClass(generateAdapter = true)
data class CreditsResponse(
    val cast: List<CastMember> = emptyList()
)

@JsonClass(generateAdapter = true)
data class ReviewItem(
    val id: String,
    val author: String = "",
    val content: String = "",
    @Json(name = "created_at") val createdAt: String? = null
)

@JsonClass(generateAdapter = true)
data class ReviewsResponse(
    val results: List<ReviewItem> = emptyList()
)

@JsonClass(generateAdapter = true)
data class MovieDetail(
    override val id: Int,
    val title: String = "",
    @Json(name = "poster_path") override val posterPath: String? = null,
    @Json(name = "backdrop_path") override val backdropPath: String? = null,
    override val overview: String = "",
    @Json(name = "release_date") val releaseDate: String? = null,
    @Json(name = "vote_average") override val voteAverage: Float = 0f,
    @Json(name = "vote_count") val voteCount: Int = 0,
    val runtime: Int = 0,
    override val genres: List<Genre> = emptyList(),
    @Json(name = "original_language") override val originalLanguage: String = "",
    @Json(name = "origin_country") override val originCountry: List<String> = emptyList(),
    @Json(name = "spoken_languages") val spokenLanguages: List<SpokenLanguage> = emptyList(),
    val tagline: String? = null,
    val status: String? = null,
    val budget: Long = 0,
    val revenue: Long = 0,
    @Json(name = "belongs_to_collection") val belongsToCollection: CollectionInfo? = null,
    val videos: VideoResults? = null,
    val credits: CreditsResponse? = null,
    val reviews: ReviewsResponse? = null
) : DetailCommon {
    override val displayTitle: String get() = title
    override val displayYear: String get() = (releaseDate ?: "").take(4)
    override val mediaType: String get() = "movie"
}

@JsonClass(generateAdapter = true)
data class CreatedBy(
    val id: Int,
    val name: String = "",
    @Json(name = "profile_path") val profilePath: String? = null
)

@JsonClass(generateAdapter = true)
data class TvEpisodeInfo(
    val id: Int,
    val name: String = "",
    val overview: String = "",
    @Json(name = "episode_number") val episodeNumber: Int = 0,
    @Json(name = "season_number") val seasonNumber: Int = 0,
    @Json(name = "air_date") val airDate: String? = null
)

@JsonClass(generateAdapter = true)
data class TvDetail(
    override val id: Int,
    val name: String = "",
    @Json(name = "poster_path") override val posterPath: String? = null,
    @Json(name = "backdrop_path") override val backdropPath: String? = null,
    override val overview: String = "",
    @Json(name = "first_air_date") val firstAirDate: String? = null,
    @Json(name = "last_air_date") val lastAirDate: String? = null,
    @Json(name = "vote_average") override val voteAverage: Float = 0f,
    @Json(name = "number_of_seasons") val numberOfSeasons: Int = 0,
    @Json(name = "number_of_episodes") val numberOfEpisodes: Int = 0,
    val seasons: List<TvSeason> = emptyList(),
    @Json(name = "last_episode_to_air") val lastEpisodeToAir: TvEpisodeInfo? = null,
    @Json(name = "next_episode_to_air") val nextEpisodeToAir: TvEpisodeInfo? = null,
    @Json(name = "created_by") val createdBy: List<CreatedBy> = emptyList(),
    override val genres: List<Genre> = emptyList(),
    @Json(name = "original_language") override val originalLanguage: String = "",
    @Json(name = "origin_country") override val originCountry: List<String> = emptyList(),
    val videos: VideoResults? = null,
    val credits: CreditsResponse? = null,
    val reviews: ReviewsResponse? = null
) : DetailCommon {
    override val displayTitle: String get() = name
    override val displayYear: String get() = (firstAirDate ?: "").take(4)
    override val mediaType: String get() = "tv"
}

@JsonClass(generateAdapter = true)
data class TvSeason(
    val id: Int,
    @Json(name = "season_number") val seasonNumber: Int = 0,
    val name: String = "",
    val episodes: List<TvEpisode> = emptyList(),
    @Json(name = "poster_path") val posterPath: String? = null,
    @Json(name = "episode_count") val episodeCount: Int = 0
)

@JsonClass(generateAdapter = true)
data class TvEpisode(
    val id: Int,
    @Json(name = "season_number") val seasonNumber: Int = 0,
    @Json(name = "episode_number") val episodeNumber: Int = 0,
    val name: String = "",
    val overview: String = "",
    @Json(name = "air_date") val airDate: String? = null,
    @Json(name = "still_path") val stillPath: String? = null,
    val runtime: Int = 0,
    @Json(name = "show_id") val showId: Int = 0
)

@JsonClass(generateAdapter = true)
data class TmdbResponse<T>(
    val page: Int = 1,
    val results: List<T> = emptyList(),
    @Json(name = "total_pages") val totalPages: Int = 1,
    @Json(name = "total_results") val totalResults: Int = 0
)

@JsonClass(generateAdapter = true)
data class HistoryEntry(
    val id: Int,
    val title: String = "",
    val posterPath: String? = null,
    val mediaType: String = "movie",
    val season: Int? = null,
    val episode: Int? = null,
    val episodeName: String? = null,
    val watchedAt: Long? = null,
    val progress: Float = 0f  // Progress percentage (0.0 to 1.0)
)

@JsonClass(generateAdapter = true)
data class SavedItem(
    val id: Int,
    val title: String = "",
    val posterPath: String? = null,
    val mediaType: String = "movie",
    val voteAverage: Float = 0f,
    val year: String = ""
)

@JsonClass(generateAdapter = true)
data class AniListMedia(
    val id: Int,
    val idMal: Int? = null,
    val title: AniListTitle? = null,
    val description: String? = null,
    val coverImage: AniListCoverImage? = null,
    val bannerImage: String? = null,
    val genres: List<String> = emptyList(),
    val averageScore: Int = 0,
    val episodes: Int = 0,
    val status: String? = null,
    val season: String? = null,
    val seasonYear: Int? = null,
    val relations: AniListRelations? = null
)

@JsonClass(generateAdapter = true)
data class AniListTitle(
    val romaji: String? = null,
    val english: String? = null,
    val native: String? = null
)

@JsonClass(generateAdapter = true)
data class AniListCoverImage(
    val extraLarge: String? = null,
    val large: String? = null,
    val medium: String? = null
)

@JsonClass(generateAdapter = true)
data class AniListRelations(
    val edges: List<AniListRelationEdge> = emptyList()
)

@JsonClass(generateAdapter = true)
data class AniListRelationEdge(
    val relationType: String = "",
    val node: AniListMedia? = null
)

@JsonClass(generateAdapter = true)
data class PlayerSource(
    val id: String,
    val label: String,
    val supportsProgress: Boolean = true,
    val isAsync: Boolean = false,
    val colorParam: String? = null,
    val langParam: String? = null,
    val extraParams: Map<String, String> = emptyMap()
)

@JsonClass(generateAdapter = true)
data class AniListDetail(
    override val id: Int,
    val title: String,
    override val posterPath: String?,
    override val backdropPath: String?,
    override val overview: String,
    val year: String,
    override val voteAverage: Float,
    override val genres: List<Genre> = emptyList(),
    override val originalLanguage: String = "ja",
    override val originCountry: List<String> = listOf("JP"),
    override val mediaType: String = "anilist"
) : DetailCommon {
    override val displayTitle: String get() = title
    override val displayYear: String get() = year
}
