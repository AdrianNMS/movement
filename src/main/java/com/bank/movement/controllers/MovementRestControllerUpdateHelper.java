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

public class MovementRestControllerUpdateHelper
{
    public static Mono<ResponseEntity<Object>> UpdateMovement(MovementConditions movCon, IMovementService movementService, Logger log, IParameterService parameterService)
    {
        return movementService.Update(movCon.getMov().getId(),movCon.getMov())
                .flatMap(movement -> Mono.just(ResponseHandler.response("Done", HttpStatus.NO_CONTENT, movement)))
                .onErrorResume(error -> Mono.just(ResponseHandler.response(error.getMessage(), HttpStatus.BAD_REQUEST, null)))
                .switchIfEmpty(Mono.just(ResponseHandler.response("Error", HttpStatus.NO_CONTENT, null)))
                .doFinally(fin -> log.info("[END] update Movement"));
    }

    public static Mono<ResponseEntity<Object>> UpdateMontReceiver(MovementConditions movCon, IMovementService movementService, Logger log, IParameterService parameterService)
    {
        return parameterService.setMont(movCon.getMov().getPasiveReceiverId(),movCon.getMontReceiver())
                .flatMap(responseMont1 -> {
                    log.info(responseMont1.toString());
                    if(responseMont1.getStatus().equalsIgnoreCase("Ok"))
                        return UpdateMovement(movCon, movementService,log,parameterService);
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
                            return UpdateMovement(movCon, movementService,log,parameterService);
                    else
                        return Mono.just(ResponseHandler.response("Error", HttpStatus.BAD_REQUEST, null));
                }).switchIfEmpty(Mono.just(ResponseHandler.response("Empty", HttpStatus.NO_CONTENT, null)));
    }

    public static Mono<ResponseEntity<Object>> CheckOldMont(MovementConditions movCon, IMovementService movementService, Logger log, IParameterService parameterService)
    {
        return movementService.Find(movCon.getMov().getId()).flatMap(movement ->
                {
                    if(movement!=null)
                        if(movCon.canAffortUpdateMonth(movCon.getMov(),movement))
                            return UpdateMont(movCon, movementService,log,parameterService);
                        else
                            return Mono.just(ResponseHandler.response("Empty", HttpStatus.NO_CONTENT, null));
                    else
                        return Mono.just(ResponseHandler.response("Not Found", HttpStatus.NOT_FOUND, null));
                }
        ).switchIfEmpty(Mono.just(ResponseHandler.response("Empty", HttpStatus.NO_CONTENT, null)));
    }

    public static Mono<ResponseEntity<Object>> ObtainListParameters(MovementConditions movCon,  IMovementService movementService, Logger log, IParameterService parameterService)
    {
        return parameterService.getTypeParams(movCon.getMov().getPasiveId())
                .flatMap(parameters -> {
                    log.info(parameters.toString());
                    //Condicionales
                    movCon.setParameters(parameters.getData());

                    return CheckOldMont(movCon, movementService,log,parameterService);
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

    public static Mono<ResponseEntity<Object>> UpdateMovementSequence(MovementConditions movCon,  IMovementService movementService, Logger log, IParameterService parameterService)
    {
        return ObtainMont(movCon, movementService,log,parameterService);
    }
}
