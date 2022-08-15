package com.bank.movement.services;

import com.bank.movement.models.documents.ComissionReport;
import com.bank.movement.models.documents.Movement;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.List;

public interface IMovementService
{
    Mono<List<Movement>> FindAll();
    Mono<List<Movement>> FindByIdPasive(String id);
    Mono<List<Movement>> FindByIdPasivePerMonth(String id);
    Mono<Movement> Find(String id);
    Mono<Movement> Create(Movement mov);
    Mono<Movement> Update(String id, Movement mov);
    Mono<Object> Delete(String id);
    Mono<Float> GetBalance(String id);
    Mono<Integer> CountMovementsPerMonth(Movement mov);
    Mono<Integer> CountMovements(Movement mov);

    Mono<List<ComissionReport>> ComissionReportBetween(String min, String max);



}
