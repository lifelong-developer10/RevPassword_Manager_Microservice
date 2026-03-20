package com.revature.user.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "security_questions")
@Data
public class SecurityQuestions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "answer_hash")
    private String answerHash;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private MasterUser user;

    @ManyToOne
    @JoinColumn(name = "question_id")
    private SecurityQuestionMaster question;
}

