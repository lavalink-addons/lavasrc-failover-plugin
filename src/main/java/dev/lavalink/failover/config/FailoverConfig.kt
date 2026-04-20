package dev.lavalink.failover.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "plugins.failover")
class FailoverConfig {
    var enabled: Boolean = true
    var logLevel: String = "INFO"
    var failureThreshold: Int = 3
    var endpointEnabled: Boolean = true
    var endpointPath: String = "/v4/failover/health"
}
