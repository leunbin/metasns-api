package com.example.metasns_api.domain.like;

import com.example.metasns_api.common.exception.LikeException;
import com.example.metasns_api.common.redis.RedisLikeCounter;
import com.example.metasns_api.domain.like.dto.LikeEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class LikeService {

    private final LikeRepository likeRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final RedisLikeCounter redisLikeCounter;

    //좋아요 추가
    public void like(Long postId, Long userId){
        if(likeRepository.existsByPostIdAndUserId(postId, userId)){
            throw new LikeException(
                    HttpStatus.BAD_REQUEST,
                    "이미 좋아요를 누른 상태입니다."
            );
        }

        Like like = Like.builder()
                .postId(postId)
                .userId(userId)
                .build();

        likeRepository.save(like);

        //비동기 레디스 카운터 증가
        eventPublisher.publishEvent(
                new LikeEvent(postId, LikeEventType.LIKE)
        );
    }

    //좋아요 취소
    public void unlike(Long postId, Long userId){

        Like like = likeRepository.findByPostIdAndUserId(postId,userId)
                .orElseThrow(()->
                        new LikeException(
                                HttpStatus.BAD_REQUEST,
                                "좋아요 상태가 아닙니다."
                        )
                );

        likeRepository.delete(like);

        eventPublisher.publishEvent(
                new LikeEvent(postId, LikeEventType.UNLIKE)
        );
    }

    //좋아요 수 조회
    public long getLikeCount(Long postId){
        String key = "post:"+postId+":like_count";

        Long count = redisLikeCounter.get(key);

        if(count != null){
            return count;
        }

        long dbCount = likeRepository.countByPostId(postId);

        redisLikeCounter.set(key, dbCount);

        return dbCount;
    }
}
