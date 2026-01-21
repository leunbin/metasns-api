package com.example.metasns_api.common.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisLikeCounter {

    private final RedisTemplate<String, Long> longRedisTemplate;

    public void set(String key, Long count){
        longRedisTemplate.opsForValue().set(key, count);
    }

    public Long get(String key){
        return longRedisTemplate.opsForValue().get(key);
    }

    public void increment(String key){
        longRedisTemplate.opsForValue().increment(key);
    }

    public void decrement(String key){
        longRedisTemplate.opsForValue().decrement(key);
    }
}
