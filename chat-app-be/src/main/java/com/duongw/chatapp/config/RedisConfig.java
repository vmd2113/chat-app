package com.duongw.chatapp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisConfig {

    @Value("${REDIS_HOST:localhost}")
    private String redisHost;


    @Value("${REDIS_PORT:6379}")
    private String redisPort;

    @Value("${REDIS_PASSWORD}")
    private String redisPassword;



    @Bean
    public JedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(redisHost, Integer.parseInt(redisPort));
        if (redisPassword != null) {
            config.setPassword(redisPassword);
        }
        return new JedisConnectionFactory(config);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory){
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisCacheManager cacheManager (RedisConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // Cache người dùng - hết hạn sau 1 giờ
        cacheConfigurations.put("users", defaultConfig.entryTtl(Duration.ofHours(1)));

        // Cache settings - hết hạn sau 2 giờ
        cacheConfigurations.put("userSettings", defaultConfig.entryTtl(Duration.ofHours(2)));

        // Cache roles và permissions - hết hạn sau 12 giờ
        cacheConfigurations.put("roles", defaultConfig.entryTtl(Duration.ofHours(12)));
        cacheConfigurations.put("permissions", defaultConfig.entryTtl(Duration.ofHours(12)));

        // Cache conversations - hết hạn sau 15 phút
        cacheConfigurations.put("conversations", defaultConfig.entryTtl(Duration.ofMinutes(15)));

        // Cache friends - hết hạn sau 30 phút
        cacheConfigurations.put("friends", defaultConfig.entryTtl(Duration.ofMinutes(30)));

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();


    }
}
