package dev.lavalink.failover.health

import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

enum class SourceStatus { HEALTHY, DEGRADED, DOWN }

data class FailureRecord(
    val timestampMs: Long,
    val errorCode: Int,
    val message: String
)

class SourceHealth(val name: String) {
    val totalRequests = AtomicLong(0)
    val successCount = AtomicLong(0)
    val failureCount = AtomicLong(0)
    val consecutiveFailures = AtomicInteger(0)

    internal val recentFailures = ConcurrentLinkedDeque<FailureRecord>()

    @Volatile var status: SourceStatus = SourceStatus.HEALTHY
    @Volatile var lastSuccessMs: Long = 0L
    @Volatile var lastFailureMs: Long = 0L

    fun recordSuccess() {
        totalRequests.incrementAndGet()
        successCount.incrementAndGet()
        consecutiveFailures.set(0)
        lastSuccessMs = System.currentTimeMillis()
        status = SourceStatus.HEALTHY
    }

    fun recordFailure(errorCode: Int, message: String) {
        totalRequests.incrementAndGet()
        failureCount.incrementAndGet()
        val consecutive = consecutiveFailures.incrementAndGet()
        lastFailureMs = System.currentTimeMillis()

        recentFailures.addLast(FailureRecord(lastFailureMs, errorCode, message))
        if (recentFailures.size > 10) recentFailures.pollFirst()

        status = when {
            consecutive >= 5 -> SourceStatus.DOWN
            consecutive >= 2 -> SourceStatus.DEGRADED
            else -> SourceStatus.HEALTHY
        }
    }

    fun successRate(): Double {
        val total = totalRequests.get()
        return if (total == 0L) 100.0 else successCount.get().toDouble() / total * 100.0
    }

    fun toMap(): Map<String, Any?> = mapOf(
        "name" to name,
        "status" to status.name,
        "totalRequests" to totalRequests.get(),
        "successCount" to successCount.get(),
        "failureCount" to failureCount.get(),
        "consecutiveFailures" to consecutiveFailures.get(),
        "successRate" to "%.1f%%".format(successRate()),
        "lastSuccess" to if (lastSuccessMs > 0) lastSuccessMs else null,
        "lastFailure" to if (lastFailureMs > 0) lastFailureMs else null,
        "recentFailures" to recentFailures.map {
            mapOf("timestampMs" to it.timestampMs, "errorCode" to it.errorCode, "message" to it.message)
        }
    )
}
