package com.codewithsai.alert.notification.repository;

import com.codewithsai.alert.notification.model.entity.AlertRaisedEntity;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;


public interface AlertRepository extends ReactiveMongoRepository<AlertRaisedEntity, String> {
}

