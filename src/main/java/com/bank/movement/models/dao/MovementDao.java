package com.bank.movement.models.dao;

import com.bank.movement.models.documents.Movement;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface MovementDao extends ReactiveMongoRepository<Movement, String> {
}
