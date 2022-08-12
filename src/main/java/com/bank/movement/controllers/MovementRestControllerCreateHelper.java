package com.bank.movement.controllers;

import com.bank.movement.handler.ResponseHandler;
import com.bank.movement.models.documents.Movement;
import com.bank.movement.models.utils.MovementConditions;
import com.bank.movement.services.IMovementService;
import com.bank.movement.services.IParameterService;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

public class MovementRestControllerCreateHelper
{
    public static Mono<ResponseEntity<Object>> SaveMovement(MovementConditions movCon, Movement mov, IMovementService movementService, Logger log, IParameterService parameterService)
    {
        return movementService.Create(mov)
                .map(movement -> ResponseHandler.response("Done", HttpStatus.OK, movement)                )
                .onErrorResume(error -> Mono.just(ResponseHandler.response(error.getMessage(), HttpStatus.BAD_REQUEST, null)))
                .doFinally(fin -> log.info("[END] create Movement"));
    }

    public static Mono<ResponseEntity<Object>> UpdateMontReceiver(MovementConditions movCon, Movement mov, IMovementService movementService, Logger log, IParameterService parameterService)
    {
        return parameterService.setMont(mov.getPasiveReceiverId(),movCon.getMont())
                .flatMap(responseMont1 -> {
                    log.info(responseMont1.toString());
                    if(responseMont1.getStatus().equalsIgnoreCase("Ok"))
                        return SaveMovement(movCon,mov, movementService,log,parameterService);
                    else
                        return Mono.just(ResponseHandler.response("Error", HttpStatus.BAD_REQUEST, null));
                }).switchIfEmpty(Mono.just(ResponseHandler.response("Empty", HttpStatus.NO_CONTENT, null)));
    }

    public static Mono<ResponseEntity<Object>> UpdateMont(MovementConditions movCon, Movement mov, IMovementService movementService, Logger log, IParameterService parameterService)
    {
        return parameterService.setMont(mov.getPasiveId(),movCon.getMont())
                .flatMap(responseMont1 -> {
                    log.info(responseMont1.toString());
                    if(responseMont1.getStatus().equalsIgnoreCase("Ok"))
                        return UpdateMontReceiver(movCon,mov, movementService,log,parameterService);
                    else
                        return Mono.just(ResponseHandler.response("Error", HttpStatus.BAD_REQUEST, null));
                }).switchIfEmpty(Mono.just(ResponseHandler.response("Empty", HttpStatus.NO_CONTENT, null)));
    }

    public static Mono<ResponseEntity<Object>> ObtainMovementPerMonth(MovementConditions movCon, Movement mov, IMovementService movementService, Logger log, IParameterService parameterService)
    {
        //Obtener movimientos maximos del mes
        return movementService.CountMovementsPerMonth(mov).flatMap(currentMovement -> {
            //Valida condicionales
            if(movCon.CheckContinueTransaction())
                return UpdateMont(movCon,mov, movementService,log,parameterService);
            else
                return Mono.just(ResponseHandler.response("You can't generate more movement today", HttpStatus.BAD_REQUEST, null));

        }).switchIfEmpty(Mono.just(ResponseHandler.response("Empty", HttpStatus.NO_CONTENT, null)));
    }

    public static Mono<ResponseEntity<Object>> PasiveMovement(MovementConditions movCon, Movement mov, IMovementService movementService, Logger log, IParameterService parameterService)
    {
        //movimientos del pasivo
        return movementService.CountMovements(mov).flatMap(currentMov ->
        {
            movCon.setMovementPerAccount(currentMov);
            if(movCon.HaveEnoughCredit())
            {
                log.info("Pasive's client have credit");
                movCon.init();
                return ObtainMovementPerMonth(movCon,mov, movementService,log,parameterService);
            }
            else
                return Mono.just(ResponseHandler.response("You don't have enough credit", HttpStatus.BAD_REQUEST, null));

        }).switchIfEmpty(Mono.just(ResponseHandler.response("Empty", HttpStatus.NO_CONTENT, null)));
    }

    public static Mono<ResponseEntity<Object>> ObtainListParameters(MovementConditions movCon, Movement mov, IMovementService movementService, Logger log, IParameterService parameterService)
    {
        return parameterService.getTypeParams(mov.getPasiveId())
                .flatMap(parameters -> {
                    log.info(parameters.toString());
                    //Condicionales
                    movCon.setParameters(parameters.getData());
                    movCon.setMov(mov);

                    return PasiveMovement(movCon,mov, movementService,log,parameterService);
                })
                .switchIfEmpty(Mono.just(ResponseHandler.response("Empty", HttpStatus.NO_CONTENT, null)));
    }

    public static Mono<ResponseEntity<Object>> ObtainMont(MovementConditions movCon, Movement mov, IMovementService movementService, Logger log, IParameterService parameterService)
    {
        //Obtener monto de pasivo
        return  parameterService.getMont(mov.getPasiveId())
                .flatMap(responseMont ->
                {
                    if(responseMont.getData() != null)
                    {
                        log.info(responseMont.getData().toString());
                        //Guarda monto de pasivo
                        movCon.setCurrentPasiveMont(responseMont.getData().getMont());
                        return ObtainListParameters(movCon,mov, movementService,log,parameterService);
                    }
                    else
                        return Mono.just(ResponseHandler.response("Not found", HttpStatus.BAD_REQUEST, null));

                })
                .switchIfEmpty(Mono.just(ResponseHandler.response("Empty", HttpStatus.NO_CONTENT, null)));
    }

    public static Mono<ResponseEntity<Object>> CreateMovementSequence(MovementConditions movCon, Movement mov, IMovementService movementService, org.slf4j.Logger log, IParameterService parameterService)
    {
        return ObtainMont(movCon,mov, movementService,log,parameterService);
    }
}