package com.example.metasns_api.domain.like;

import com.example.metasns_api.common.exception.LikeException;
import com.example.metasns_api.common.redis.RedisLikeCounter;
import com.example.metasns_api.domain.like.dto.LikeEvent;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class LikeServiceTest {
    @InjectMocks
    private LikeService likeService;

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private RedisLikeCounter redisLikeCounter;

    @Test
    void like_success(){
        Long postId = 1L;
        Long userId = 10L;

        when(likeRepository.existsByPostIdAndUserId(postId, userId))
                .thenReturn(false);

        likeService.like(postId, userId);

        verify(likeRepository).save(any(Like.class));

        ArgumentCaptor<LikeEvent> captor = ArgumentCaptor.forClass(LikeEvent.class);

        verify(eventPublisher).publishEvent(captor.capture());

        LikeEvent event = captor.getValue();
        assertThat(event.postId()).isEqualTo(postId);
        assertThat(event.type()).isEqualTo(LikeEventType.LIKE);
    }

    @Test
    void like_already_liked(){
        Long postId = 1L;
        Long userId = 10L;

        when(likeRepository.existsByPostIdAndUserId(postId, userId))
                .thenReturn(true);

        assertThatThrownBy(()->likeService.like(postId, userId))
                .isInstanceOf(LikeException.class);

        verify(likeRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }
    @Test
    void unlike_success(){
        Long postId = 1L;
        Long userId = 10L;

        Like like = Like.builder()
                .postId(postId)
                .userId(userId)
                .build();

        when(likeRepository.findByPostIdAndUserId(postId,userId))
                .thenReturn(Optional.of(like));

        likeService.unlike(postId, userId);

        verify(likeRepository).delete(like);
        verify(eventPublisher).publishEvent(any(LikeEvent.class));
    }

    @Test
    void unlike_not_liked(){
        when(likeRepository.findByPostIdAndUserId(any(), any()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> likeService.unlike(1L, 10L))
                .isInstanceOf(LikeException.class);

        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void getLikeCount_from_redis(){
        Long postId = 1L;
        String key = "post: "+postId+":like_count";

        when(redisLikeCounter.get(key)).thenReturn(5L);

        long count = likeService.getLikeCount(postId);

        assertThat(count).isEqualTo(5L);
        verify(likeRepository, never()).countByPostId(any());
    }

    @Test
    void getLikeCount_from_db(){
        Long postId = 1L;
        String key = "post: "+postId+":like_count";

        when(redisLikeCounter.get(key)).thenReturn(null);
        when(likeRepository.countByPostId(postId)).thenReturn(7L);

        long count = likeService.getLikeCount(postId);

        assertThat(count).isEqualTo(7L);
        verify(redisLikeCounter).set(key,7L);
    }
}
