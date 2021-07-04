package in.mrinmoy.example.authentication.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import in.mrinmoy.example.authentication.model.Token;

public interface TokenRepository extends MongoRepository<Token, Integer> {
    List<Token> findAllByUserId(String userId);

    Optional<Token> findByToken(String token);
}
