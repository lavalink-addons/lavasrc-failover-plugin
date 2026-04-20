package dev.lavalink.failover.health

import java.time.Instant
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

enum class SourceStatus { HEALTHY, DEGRADED, DOWN }

data class FailureRecord(
    val timestamp: Instant,
    val errorCode: Int,
    val message: String
)

class SourceHealth(val name: String) {
    val totalRequests = AtomicLong(0)
    val successCount = AtomicLong(0)
    val failureCount = AtomicLong(0)
    val consecutiveFailures = AtomicInteger(0)
    val recentFailures = ArrayDeque<FailureRecord>(50)

    @Volatile var status: SourceStatus = SourceStatus.HEALTHY
    @Volatile var lastSuccess: Instant? = null
    @Volatile var lastFailure: Instant? = null

    fun recordSuccess() {
        totalRequests.incrementAndGet()
        successCount.incrementAndGet()
        consecutiveFailures.set(0)
        lastSuccess = Instant.now()
        status = SourceStatus.HEALTHY
    }

    fun recordFailure(errorCode: Int, message: String) {
        totalRequests.incrementAndGet()
        failureCount.incrementAndGet()
        val consecutive = consecutiveFailures.incrementAndGet()
        lastFailure = Instant.now()
        synchronized(recentFailures) {
            if (recentFailures.size >= 50) recentFailures.removeFirst()
            recentFailures.addLast(FailureRecord(Instant.now(), errorCode, message))
        }
        status = when {
            consecutive >= 5 -> SourceStatus.DOWN
            consecutive >= 2 -> SourceStatus.DEGRADED
            else -> SourceStatus.HEALTHY
        }
    }

    fun successRate(): Double {
        val total = totalRequests.get()
        return if (total == 0L) 100.0 else (successCount.get().toDouble() / total) * 100.0
    }

    fun toMap(): Map<String, Any?> = mapOf(
        "name" to name,
        "status" to status.name,
        "totalRequests" to totalRequests.get(),
        "successCount" to successCount.get(),
        "failureCount" to failureCount.get(),
        "consecutiveFailures" to consecutiveFailures.get(),
        "successRate" to String.format("%.1f%%", successRate()),
        "lastSuccess" to lastSuccess?.toString(),
        "lastFailure" to lastFailure?.toString(),
        "recentFailures" to synchronized(recentFailures) {
            recentFailures.takeLast(10).map {
                mapOf("timestamp" to it.timestamp.toString(), "errorCode" to it.errorCode, "message" to it.message)
            }
        }
    )
}
