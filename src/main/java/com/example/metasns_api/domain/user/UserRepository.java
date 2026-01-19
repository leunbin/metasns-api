package com.example.metasns_api.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    //이메일로 유저 찾기
    Optional<User> findByEmail(String email);

    //이메일 중복 여부
    boolean existsByEmail(String email);
}
