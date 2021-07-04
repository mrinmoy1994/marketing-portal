package in.mrinmoy.example.authentication.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import in.mrinmoy.example.authentication.model.ContentRegistry;

public interface ContentRegistryRepository extends MongoRepository<ContentRegistry, String>{
}
