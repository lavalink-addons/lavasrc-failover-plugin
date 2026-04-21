package dev.lavalink.failover.listener

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import dev.arbjerg.lavalink.api.AudioPluginInfoModifier
import dev.lavalink.failover.health.HealthRegistry
import org.springframework.stereotype.Component

@Component
class FailoverTrackLoadInterceptor(
    private val registry: HealthRegistry
) : AudioPluginInfoModifier {

    // Lookup direto O(1) em vez de firstOrNull
    private val sourceMap = mapOf(
        "spotify" to "spotify",
        "deezer" to "deezer",
        "applemusic" to "applemusic",
        "yandexmusic" to "yandexmusic",
        "youtube" to "youtube",
        "soundcloud" to "soundcloud",
        "ytdlp" to "ytdlp"
    )

    private fun detectSource(name: String): String {
        val lower = name.lowercase()
        return sourceMap.entries.firstOrNull { lower.contains(it.key) }?.value ?: "unknown"
    }

    fun onLoadSuccess(sourceName: String) = registry.recordSuccess(detectSource(sourceName))

    fun onLoadFailure(sourceName: String, error: Throwable) {
        val msg = error.message ?: "Unknown error"
        val code = when {
            msg.contains("429") || msg.contains("Too many requests", true) -> 429
            msg.contains("403") || msg.contains("Forbidden", true) -> 403
            msg.contains("401") -> 401
            msg.contains("404") -> 404
            else -> 0
        }
        registry.recordFailure(detectSource(sourceName), code, msg)
    }

    override fun modifyAudioTrackPluginInfo(track: AudioTrack): kotlinx.serialization.json.JsonObject? = null
}
