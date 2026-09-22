package com.tokenmall.common.redis;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;


public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory factory) {

        RedisTemplate<String, Object> template = new RedisTemplate<>();

        template.setConnectionFactory(factory);

        // Key
        StringRedisSerializer stringSerializer =
                new StringRedisSerializer();

        // ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();

        // 支持 LocalDateTime、LocalDate 等
        objectMapper.registerModule(new JavaTimeModule());

        // 时间使用 ISO-8601 字符串
        objectMapper.disable(
                SerializationFeature.WRITE_DATES_AS_TIMESTAMPS
        );

        // 激活类型信息（写入 @class 字段），反序列化时才能还原成具体对象
        // 使用 EVERYTHING：与 Spring Data Redis 官方默认一致，
        // 避免不可变集合（如 Stream.toList() 的结果）、record 等类型丢失类型信息导致读取失败
        objectMapper.activateDefaultTyping(
                objectMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.EVERYTHING,
                JsonTypeInfo.As.PROPERTY
        );

        // JSON 序列化器
        GenericJackson2JsonRedisSerializer jsonSerializer =
                new GenericJackson2JsonRedisSerializer(objectMapper);

        // Key
        template.setKeySerializer(stringSerializer);

        // Value
        template.setValueSerializer(jsonSerializer);

        // Hash Key
        template.setHashKeySerializer(stringSerializer);

        // Hash Value
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();

        return template;
    }
}