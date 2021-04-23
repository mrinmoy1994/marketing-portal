package in.mrinmoy.example.authentication.repositories;

import in.mrinmoy.example.authentication.model.Token;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface TokenMongoRepository extends MongoRepository<Token, Integer> {
    List<Token> findAllByUserId(String userId);

    Optional<Token> findByToken(String token);
}
