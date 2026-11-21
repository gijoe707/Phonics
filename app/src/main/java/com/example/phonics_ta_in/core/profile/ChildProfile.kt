package com.example.phonics_ta_in.core.profile

import java.net.URLDecoder
import java.net.URLEncoder
import java.util.UUID

/** One child using the app. Their stars, streak and lesson progress are saved under [id]. */
data class ChildProfile(
    val id: String,
    val name: String,
    val avatar: String
)

/** Pure rules for profiles, so they can be unit tested without Android. */
object ProfileLogic {
    const val MAX_NAME_LENGTH = 20

    /** Pictures a child can pick for their profile. */
    val AVATARS = listOf("🦁", "🐯", "🐼", "🦊", "🐰", "🐘", "🦜", "🐬")

    enum class NameError { EMPTY, TOO_LONG, DUPLICATE }

    /**
     * Null when [raw] is fine as a profile name. Names must be unique (ignoring case);
     * [ignoreId] lets a profile keep its own name while it is being edited.
     */
    fun validateName(raw: String, existing: List<ChildProfile>, ignoreId: String? = null): NameError? {
        val name = raw.trim()
        if (name.isEmpty()) return NameError.EMPTY
        if (name.length > MAX_NAME_LENGTH) return NameError.TOO_LONG
        val taken = existing.any { it.id != ignoreId && it.name.equals(name, ignoreCase = true) }
        return if (taken) NameError.DUPLICATE else null
    }

    fun newProfile(name: String, avatar: String): ChildProfile =
        ChildProfile(id = UUID.randomUUID().toString(), name = name.trim(), avatar = avatar)

    /** At launch the app skips the picker only when there is exactly one profile to pick. */
    fun autoSelectId(profiles: List<ChildProfile>): String? = profiles.singleOrNull()?.id

    // --- Storage format: records split by ",", fields by ":", every field URL-encoded ---

    fun encode(profiles: List<ChildProfile>): String =
        profiles.joinToString(",") { profile ->
            listOf(profile.id, profile.name, profile.avatar).joinToString(":") { encodeField(it) }
        }

    fun decode(text: String): List<ChildProfile> =
        text.split(",").mapNotNull { record ->
            runCatching {
                val fields = record.split(":")
                if (fields.size != 3) return@runCatching null
                val id = decodeField(fields[0])
                val name = decodeField(fields[1])
                val avatar = decodeField(fields[2])
                if (id.isBlank() || name.isBlank()) null else ChildProfile(id, name, avatar)
            }.getOrNull()
        }

    private fun encodeField(value: String): String = URLEncoder.encode(value, "UTF-8")

    private fun decodeField(value: String): String = URLDecoder.decode(value, "UTF-8")
}
