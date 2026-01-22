package com.example.metasns_api.domain.like;

import com.example.metasns_api.common.redis.RedisLikeCounter;
import com.example.metasns_api.domain.like.dto.LikeEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class LikeEventListener {

    private final RedisLikeCounter redisLikeCounter;

    @Async
    @EventListener
    public void handle(LikeEvent event){
        String key = "post:"+event.postId()+":like_count";

        if(event.type() == LikeEventType.LIKE){
            redisLikeCounter.increment(key);
        } else {
            redisLikeCounter.decrement(key);
        }
    }
}
