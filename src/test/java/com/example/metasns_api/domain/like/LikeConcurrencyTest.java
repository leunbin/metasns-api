package com.example.metasns_api.domain.like;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("concurrency")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@TestPropertySource(locations = "classpath:application-concurrency.yml")
class LikeConcurrencyTest {

    @Autowired
    LikeService likeService;

    @Autowired
    LikeRepository likeRepository;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    Long postId;
    Long userId;

    @BeforeEach
    void setUp() {
        postId = 1L;
        userId = 1L;

        //혹시 모를 잔여 데이터 삭제하기
        likeRepository.deleteByPostIdAndUserId(postId, userId);
    }

    @AfterEach
    void tearDown(){
        likeRepository.deleteByPostIdAndUserId(postId, userId);
    }

    @Test
    //동일 유저가 동일 게시물에 동시에 좋아요 요청
    //Like row는 반드시 1개
    void concurrent_like_should_create_only_one_row() throws Exception {
        //스레드 수 & 동시에 100개 요청을 보낸다는 의미
        int threadCount = 100;
        //동시에 실행 가능한 실제 스레드 수 10개
        ExecutorService executorService = Executors.newFixedThreadPool(100);

        //모든 스레드가 대기 & countDown 호출 시 동시에 출발
        CountDownLatch startLatch = new CountDownLatch(1);
        //모든 작업이 끝날때까지 메인 스레드 대기
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        //100개 작업을 executorService에 등록
        for(int i = 0; i < threadCount; i++){
            //각각의 Runnable 하나 생성 -> 이 Runnable들이 경쟁
            executorService.submit(()->{
                try{
                    //모든 스레드 대기
                    startLatch.await();

                    //동시에 실행될 코드
                    likeService.like(postId, userId);
                } catch (Exception e){
                    //unique 제약 예외는 정상적인 결과일 수 있음
                } finally {
                    endLatch.countDown();
                }
            });
        }

        //모든 스레드 동시 출발
        startLatch.countDown();

        //전부 끝날 때까지 대기
        endLatch.await();

        long likeCount = likeRepository.countByPostIdAndUserId(postId, userId);
        assertThat(likeCount).isEqualTo(1);
    }

    @Test
    //동시 좋아요 요청 시 레디스와 db row 수 일치
    void like_count_should_be_consistent_between_redis_and_db() throws Exception{
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(100);

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        String redisKey = "post:"+postId+":like_count";

        stringRedisTemplate.delete(redisKey);

        for(int i = 0; i<threadCount; i++){
            final Long currentUserId = (long) (i+1);

            executorService.submit(()->{
                try{
                    startLatch.await();
                    likeService.like(postId,currentUserId);
                } catch (Exception e){

                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        endLatch.await();

        long dbCount = likeRepository.countByPostId(postId);

        String redisValue = stringRedisTemplate.opsForValue().get(redisKey);
        long redisCount = redisValue == null ? 0 : Long.parseLong(redisValue);

        assertThat(redisCount).isEqualTo(dbCount);

    }

    @Test
    //like & unlike 동시 요청 시 상태는 일관되게 유지된다
    void like_and_unlike_concurrently_should_result_in_consistent_state() throws Exception{

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(2);

        String redisKey = "post:"+postId+":like_count";

        likeRepository.deleteByPostIdAndUserId(postId, userId);
        stringRedisTemplate.delete(redisKey);

        //like request
        executorService.submit(() -> {
            try{
                startLatch.await();
                likeService.like(postId, userId);
            } catch (Exception e){

            } finally {
                endLatch.countDown();
            }
        });

        //unlike request
        executorService.submit(()->{
            try{
                startLatch.await();
                likeService.unlike(postId, userId);
            } catch (Exception e){

            } finally {
                endLatch.countDown();
            }
        });

        startLatch.countDown();
        endLatch.await();

        long dbCount = likeRepository.countByPostIdAndUserId(postId, userId);

        String redisValue = stringRedisTemplate.opsForValue().get(redisKey);
        long redisCount = redisValue == null ? 0 : Long.parseLong(redisValue);

        assertThat(dbCount).isEqualTo(redisCount);
        assertThat(dbCount).isBetween(0L, 1L);
    }

    @Test
    //동시 좋아요 요청 시 Unique Key 충돌이 발생해도 시스템은 정상
    void unique_key_violation_should_not_break_system() throws Exception {

        int threadCount = 50;
        ExecutorService executorService = Executors.newFixedThreadPool(50);

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        for (int i = 0; i<threadCount; i++){
            executorService.submit(()->{
                try{
                    startLatch.await();
                    likeService.like(postId, userId);
                } catch (Exception e){

                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        endLatch.await();

        long count = likeRepository.countByPostIdAndUserId(postId, userId);
        assertThat(count).isEqualTo(1);
    }

    @Test
    //deadlock 발생 가능 상황에서도 재시도 로직이 정상 동작
    void retry_logic_should_handle_deadlock_safely() throws Exception{
        int threadCount = 20;
        int repeatCount = 10;

        for(int i = 0; i<repeatCount; i++){
            ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch endLatch = new CountDownLatch(threadCount);

            for(int j = 0; j<threadCount; j++){
                executorService.submit(()->{
                    try{
                        startLatch.await();
                        likeService.like(postId,userId);
                    } catch (Exception e){

                    } finally {
                        endLatch.countDown();
                    }
                });
            }

            startLatch.countDown();
            endLatch.await();

            long count = likeRepository.countByPostIdAndUserId(postId,userId);
            assertThat(count).isEqualTo(1);

            likeRepository.deleteByPostIdAndUserId(postId, userId);
        }
    }
}
