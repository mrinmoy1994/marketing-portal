package in.mrinmoy.example.authentication.repositories;

import in.mrinmoy.example.authentication.model.Image;
import in.mrinmoy.example.authentication.model.KYCDetails;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ImageRepository extends MongoRepository<Image, String> {
}
