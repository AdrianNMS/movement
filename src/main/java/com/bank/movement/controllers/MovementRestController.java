package com.bank.movement.controllers;

import com.bank.movement.controllers.helpers.MovementRestControllerCreateHelper;
import com.bank.movement.handler.ResponseHandler;
import com.bank.movement.models.documents.Movement;
import com.bank.movement.models.emus.TypePasiveMovement;
import com.bank.movement.models.utils.MovementConditions;
import com.bank.movement.services.IMovementService;
import com.bank.movement.services.IPasiveService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/movement")
public class MovementRestController
{
    @Autowired
    private IMovementService movementService;

    @Autowired
    private IPasiveService pasiveService;
    private static final Logger log = LoggerFactory.getLogger(MovementRestController.class);

    @GetMapping
    public Mono<ResponseEntity<Object>> findAll()
    {
        log.info("[INI] findAll Movement");
        return movementService.FindAll()
                .map(movements -> ResponseHandler.response("Done", HttpStatus.OK, movements))
                .onErrorResume(error -> Mono.just(ResponseHandler.response(error.getMessage(), HttpStatus.BAD_REQUEST, null)))
                .switchIfEmpty(Mono.just(ResponseHandler.response("Empty", HttpStatus.NO_CONTENT, null)))
                .doFinally(fin -> log.info("[END] findAll Movement"));
    }

    @GetMapping("/clientMovements/{id}")
    public Mono<ResponseEntity<Object>> findByIdPasive(@PathVariable String id)
    {
        log.info("[INI] findByIdClient Movement");
        return movementService.FindByIdPasive(id)
                .map(movements -> ResponseHandler.response("Done", HttpStatus.OK, movements))
                .onErrorResume(error -> Mono.just(ResponseHandler.response(error.getMessage(), HttpStatus.BAD_REQUEST, null)))
                .switchIfEmpty(Mono.just(ResponseHandler.response("Empty", HttpStatus.NO_CONTENT, null)))
                .doFinally(fin -> log.info("[END] findByIdClient Movement"));
    }


    @GetMapping("/{id}")
    public Mono<ResponseEntity<Object>> find(@PathVariable String id)
    {
        log.info("[INI] find Movement");
        return movementService.Find(id)
                .map(movement -> ResponseHandler.response("Done", HttpStatus.OK, movement))
                .onErrorResume(error -> Mono.just(ResponseHandler.response(error.getMessage(), HttpStatus.BAD_REQUEST, null)))
                .switchIfEmpty(Mono.just(ResponseHandler.response("Empty", HttpStatus.NO_CONTENT, null)))
                .doFinally(fin -> log.info("[END] find Movement"));
    }

    @PostMapping
    public Mono<ResponseEntity<Object>> create(@Valid  @RequestBody Movement mov)
    {
        log.info("[INI] create Movement");
        mov.setCreated(LocalDateTime.now());

        MovementConditions movCon = new MovementConditions();
        movCon.setMov(mov);

        return MovementRestControllerCreateHelper.CreateMovementSequence(movCon, movementService,log, pasiveService);
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Object>> delete(@PathVariable("id") String id)
    {
        log.info("[INI] delete Movement");
        log.info(id);

        return movementService.Delete(id)
                .flatMap(o -> Mono.just(ResponseHandler.response("Done", HttpStatus.OK, null)))
                .onErrorResume(error -> Mono.just(ResponseHandler.response(error.getMessage(), HttpStatus.BAD_REQUEST, null)))
                .switchIfEmpty(Mono.just(ResponseHandler.response("Error", HttpStatus.NO_CONTENT, null)))
                .doFinally(fin -> log.info("[END] delete Movement"));
    }

    @GetMapping("/balance/{id}")
    public Mono<ResponseEntity<Object>> getBalance(@PathVariable("id") String id)
    {
        log.info("[INI] getBalance Movement");
        log.info(id);

        return movementService.GetBalance(id)
                .flatMap(balance -> Mono.just(ResponseHandler.response("Done", HttpStatus.OK, balance)))
                .onErrorResume(error -> Mono.just(ResponseHandler.response(error.getMessage(), HttpStatus.BAD_REQUEST, null)))
                .switchIfEmpty(Mono.just(ResponseHandler.response("Empty", HttpStatus.NO_CONTENT, null)))
                .doFinally(fin -> log.info("[END] getBalance Movement"));
    }

    @GetMapping("/report")
    public Mono<ResponseEntity<Object>> reports(@Validated @RequestParam("max") String max, @Validated @RequestParam("min") String min)
    {

        log.info("[INI] Reports Movement");
        return movementService.ComissionReportBetween(min,max)
                .flatMap(comission -> Mono.just(ResponseHandler.response("Done", HttpStatus.OK, comission)))
                .onErrorResume(error -> Mono.just(ResponseHandler.response(error.getMessage(), HttpStatus.BAD_REQUEST, null)))
                .switchIfEmpty(Mono.just(ResponseHandler.response("Empty", HttpStatus.NO_CONTENT, null)))
                .doFinally(fin -> log.info("[END] Reports Movement"));
    }

    @GetMapping("/report/debit/{idDebitCard}")
    public Mono<ResponseEntity<Object>> getFist10MovementDebitCard(@Validated @PathVariable String idDebitCard)
    {
        log.info("[INI] getFist10MovementDebitCard Movement");
        return movementService.FindAll()
                .flatMap(movements -> {
                    var listMov = movements.stream().filter(movement ->
                            movement.getDebitCardId()!=null
                            && !movement.getDebitCardId().isEmpty()
                            && movement.getDebitCardId().equals(idDebitCard)
                            && movement.getTypePasiveMovement() == TypePasiveMovement.DEBITCARD
                            )
                            .limit(10)
                            .collect(Collectors.toList());

                    if(!listMov.isEmpty())
                        return Mono.just(ResponseHandler.response("Done", HttpStatus.OK, listMov));
                    else
                        return Mono.just(ResponseHandler.response("Empty", HttpStatus.NOT_FOUND, null));
                })
                .doFinally(fin -> log.info("[END] getFist10MovementDebitCard Movement"));
    }

}
