package in.mrinmoy.example.authentication.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import in.mrinmoy.example.authentication.model.ContentRecord;

@Repository
public interface ContentRepository extends MongoRepository<ContentRecord, String> {
}
