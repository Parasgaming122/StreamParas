package com.example.data.api

object AgeRating {
    // Maps certification strings to minimum ages per country
    private val usRatings = mapOf(
        "G" to 0, "PG" to 8, "PG-13" to 13, "R" to 17, "NC-17" to 18,
        "TV-Y" to 0, "TV-Y7" to 7, "TV-G" to 0, "TV-PG" to 10, "TV-14" to 14, "TV-MA" to 17
    )

    private val deRatings = mapOf(
        "0" to 0, "6" to 6, "12" to 12, "16" to 16, "18" to 18,
        "FSK 0" to 0, "FSK 6" to 6, "FSK 12" to 12, "FSK 16" to 16, "FSK 18" to 18
    )

    private val gbRatings = mapOf(
        "U" to 0, "PG" to 8, "12A" to 12, "12" to 12, "15" to 15, "18" to 18
    )

    private val frRatings = mapOf(
        "U" to 0, "10" to 10, "12" to 12, "16" to 16, "18" to 18
    )

    private val auRatings = mapOf(
        "G" to 0, "PG" to 8, "M" to 12, "MA15+" to 15, "R18+" to 18
    )

    private val nzRatings = mapOf(
        "G" to 0, "PG" to 8, "M" to 12, "R13" to 13, "R15" to 15, "R16" to 16, "R18" to 18
    )

    private val brRatings = mapOf(
        "L" to 0, "10" to 10, "12" to 12, "14" to 14, "16" to 16, "18" to 18
    )

    private val caRatings = mapOf(
        "G" to 0, "PG" to 8, "14A" to 14, "18A" to 18, "A" to 18
    )

    private val jpRatings = mapOf(
        "G" to 0, "PG12" to 12, "R15+" to 15, "R18+" to 18
    )

    private val countryMaps = mapOf(
        "US" to usRatings,
        "DE" to deRatings,
        "GB" to gbRatings,
        "FR" to frRatings,
        "AU" to auRatings,
        "NZ" to nzRatings,
        "BR" to brRatings,
        "CA" to caRatings,
        "JP" to jpRatings
    )

    fun getMinAge(country: String, certification: String): Int {
        val cleanCert = certification.uppercase().trim()
        val ratings = countryMaps[country.uppercase()] ?: usRatings
        return ratings[cleanCert] ?: try {
            // Fallback: try parsing direct digits (e.g. "12", "16", "18")
            val digits = cleanCert.filter { it.isDigit() }
            if (digits.isNotEmpty()) digits.toInt() else 0
        } catch (e: Exception) {
            0
        }
    }

    fun isRestricted(contentMinAge: Int, ageLimitSetting: String): Boolean {
        if (ageLimitSetting.isEmpty()) return false
        val limit = ageLimitSetting.toIntOrNull() ?: return false
        return contentMinAge > limit
    }
}
