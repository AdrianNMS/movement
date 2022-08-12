package com.bank.movement.services;

import com.bank.movement.models.documents.Movement;
import reactor.core.publisher.Mono;

import java.util.List;

public interface MovementService
{
    Mono<List<Movement>> FindAll();
    Mono<List<Movement>> findByIdClient(String idClient);
    Mono<Movement> Find(String id);
    Mono<Movement> Create(Movement mov);
    Mono<Movement> Update(String id, Movement mov);
    Mono<Object> Delete(String id);
    Mono<Float> GetBalance(String id);
    Mono<Integer> CountMovementsPerMonth(Movement mov);
    Mono<Integer> CountMovements(Movement mov);



}
