package name.expenses.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for caching system.
 */
@Data
@Component
@ConfigurationProperties(prefix = "spring.data.redis")
public class CacheProperties {
    /**
     * Redis host.
     */
    private String host;

    /**
     * Redis port.
     */
    private int port;

    /**
     * Redis password (optional).
     */
    private String password;

    /**
     * Connection timeout in milliseconds.
     */
    private int timeout;
    /**
     * Default TTL for cache entries in seconds.
     */
    private int defaultTtl = 300;
}
