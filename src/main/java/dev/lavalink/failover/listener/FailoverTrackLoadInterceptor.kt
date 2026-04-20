package dev.lavalink.failover.listener

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import dev.arbjerg.lavalink.api.AudioPluginInfoModifier
import dev.lavalink.failover.health.HealthRegistry
import kotlinx.serialization.json.JsonObject
import org.springframework.stereotype.Component

@Component
class FailoverTrackLoadInterceptor(
    private val registry: HealthRegistry
) : AudioPluginInfoModifier {

    private val sourceMap = mapOf(
        "Spotify" to "spotify",
        "Deezer" to "deezer",
        "AppleMusic" to "applemusic",
        "YandexMusic" to "yandexmusic",
        "YouTube" to "youtube",
        "SoundCloud" to "soundcloud",
        "YtDlp" to "ytdlp"
    )

    fun detectSource(name: String): String =
        sourceMap.entries.firstOrNull { name.contains(it.key, ignoreCase = true) }?.value ?: "unknown"

    fun onLoadSuccess(sourceName: String) = registry.recordSuccess(detectSource(sourceName))

    fun onLoadFailure(sourceName: String, error: Throwable) {
        val code = when {
            error.message?.contains("429") == true || error.message?.contains("Too many requests", true) == true -> 429
            error.message?.contains("403") == true || error.message?.contains("Forbidden", true) == true -> 403
            error.message?.contains("401") == true -> 401
            error.message?.contains("404") == true -> 404
            else -> 0
        }
        registry.recordFailure(detectSource(sourceName), code, error.message ?: "Unknown error")
    }

    override fun modifyAudioTrackPluginInfo(track: AudioTrack): kotlinx.serialization.json.JsonObject? = null
}
