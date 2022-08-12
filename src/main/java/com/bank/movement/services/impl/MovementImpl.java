package com.bank.movement.services.impl;

import com.bank.movement.models.dao.MovementDao;
import com.bank.movement.models.documents.Movement;
import com.bank.movement.models.emus.TypeMovement;
import com.bank.movement.services.MovementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MovementImpl implements MovementService
{
    @Autowired
    private MovementDao dao;

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
                .flatMap(movements -> {
                    float balance = 0.f;

                    for(Movement movement : movements)
                    {
                        if(movement.getClientId().equals(id))
                            if(movement.getTypeMovement().equals(TypeMovement.DEPOSITS))
                            {
                                balance = balance - movement.getComissionMont() + movement.getMont();
                            }
                            else if(movement.getTypeMovement().equals(TypeMovement.WITHDRAWALS))
                            {
                                balance = balance - movement.getComissionMont()  - movement.getMont();
                            }
                    }

                    return Mono.just(balance);

                });
    }

    @Override
    public Mono<Integer> CountMovementsPerMonth(Movement mov) {
        return findByIdClient(mov.getClientId())
                .flatMap(movements -> {
                    int count = 0;

                    LocalDateTime dateNow = LocalDateTime.now();

                    for (Movement movement:movements)
                    {
                        LocalDateTime dateCreated = movement.getCreated();

                        if (dateCreated.getDayOfMonth() == dateNow.getDayOfMonth()
                                && dateCreated.getYear() == dateNow.getYear()
                                && movement.getTypePasiveMovement() == mov.getTypePasiveMovement())
                        {
                            count++;
                        }
                    }

                    return Mono.just(count);
                });
    }

    @Override
    public Mono<Integer> CountMovements(Movement mov) {
        return findByIdClient(mov.getClientId())
                .flatMap(movements -> {
                    int count = 0;

                    LocalDateTime dateNow = LocalDateTime.now();

                    for (Movement movement:movements)
                    {
                        LocalDateTime dateCreated = movement.getCreated();

                        if (movement.getTypePasiveMovement() == mov.getTypePasiveMovement())
                        {
                            count++;
                        }
                    }

                    return Mono.just(count);
                });
    }


}
