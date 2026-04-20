package dev.lavalink.failover.rest

import dev.lavalink.failover.health.HealthRegistry
import dev.lavalink.failover.health.SourceStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v4/failover")
class FailoverController(private val registry: HealthRegistry) {

    @GetMapping("/health")
    fun health(): ResponseEntity<Map<String, Any>> = ResponseEntity.ok(registry.allHealth())

    @GetMapping("/health/{source}")
    fun sourceHealth(@PathVariable source: String): ResponseEntity<Any> {
        val src = source.lowercase()
        if (src !in registry.knownSources)
            return ResponseEntity.badRequest().body(mapOf("error" to "Unknown source: $source"))
        return ResponseEntity.ok(registry.get(src).toMap())
    }

    @PostMapping("/reset/{source}")
    fun resetSource(@PathVariable source: String): ResponseEntity<Any> {
        val src = source.lowercase()
        if (src !in registry.knownSources)
            return ResponseEntity.badRequest().body(mapOf("error" to "Unknown source: $source"))
        val health = registry.get(src)
        health.totalRequests.set(0)
        health.successCount.set(0)
        health.failureCount.set(0)
        health.consecutiveFailures.set(0)
        health.status = SourceStatus.HEALTHY
        synchronized(health.recentFailures) { health.recentFailures.clear() }
        return ResponseEntity.ok(mapOf("message" to "Reset $src stats successfully"))
    }

    @GetMapping("/summary")
    fun summary(): ResponseEntity<Map<String, Any>> {
        val all = registry.allHealth()
        @Suppress("UNCHECKED_CAST")
        val sources = all["sources"] as List<Map<String, Any>>
        return ResponseEntity.ok(mapOf(
            "overall" to (all["overall"] ?: "UNKNOWN"),
            "healthy" to sources.count { it["status"] == "HEALTHY" },
            "degraded" to sources.count { it["status"] == "DEGRADED" },
            "down" to sources.count { it["status"] == "DOWN" },
            "sources" to sources.map { mapOf("name" to it["name"], "status" to it["status"], "successRate" to it["successRate"]) }
        ))
    }
}
