package in.mrinmoy.example.authentication.repositories;

import in.mrinmoy.example.authentication.model.KYCDetails;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KycMongoRepository extends MongoRepository<KYCDetails, Long> {
    Optional<KYCDetails> findByUserId(String userId);
}
