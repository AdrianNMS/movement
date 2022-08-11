package com.bank.movement.controllers;

import com.bank.movement.handler.ResponseHandler;
import com.bank.movement.models.documents.Movement;
import com.bank.movement.models.utils.MovementConditions;
import com.bank.movement.services.MovementService;
import com.bank.movement.services.ParameterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/movement")
public class MovementRestController
{
    @Autowired
    private MovementService movementService;

    @Autowired
    private ParameterService parameterService;
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

    @GetMapping("/clientMovements/{idClient}")
    public Mono<ResponseEntity<Object>> findByIdClient(@PathVariable String idClient)
    {
        log.info("[INI] findByIdClient Movement");
        return movementService.findByIdClient(idClient)
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

        //Obtener monto de pasivo
        return  parameterService.getMont(mov.getPasiveId())
                .flatMap(responseMont ->
                {
                    if(responseMont.getData() != null)
                    {
                        log.info(responseMont.getData().toString());
                        //Guarda monto de pasivo
                        movCon.setPasiveMont(responseMont.getData().getMont());
                        return parameterService.getTypeParams(mov.getPasiveId())
                                .flatMap(parameters -> {
                                    log.info(parameters.toString());
                                    //Condicionales
                                    movCon.setParameters(parameters.getData());
                                    movCon.setMov(mov);

                                    if(movCon.HaveEnoughCredit())
                                    {
                                        log.info("Pasive's client have credit");
                                        movCon.init();
                                        return parameterService.setMont(mov.getPasiveId(),movCon.getMont())
                                                .flatMap(responseMont1 -> {
                                                    log.info(responseMont1.toString());
                                                    if(responseMont1.getStatus().equalsIgnoreCase("Ok"))
                                                        //Obtener movimientos maximos del mes
                                                        return movementService.CountMovements(mov).flatMap(currentMovement -> {
                                                            //Valida condicionales
                                                            if(movCon.CheckContinueTransaction())
                                                                return movementService.Create(mov)
                                                                        .map(movement -> ResponseHandler.response("Done", HttpStatus.OK, movement)                )
                                                                        .onErrorResume(error -> Mono.just(ResponseHandler.response(error.getMessage(), HttpStatus.BAD_REQUEST, null)))
                                                                        .doFinally(fin -> log.info("[END] create Movement"));
                                                            else
                                                                return Mono.just(ResponseHandler.response("You can't generate more movement today", HttpStatus.BAD_REQUEST, null));
                                                        });
                                                    else
                                                        return Mono.just(ResponseHandler.response("Error", HttpStatus.BAD_REQUEST, null));
                                                });
                                    }
                                    else
                                        return Mono.just(ResponseHandler.response("You don't have enough credit", HttpStatus.BAD_REQUEST, null));

                                })
                                .switchIfEmpty(Mono.just(ResponseHandler.response("Empty", HttpStatus.NO_CONTENT, null)));
                    }
                    else
                        return Mono.just(ResponseHandler.response("Not found", HttpStatus.BAD_REQUEST, null));

                })
                .switchIfEmpty(Mono.just(ResponseHandler.response("Empty", HttpStatus.NO_CONTENT, null)));
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Object>> update(@PathVariable("id") String id,@Valid @RequestBody Movement mov)
    {
        log.info("[INI] update Movement");
        log.info(id);
        return movementService.Update(id,mov)
                .flatMap(movement -> Mono.just(ResponseHandler.response("Done", HttpStatus.NO_CONTENT, movement)))
                .onErrorResume(error -> Mono.just(ResponseHandler.response(error.getMessage(), HttpStatus.BAD_REQUEST, null)))
                .switchIfEmpty(Mono.just(ResponseHandler.response("Error", HttpStatus.NO_CONTENT, null)))
                .doFinally(fin -> log.info("[END] update Movement"));

    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Object>> delete(@PathVariable("id") String id)
    {
        log.info("[INI] delete Movement");
        log.info(id);

        return movementService.Delete(id)
                .flatMap(o -> Mono.just(ResponseHandler.response("Done", HttpStatus.NO_CONTENT, null)))
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
                .map(balance -> ResponseHandler.response("Done", HttpStatus.OK, balance))
                .onErrorResume(error -> Mono.just(ResponseHandler.response(error.getMessage(), HttpStatus.BAD_REQUEST, null)))
                .switchIfEmpty(Mono.just(ResponseHandler.response("Empty", HttpStatus.NO_CONTENT, null)))
                .doFinally(fin -> log.info("[END] getBalance Movement"));
    }

}
