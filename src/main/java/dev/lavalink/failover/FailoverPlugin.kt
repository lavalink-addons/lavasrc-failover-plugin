package dev.lavalink.failover

import dev.lavalink.failover.config.FailoverConfig
import dev.lavalink.failover.health.HealthRegistry
import dev.lavalink.failover.listener.FailoverTrackLoadInterceptor
import dev.lavalink.failover.rest.FailoverController
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(FailoverConfig::class)
class FailoverPlugin {
    private val log = LoggerFactory.getLogger(FailoverPlugin::class.java)

    @Bean
    fun healthRegistry(): HealthRegistry {
        log.info("[Failover] 🚀 LavaSrc Failover Plugin loaded!")
        return HealthRegistry()
    }

    @Bean
    fun failoverInterceptor(registry: HealthRegistry) = FailoverTrackLoadInterceptor(registry)

    @Bean
    fun failoverController(registry: HealthRegistry): FailoverController {
        log.info("[Failover] 📡 REST available at /v4/failover/health")
        return FailoverController(registry)
    }
}
