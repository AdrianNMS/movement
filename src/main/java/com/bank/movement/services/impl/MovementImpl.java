package com.bank.movement.services.impl;

import com.bank.movement.models.dao.IMovementDao;
import com.bank.movement.models.documents.Movement;
import com.bank.movement.services.IMovementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MovementImpl implements IMovementService
{
    @Autowired
    private IMovementDao dao;

    @Override
    public Mono<List<Movement>> FindAll() {
        return dao.findAll().collectList();
    }

    @Override
    public Mono<List<Movement>> findByIdClient(String idClient) {
        return dao.findAll()
                .filter(movement ->
                        movement.getClientId().equals(idClient)
                ).collectList();
    }

    @Override
    public Mono<Movement> Find(String id) {
        return dao.findById(id);
    }

    @Override
    public Mono<Movement> Create(Movement mov) {
        return dao.save(mov);
    }

    @Override
    public Mono<Movement> Update(String id, Movement mov) {
        return dao.existsById(id).flatMap(check ->
        {
            if (check)
            {
                mov.setDateUpdate(LocalDateTime.now());
                return dao.save(mov);
            }
            else
                return Mono.empty();

        });
    }

    @Override
    public Mono<Object> Delete(String id) {
        return dao.existsById(id).flatMap(check -> {
            if (check)
                return dao.deleteById(id);
            else
                return Mono.empty();
        });
    }

    @Override
    public Mono<Float> GetBalance(String id) {

        return dao.findAll()
                .collectList()
                .flatMap(movements ->
                            Mono.just(movements.stream()
                                    .map(Movement::getCurrentMont)
                                    .reduce(0f, Float::sum))
                );
    }

    @Override
    public Mono<Integer> CountMovementsPerMonth(Movement mov) {
        return findByIdClient(mov.getClientId())
                .flatMap(movements -> {
                    LocalDateTime dateNow = LocalDateTime.now();
                    return Mono.just((int)movements.stream()
                            .filter(movement ->
                                (movement.getCreated().getDayOfMonth() == dateNow.getDayOfMonth()
                                    && movement.getCreated().getYear() == dateNow.getYear()
                                    && movement.getTypePasiveMovement() == mov.getTypePasiveMovement())
                            )
                            .count());
                });
    }

    @Override
    public Mono<Integer> CountMovements(Movement mov) {
        return findByIdClient(mov.getClientId())
                .flatMap(movements ->
                    Mono.just((int)movements.stream()
                            .filter(movement -> movement.getTypePasiveMovement() == mov.getTypePasiveMovement())
                            .count())
                );
    }


}