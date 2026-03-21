package com.revature.notification.models;

import jakarta.persistence.Column;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
        import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String message;

    private String type;

    private boolean readStatus;

    @Column(name = "created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}