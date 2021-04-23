package in.mrinmoy.example.authentication.repositories;

import in.mrinmoy.example.authentication.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserMongoRepository extends MongoRepository<User, String> {

    Optional<User> findByUsername(String username);
    // public List<User> findByLastName(String lastName);
}