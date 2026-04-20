package dev.lavalink.failover.health

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class HealthRegistry {
    private val log = LoggerFactory.getLogger(HealthRegistry::class.java)
    private val sources = ConcurrentHashMap<String, SourceHealth>()
    val knownSources = listOf("spotify", "deezer", "applemusic", "yandexmusic", "youtube", "soundcloud", "ytdlp")

    init {
        knownSources.forEach { sources[it] = SourceHealth(it) }
    }

    fun get(source: String): SourceHealth = sources.getOrPut(source) { SourceHealth(source) }

    fun recordSuccess(source: String) {
        get(source).recordSuccess()
        log.debug("[Failover] ✅ {} - success recorded", source)
    }

    fun recordFailure(source: String, errorCode: Int, message: String) {
        val health = get(source)
        health.recordFailure(errorCode, message)
        log.warn("[Failover] ❌ {} - failure (code={}, consecutive={}, status={})",
            source, errorCode, health.consecutiveFailures.get(), health.status)
    }

    fun allHealth(): Map<String, Any> {
        val sourceData = sources.values.sortedBy { it.name }.map { it.toMap() }
        val overall = when {
            sourceData.any { it["status"] == "DOWN" } -> "DEGRADED"
            sourceData.any { it["status"] == "DEGRADED" } -> "DEGRADED"
            else -> "HEALTHY"
        }
        return mapOf(
            "overall" to overall,
            "sources" to sourceData,
            "activeSources" to sourceData.count { it["status"] == "HEALTHY" },
            "downSources" to sourceData.count { it["status"] == "DOWN" }
        )
    }
}
