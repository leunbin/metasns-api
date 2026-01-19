package com.example.metasns_api.domain.user;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.cglib.core.Local;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue
    private Long id;

    @Column
    private String email;

    @Column
    private String password;

    @Enumerated(EnumType.STRING)
    @Column
    private UserStatus status;

    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime deletedAt;

    @Builder
    public User(String email, String encodedPassword){
        this.email = email;
        this.password = encodedPassword;
        this.status = UserStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
    }

    public void deleteUser() {
        this.deletedAt = LocalDateTime.now();
        this.status = UserStatus.DELETED;
    }
}
