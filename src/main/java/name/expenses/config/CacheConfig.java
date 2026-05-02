package name.expenses.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cache configuration that automatically detects Redis availability
 * and falls back to HashMap-based caching when Redis is not available.
 */
@Slf4j
@Configuration
@EnableCaching
@RequiredArgsConstructor
public class CacheConfig {

    private final CacheProperties cacheProperties;

    @Value("${app.cache.default-ttl:300}")
    private long defaultTtl;

    @Value("${app.cache.entity-ttls.accounts:300}")
    private long accountsTtl;

    @Value("${app.cache.entity-ttls.categories:600}")
    private long categoriesTtl;

    @Value("${app.cache.entity-ttls.budgets:120}")
    private long budgetsTtl;

    /**
     * Primary cache manager that tries Redis first, falls back to HashMap.
     */
    @Bean
    @Primary
    public CacheManager cacheManager() {
        if (isRedisAvailable()) {
            log.info("Redis is available, using Redis cache manager");
            return redisCacheManager();
        } else {
            log.warn("Redis is not available, falling back to HashMap cache manager");
            return hashMapCacheManager();
        }
    }

    /**
     * Redis cache manager configuration.
     */
    @Bean
    @ConditionalOnProperty(name = "spring.data.redis.host")
    public CacheManager redisCacheManager() {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(defaultTtl))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        cacheConfigurations.put("accounts", defaultConfig.entryTtl(Duration.ofSeconds(accountsTtl)));
        cacheConfigurations.put("categories", defaultConfig.entryTtl(Duration.ofSeconds(categoriesTtl)));
        cacheConfigurations.put("budgets", defaultConfig.entryTtl(Duration.ofSeconds(budgetsTtl)));
        cacheConfigurations.put("subCategories", defaultConfig.entryTtl(Duration.ofSeconds(defaultTtl)));
        cacheConfigurations.put("expenses", defaultConfig.entryTtl(Duration.ofSeconds(defaultTtl)));
        cacheConfigurations.put("transactions", defaultConfig.entryTtl(Duration.ofSeconds(defaultTtl)));
        cacheConfigurations.put("budgetTransfers", defaultConfig.entryTtl(Duration.ofSeconds(defaultTtl)));

        RedisConnectionFactory connectionFactory = redisConnectionFactory();
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }

    /**
     * HashMap-based cache manager as fallback.
     */
    @Bean
    public CacheManager hashMapCacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();

        List<ConcurrentMapCache> caches = Arrays.asList(
                new ConcurrentMapCache("accounts"),
                new ConcurrentMapCache("categories"),
                new ConcurrentMapCache("budgets"),
                new ConcurrentMapCache("customers"),
                new ConcurrentMapCache("users"),
                new ConcurrentMapCache("subcategories")
        );

        cacheManager.setCaches(caches);
        return cacheManager;
    }

    /**
     * Redis connection factory.
     */
    @Bean
    @ConditionalOnProperty(name = "spring.data.redis.host")
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(cacheProperties.getHost());
        config.setPort(cacheProperties.getPort());
        if (cacheProperties.getPassword() != null && !cacheProperties.getPassword().isEmpty()) {
            config.setPassword(cacheProperties.getPassword());
        }
        return new LettuceConnectionFactory(config);
    }

    /**
     * Configured Redis template.
     */
    @Bean
    @ConditionalOnProperty(name = "spring.data.redis.host")
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        return createRedisTemplate(connectionFactory);
    }

    /**
     * Check if Redis is available by attempting a connection test.
     */
    private boolean isRedisAvailable() {
        try {
            RedisConnectionFactory factory = redisConnectionFactory();
            factory.getConnection().ping();
            return true;
        } catch (Exception e) {
            log.debug("Redis availability check failed", e);
            return false;
        }
    }

    public RedisTemplate<String, Object> createRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }
}
