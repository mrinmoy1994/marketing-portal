package in.mrinmoy.example.authentication.repositories;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import in.mrinmoy.example.authentication.model.KYCDetails;

@Repository
public interface KycRepository extends MongoRepository<KYCDetails, String> {
    Optional<KYCDetails> findByUserId(String userId);
}
