package io.busata.fourleftdiscord;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CachingConfig {
    @Bean
    public CacheManager cacheManager() {
        final var cacheManager = new CaffeineCacheManager("field_mappings", "channel_configurations", "view_results","discord-user-guilds", "easportsconfigs");
        cacheManager.setCaffeine(caffeineCacheBuilder());
        return cacheManager;
    }

    private Caffeine< Object, Object > caffeineCacheBuilder() {
        return Caffeine.newBuilder()
                .initialCapacity(100)
                .expireAfterAccess(1, TimeUnit.MINUTES);
    }
}
