package in.mrinmoy.example.authentication.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import in.mrinmoy.example.authentication.model.Notification;

public interface NotificationRepository extends MongoRepository<Notification, String> {
}