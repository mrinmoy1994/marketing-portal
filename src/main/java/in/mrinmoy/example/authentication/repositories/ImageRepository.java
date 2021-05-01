package in.mrinmoy.example.authentication.repositories;

import in.mrinmoy.example.authentication.model.Image;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ImageRepository extends MongoRepository<Image, String> {
}
