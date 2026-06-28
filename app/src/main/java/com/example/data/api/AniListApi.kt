package com.example.data.api

import com.example.data.model.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap

object AniListApi {
    private val client = OkHttpClient()
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val mediaAdapter = moshi.adapter(AniListMedia::class.java)

    private data class CacheEntry(val data: AniListMedia, val timestamp: Long)
    private val cache = ConcurrentHashMap<String, CacheEntry>()
    private const val CACHE_TTL = 7 * 24 * 60 * 60 * 1000L // 7 days

    suspend fun fetchAnilistData(title: String, mediaType: String): AniListMedia? {
        if (mediaType != "tv" && mediaType != "movie") return null
        
        val cacheKey = title.lowercase().trim()
        val now = System.currentTimeMillis()
        val cached = cache[cacheKey]
        if (cached != null && now - cached.timestamp < CACHE_TTL) {
            return cached.data
        }

        return withContext(Dispatchers.IO) {
            try {
                val query = """
                    query (${'$'}search: String) {
                      Page(page: 1, perPage: 1) {
                        media(search: ${'$'}search, type: ANIME) {
                          id
                          idMal
                          title {
                            romaji
                            english
                            native
                          }
                          description
                          coverImage {
                            extraLarge
                            large
                            medium
                          }
                          bannerImage
                          genres
                          averageScore
                          episodes
                          status
                          season
                          seasonYear
                          relations {
                            edges {
                              relationType
                              node {
                                id
                                idMal
                                title {
                                  romaji
                                  english
                                }
                                coverImage {
                                  medium
                                }
                                seasonYear
                              }
                            }
                          }
                        }
                      }
                    }
                """.trimIndent()

                val variables = JSONObject().put("search", title)
                val jsonBody = JSONObject()
                    .put("query", query)
                    .put("variables", variables)

                val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url("https://graphql.anilist.co")
                    .post(requestBody)
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@withContext null
                    val jsonStr = response.body?.string() ?: return@withContext null
                    
                    val root = JSONObject(jsonStr)
                    val dataObj = root.optJSONObject("data") ?: return@withContext null
                    val pageObj = dataObj.optJSONObject("Page") ?: return@withContext null
                    val mediaArray = pageObj.optJSONArray("media") ?: return@withContext null
                    
                    if (mediaArray.length() == 0) return@withContext null
                    val firstMedia = mediaArray.getJSONObject(0).toString()
                    
                    val media = mediaAdapter.fromJson(firstMedia)
                    if (media != null) {
                        cache[cacheKey] = CacheEntry(media, now)
                    }
                    media
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    fun cleanDescription(desc: String?): String {
        if (desc.isNullOrEmpty()) return ""
        var cleaned = desc
            .replace("<br>", "\n")
            .replace("<br/>", "\n")
            .replace("<br />", "\n")
            .replace("<i>", "")
            .replace("</i>", "")
            .replace("<b>", "")
            .replace("</b>", "")
            .replace(Regex("<[^>]*>"), "") // strip remaining html
        
        // Remove trailing source notes
        val sourceIndex = cleaned.indexOf("(Source:")
        if (sourceIndex != -1) {
            cleaned = cleaned.substring(0, sourceIndex)
        }
        val noteIndex = cleaned.indexOf("Note:")
        if (noteIndex != -1) {
            cleaned = cleaned.substring(0, noteIndex)
        }
        return cleaned.trim()
    }

    // Helper to build a sequel chain sorted chronologically
    fun buildAnilistSeasons(mainMedia: AniListMedia?): List<SavedItem> {
        if (mainMedia == null) return emptyList()
        val list = mutableListOf<AniListMedia>()
        list.add(mainMedia)

        mainMedia.relations?.edges?.forEach { edge ->
            if (edge.relationType == "SEQUEL" || edge.relationType == "PREQUEL") {
                edge.node?.let { list.add(it) }
            }
        }

        // Sort by year
        return list.sortedBy { it.seasonYear ?: 9999 }.map { media ->
            SavedItem(
                id = media.id,
                title = media.title?.english ?: media.title?.romaji ?: "Anime",
                posterPath = media.coverImage?.large ?: media.coverImage?.medium,
                mediaType = "tv",
                voteAverage = (media.averageScore / 10f),
                year = media.seasonYear?.toString() ?: ""
            )
        }
    }
}
