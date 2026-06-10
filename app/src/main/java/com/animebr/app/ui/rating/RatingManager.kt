package com.animebr.app.ui.rating

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RatingManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("rating_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_SHOW_COUNT = "rating_show_count"
        private const val KEY_RATED = "has_rated"
        private const val DEFAULT_MAX_SHOW_COUNT = 1
    }

    var maxShowCount: Int = DEFAULT_MAX_SHOW_COUNT

    private var currentShowCount: Int
        get() = prefs.getInt(KEY_SHOW_COUNT, 0)
        set(value) = prefs.edit().putInt(KEY_SHOW_COUNT, value).apply()

    private var hasRated: Boolean
        get() = prefs.getBoolean(KEY_RATED, false)
        set(value) = prefs.edit().putBoolean(KEY_RATED, value).apply()

    fun shouldShowRating(): Boolean {
        if (hasRated) return false
        return currentShowCount < maxShowCount
    }

    fun onRatingShown() {
        currentShowCount = currentShowCount + 1
    }

    fun onRatingSubmitted() {
        hasRated = true
    }

    fun updateFromRemoteConfig(rateTime: Int) {
        maxShowCount = rateTime
    }
}
