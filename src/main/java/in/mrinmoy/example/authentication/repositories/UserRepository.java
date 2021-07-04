package in.mrinmoy.example.authentication.repositories;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import in.mrinmoy.example.authentication.model.User;

public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByUsername(String username);
}