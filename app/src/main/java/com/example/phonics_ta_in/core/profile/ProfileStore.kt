package com.example.phonics_ta_in.core.profile

import android.content.Context
import com.example.phonics_ta_in.core.progress.ProgressStore

/**
 * Saves the list of child profiles and the settings that belong to the whole device
 * (not to one child), such as the parent's background-music switch.
 *
 * Each profile's stars, streak and lesson progress live in their own [ProgressStore] file.
 */
class ProfileStore(context: Context) {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun loadProfiles(): List<ChildProfile> =
        ProfileLogic.decode(prefs.getString(KEY_PROFILES, "") ?: "")

    fun createProfile(name: String, avatar: String): ChildProfile {
        val profile = ProfileLogic.newProfile(name, avatar)
        val existing = loadProfiles()
        // Progress saved before profiles existed goes to the very first profile, once.
        if (existing.isEmpty() && !prefs.getBoolean(KEY_LEGACY_ADOPTED, false)) {
            ProgressStore.copyLegacyInto(appContext, profile.id)
        }
        prefs.edit()
            .putString(KEY_PROFILES, ProfileLogic.encode(existing + profile))
            .putBoolean(KEY_LEGACY_ADOPTED, true)
            .apply()
        return profile
    }

    fun updateProfile(id: String, name: String, avatar: String) {
        val updated = loadProfiles().map {
            if (it.id == id) it.copy(name = name.trim(), avatar = avatar) else it
        }
        prefs.edit().putString(KEY_PROFILES, ProfileLogic.encode(updated)).apply()
    }

    /** Removes the profile and erases its saved progress. */
    fun deleteProfile(id: String) {
        val remaining = loadProfiles().filterNot { it.id == id }
        prefs.edit().putString(KEY_PROFILES, ProfileLogic.encode(remaining)).apply()
        ProgressStore.delete(appContext, id)
    }

    /** Parent setting, shared by every profile. Falls back to the pre-profiles setting on first run. */
    fun loadMusicEnabled(): Boolean =
        if (prefs.contains(KEY_MUSIC_ENABLED)) prefs.getBoolean(KEY_MUSIC_ENABLED, true)
        else ProgressStore.legacyMusicEnabled(appContext)

    fun saveMusicEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_MUSIC_ENABLED, enabled).apply()
    }

    private companion object {
        const val PREFS_NAME = "phonics_profiles"
        const val KEY_PROFILES = "profiles"
        const val KEY_LEGACY_ADOPTED = "legacy_progress_adopted"
        const val KEY_MUSIC_ENABLED = "music_enabled"
    }
}
