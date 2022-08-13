package com.bank.movement.controllers;

import com.bank.movement.handler.ResponseHandler;
import com.bank.movement.models.emus.TypeMovement;
import com.bank.movement.models.utils.MovementConditions;
import com.bank.movement.services.IMovementService;
import com.bank.movement.services.IParameterService;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

public class MovementRestControllerCreateHelper
{
    public static Mono<ResponseEntity<Object>> SaveMovement(MovementConditions movCon, IMovementService movementService, Logger log, IParameterService parameterService)
    {
        return movementService.Create(movCon.getMov())
                .map(movement -> ResponseHandler.response("Done", HttpStatus.OK, movement)                )
                .onErrorResume(error -> Mono.just(ResponseHandler.response(error.getMessage(), HttpStatus.BAD_REQUEST, null)))
                .doFinally(fin -> log.info("[END] create Movement"));
    }

    public static Mono<ResponseEntity<Object>> UpdateMontReceiver(MovementConditions movCon, IMovementService movementService, Logger log, IParameterService parameterService)
    {
        return parameterService.setMont(movCon.getMov().getPasiveReceiverId(),movCon.getMontReceiver())
                .flatMap(responseMont1 -> {
                    log.info(responseMont1.toString());
                    if(responseMont1.getStatus().equalsIgnoreCase("Ok"))
                        return SaveMovement(movCon, movementService,log,parameterService);
                    else
                        return Mono.just(ResponseHandler.response("Error", HttpStatus.BAD_REQUEST, null));
                }).switchIfEmpty(Mono.just(ResponseHandler.response("Empty", HttpStatus.NO_CONTENT, null)));
    }

    public static Mono<ResponseEntity<Object>> UpdateMont(MovementConditions movCon, IMovementService movementService, Logger log, IParameterService parameterService)
    {
        return parameterService.setMont(movCon.getMov().getPasiveId(),movCon.getMont())
                .flatMap(responseMont1 -> {
                    log.info(responseMont1.toString());
                    if(responseMont1.getStatus().equalsIgnoreCase("Ok"))
                        if(movCon.getMov().getTypeMovement() == TypeMovement.DEPOSITS)
                            return UpdateMontReceiver(movCon, movementService,log,parameterService);
                        else
                            return SaveMovement(movCon, movementService,log,parameterService);
                    else
                        return Mono.just(ResponseHandler.response("Error", HttpStatus.BAD_REQUEST, null));
                }).switchIfEmpty(Mono.just(ResponseHandler.response("Empty", HttpStatus.NO_CONTENT, null)));
    }

    public static Mono<ResponseEntity<Object>> ObtainMovementPerMonth(MovementConditions movCon, IMovementService movementService, Logger log, IParameterService parameterService)
    {
        //Obtener movimientos maximos del mes
        return movementService.CountMovementsPerMonth(movCon.getMov()).flatMap(currentMovement -> {
            //Valida condicionales
            if(movCon.CheckContinueTransaction())
                return UpdateMont(movCon, movementService,log,parameterService);
            else
                return Mono.just(ResponseHandler.response("You can't generate more movement today", HttpStatus.BAD_REQUEST, null));

        }).switchIfEmpty(Mono.just(ResponseHandler.response("Empty", HttpStatus.NO_CONTENT, null)));
    }

    public static Mono<ResponseEntity<Object>> PasiveMovement(MovementConditions movCon, IMovementService movementService, Logger log, IParameterService parameterService)
    {
        //movimientos del pasivo
        return movementService.CountMovements(movCon.getMov()).flatMap(currentMov ->
        {
            movCon.setMovementPerAccount(currentMov);
            movCon.init();

            if(movCon.HaveEnoughCredit())
            {
                log.info("Pasive's client have credit");

                return ObtainMovementPerMonth(movCon, movementService,log,parameterService);
            }
            else
                return Mono.just(ResponseHandler.response("You don't have enough credit", HttpStatus.BAD_REQUEST, null));

        }).switchIfEmpty(Mono.just(ResponseHandler.response("Empty", HttpStatus.NO_CONTENT, null)));
    }

    public static Mono<ResponseEntity<Object>> ObtainListParameters(MovementConditions movCon, IMovementService movementService, Logger log, IParameterService parameterService)
    {
        return parameterService.getTypeParams(movCon.getMov().getPasiveId())
                .flatMap(parameters -> {
                    log.info(parameters.toString());
                    //Condicionales
                    movCon.setParameters(parameters.getData());

                    return PasiveMovement(movCon, movementService,log,parameterService);
                })
                .switchIfEmpty(Mono.just(ResponseHandler.response("Empty", HttpStatus.NO_CONTENT, null)));
    }

    public static Mono<ResponseEntity<Object>> ObtainMont(MovementConditions movCon, IMovementService movementService, Logger log, IParameterService parameterService)
    {
        //Obtener monto de pasivo
        return  parameterService.getMont(movCon.getMov().getPasiveId())
                .flatMap(responseMont ->
                {
                    if(responseMont.getData() != null)
                    {
                        log.info(responseMont.getData().toString());
                        //Guarda monto de pasivo
                        movCon.setCurrentPasiveMont(responseMont.getData().getMont());
                        return ObtainListParameters(movCon, movementService,log,parameterService);
                    }
                    else
                        return Mono.just(ResponseHandler.response("Not found", HttpStatus.BAD_REQUEST, null));

                })
                .switchIfEmpty(Mono.just(ResponseHandler.response("Empty", HttpStatus.NO_CONTENT, null)));
    }

    public static Mono<ResponseEntity<Object>> CreateMovementSequence(MovementConditions movCon, IMovementService movementService, org.slf4j.Logger log, IParameterService parameterService)
    {
        return ObtainMont(movCon, movementService,log,parameterService);
    }
}