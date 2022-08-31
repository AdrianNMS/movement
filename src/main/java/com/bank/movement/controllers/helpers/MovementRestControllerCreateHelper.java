package com.bank.movement.controllers.helpers;

import com.bank.movement.handler.ResponseHandler;
import com.bank.movement.models.emus.TypeMovement;
import com.bank.movement.models.emus.TypePasiveMovement;
import com.bank.movement.models.utils.Mont;
import com.bank.movement.models.utils.MovementConditions;
import com.bank.movement.services.IMovementService;
import com.bank.movement.services.IPasiveService;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

import javax.ws.rs.core.Response;

public class MovementRestControllerCreateHelper
{
    public static Mono<ResponseEntity<Object>> SaveMovement(MovementConditions movCon, IMovementService movementService, Logger log, IPasiveService pasiveService)
    {
        return movementService.Create(movCon.getMov())
                .flatMap(movement -> Mono.just(ResponseHandler.response("Done", HttpStatus.OK, movement)))
                .onErrorResume(error -> Mono.just(ResponseHandler.response(error.getMessage(), HttpStatus.BAD_REQUEST, null)))
                .doFinally(fin -> log.info("[END] create Movement"));

    }

    public static Mono<ResponseEntity<Object>> UpdateMontReceiver(MovementConditions movCon, IMovementService movementService, Logger log, IPasiveService pasiveService)
    {
        return pasiveService.setMont(movCon.getMov().getPasiveReceiverId(),movCon.getMontReceiver())
                .flatMap(responseMont1 -> {
                    log.info(responseMont1.toString());
                    if(responseMont1.getStatus().equalsIgnoreCase("Ok"))
                        return SaveMovement(movCon, movementService,log,pasiveService);
                    else
                        return Mono.just(ResponseHandler.response("Error", HttpStatus.BAD_REQUEST, null));
                }).switchIfEmpty(Mono.just(ResponseHandler.response("Empty", HttpStatus.NO_CONTENT, null)));
    }

    public static Mono<ResponseEntity<Object>> UpdateMont(MovementConditions movCon, IMovementService movementService, Logger log, IPasiveService pasiveService)
    {
        return pasiveService.setMont(movCon.getMov().getPasiveId(),movCon.getMont())
                .flatMap(responseMont1 -> {

                    if(responseMont1.getStatus().equalsIgnoreCase("Ok"))
                        if(movCon.getMov().getTypeMovement() == TypeMovement.DEPOSITS)
                        {
                            if(movCon.getMov().getIsThirdPartyMovement())
                                return SaveMovement(movCon, movementService,log,pasiveService);
                            else
                                return UpdateMontReceiver(movCon, movementService,log,pasiveService);
                        }
                        else
                            return SaveMovement(movCon, movementService,log,pasiveService);
                    else
                        return Mono.just(ResponseHandler.response("Error", HttpStatus.BAD_REQUEST, null));
                }).switchIfEmpty(Mono.just(ResponseHandler.response("Empty", HttpStatus.NO_CONTENT, null)));
    }

    public static Mono<ResponseEntity<Object>> ObtainMovementPerMonth(MovementConditions movCon, IMovementService movementService, Logger log, IPasiveService pasiveService)
    {
        //Obtener movimientos maximos del mes
        return movementService.CountMovementsPerMonth(movCon.getMov()).flatMap(currentMovement -> {
            movCon.setMovementPerMonth(currentMovement);

            //Valida condicionales
            if(movCon.CheckContinueTransaction())
                return UpdateMont(movCon, movementService,log,pasiveService);
            else
                return Mono.just(ResponseHandler.response("You can't generate more movement today", HttpStatus.BAD_REQUEST, null));

        }).switchIfEmpty(Mono.just(ResponseHandler.response("Empty", HttpStatus.NO_CONTENT, null)));
    }

    public static Mono<ResponseEntity<Object>> PasiveMovement(MovementConditions movCon, IMovementService movementService, Logger log, IPasiveService pasiveService)
    {
        //movimientos del pasivo
        return movementService.CountMovements(movCon.getMov()).flatMap(currentMov ->
        {
            movCon.setMovementPerAccount(currentMov);
            movCon.init();

            if(movCon.HaveEnoughCredit())
            {
                log.info("Pasive's client have credit");

                return ObtainMovementPerMonth(movCon, movementService,log,pasiveService);
            }
            else
                return Mono.just(ResponseHandler.response("You don't have enough credit", HttpStatus.BAD_REQUEST, null));

        }).switchIfEmpty(Mono.just(ResponseHandler.response("Empty", HttpStatus.NO_CONTENT, null)));
    }

    public static Mono<ResponseEntity<Object>> PayWithCreditCard(MovementConditions movCon, IMovementService movementService, Logger log, IPasiveService pasiveService)
    {
        movCon.initMont();
        log.info(movCon.getMov().toString());
        log.info(movCon.getMont().toString());
        return pasiveService.payWithDebitCard(movCon.getMov().getDebitCardId(),movCon.getMont())
                .flatMap(responseDebitCard -> {
                    if(responseDebitCard.getData())
                    {
                        if(movCon.getMov().getTypeMovement() == TypeMovement.DEPOSITS)
                        {
                            if(movCon.getMov().getIsThirdPartyMovement())
                                return SaveMovement(movCon, movementService,log,pasiveService);
                            else
                                return UpdateMontReceiver(movCon, movementService,log,pasiveService);
                        }
                        else
                            return SaveMovement(movCon, movementService,log,pasiveService);
                    }
                    else
                        return Mono.just(ResponseHandler.response("You don't have enough credit", HttpStatus.BAD_REQUEST, null));
                });
    }

    public static Mono<ResponseEntity<Object>> ObtainListParameters(MovementConditions movCon, IMovementService movementService, Logger log, IPasiveService pasiveService)
    {
        return pasiveService.getTypeParams(movCon.getMov().getPasiveId())
                .flatMap(parameter -> {
                    log.info(parameter.toString());
                    //Condicionales
                    movCon.setParameter(parameter.getData());

                    return PasiveMovement(movCon, movementService, log, pasiveService);

                })
                .switchIfEmpty(Mono.just(ResponseHandler.response("Empty", HttpStatus.NO_CONTENT, null)));
    }

    public static Mono<ResponseEntity<Object>> ObtainMont(MovementConditions movCon, IMovementService movementService, Logger log, IPasiveService pasiveService)
    {
        //Obtener monto de pasivo
        return  pasiveService.getMont(movCon.getMov().getPasiveId())
                .flatMap(responseMont ->
                {
                    if(responseMont.getData() != null)
                    {
                        log.info(responseMont.getData().toString());
                        //Guarda monto de pasivo
                        movCon.setCurrentPasiveMont(responseMont.getData().getMont());
                        return ObtainListParameters(movCon, movementService,log,pasiveService);
                    }
                    else
                        return Mono.just(ResponseHandler.response("Not found", HttpStatus.BAD_REQUEST, null));

                })
                .switchIfEmpty(Mono.just(ResponseHandler.response("Empty", HttpStatus.NO_CONTENT, null)));
    }

    public static Mono<ResponseEntity<Object>> CreateMovementSequence(MovementConditions movCon, IMovementService movementService, Logger log, IPasiveService pasiveService)
    {
        if (movCon.getMov().getDebitCardId() == null || movCon.getMov().getDebitCardId().isEmpty())
            return ObtainMont(movCon, movementService,log,pasiveService);
        else
        {
            movCon.getMov().setTypePasiveMovement(TypePasiveMovement.DEBITCARD);
            return PayWithCreditCard(movCon,movementService,log,pasiveService);
        }


    }
}