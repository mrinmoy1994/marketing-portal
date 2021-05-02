package in.mrinmoy.example.authentication.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import in.mrinmoy.example.authentication.model.Image;

public interface ImageRepository extends MongoRepository<Image, String> {
}
