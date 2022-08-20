package com.bank.movement.services.impl;

import com.bank.movement.models.dao.IMovementDao;
import com.bank.movement.models.documents.ComissionReport;
import com.bank.movement.models.documents.Movement;
import com.bank.movement.models.utils.Utils;
import com.bank.movement.services.IMovementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
    public Mono<List<Movement>> FindByIdPasive(String id) {
        return dao.findAll()
                .filter(movement ->
                        (movement.getPasiveId().equals(id) || movement.getPasiveReceiverId().equals(id))
                ).collectList();
    }

    @Override
    public Mono<List<Movement>> FindByIdPasivePerMonth(String id) {
        LocalDateTime dateNow = LocalDateTime.now();
        return dao.findAll()
                .filter(movement ->
                        movement.getCreated().getMonthValue() == dateNow.getMonthValue()
                                && movement.getCreated().getYear() == dateNow.getYear() &&
                        (movement.getPasiveId().equals(id) || movement.getPasiveReceiverId().equals(id))
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
                return dao.deleteById(id).then(Mono.just(true));
            else
                return Mono.empty();
        });
    }

    @Override
    public Mono<Float> GetBalance(String id) {

        return FindByIdPasivePerMonth(id)
                .flatMap(movements ->
                            Mono.just((float)movements.stream()
                                    .mapToDouble(Movement::getMont)
                                    .average().getAsDouble())
                );
    }



    @Override
    public Mono<Integer> CountMovementsPerMonth(Movement mov) {
        return FindByIdPasive(mov.getPasiveId())
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
        return FindByIdPasive(mov.getPasiveId())
                .flatMap(movements ->
                    Mono.just((int)movements.stream()
                            .filter(movement -> movement.getTypePasiveMovement() == mov.getTypePasiveMovement())
                            .count())
                );
    }

    @Override
    public Mono<List<ComissionReport>> ComissionReportBetween(String min, String max)
    {
        LocalDateTime localDateTimeMin = Utils.parseLocalDate(min);

        LocalDateTime localDateTimeMax = Utils.parseLocalDate(max);

        return FindAll().flatMap(movements ->
                Mono.just(movements.stream().filter(movement ->
                        Utils.BetweenDates(movement.getCreated(), localDateTimeMin, localDateTimeMax)
                ).map(movement -> ComissionReport.
                        builder()
                        .idEmisor(movement.getPasiveId())
                        .idReceptor(movement.getPasiveReceiverId())
                        .Mont(movement.getMont())
                        .MontComission(movement.getComissionMont()+movement.getComissionMaxMont())
                        .build()
                ).collect(Collectors.toList()))
        );
    }


}
