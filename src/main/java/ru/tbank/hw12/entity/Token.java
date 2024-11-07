package ru.tbank.hw12.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "users", name = "token")
public class Token {
    @Id
    private String token;
    @Column(name = "black_listed")
    private boolean isBlackListed;
}
