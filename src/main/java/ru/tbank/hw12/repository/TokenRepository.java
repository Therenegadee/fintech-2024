package ru.tbank.hw12.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.tbank.hw12.entity.Token;

@Repository
public interface TokenRepository extends JpaRepository<Token, String> {
}
