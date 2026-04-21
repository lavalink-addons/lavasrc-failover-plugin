package dev.lavalink.failover.health

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class HealthRegistry {
    private val log = LoggerFactory.getLogger(HealthRegistry::class.java)
    private val sources = ConcurrentHashMap<String, SourceHealth>(8)
    val knownSources = listOf("spotify", "deezer", "applemusic", "yandexmusic", "youtube", "soundcloud", "ytdlp")

    init {
        knownSources.forEach { sources[it] = SourceHealth(it) }
    }

    fun get(source: String): SourceHealth = sources.getOrPut(source) { SourceHealth(source) }

    fun recordSuccess(source: String) {
        get(source).recordSuccess()
        log.debug("[Failover] {} success", source)
    }

    fun recordFailure(source: String, errorCode: Int, message: String) {
        val health = get(source)
        health.recordFailure(errorCode, message)
        if (log.isWarnEnabled) {
            log.warn("[Failover] {} failure code={} consecutive={} status={}", source, errorCode, health.consecutiveFailures.get(), health.status)
        }
    }

    fun allHealth(): Map<String, Any> {
        val sourceData = sources.values.sortedBy { it.name }
        var hasDown = false
        var hasDegraded = false
        var healthyCount = 0
        var downCount = 0
        var degradedCount = 0

        val mapped = sourceData.map { s ->
            when (s.status) {
                SourceStatus.DOWN -> { hasDown = true; downCount++ }
                SourceStatus.DEGRADED -> { hasDegraded = true; degradedCount++ }
                SourceStatus.HEALTHY -> healthyCount++
            }
            s.toMap()
        }

        val overall = if (hasDown || hasDegraded) "DEGRADED" else "HEALTHY"
        return mapOf(
            "overall" to overall,
            "sources" to mapped,
            "activeSources" to healthyCount,
            "downSources" to downCount
        )
    }
}
