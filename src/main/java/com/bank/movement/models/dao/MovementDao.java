package com.bank.movement.models.dao;

import com.bank.movement.models.documents.Movement;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface MovementDao extends ReactiveMongoRepository<Movement, String>
{
}
