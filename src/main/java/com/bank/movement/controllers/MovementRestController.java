package com.bank.movement.controllers;

import com.bank.movement.handler.ResponseHandler;
import com.bank.movement.models.dao.MovementDao;
import com.bank.movement.models.documents.Movement;
import com.bank.movement.models.documents.Parameter;
import com.bank.movement.models.emus.TypePasiveMovement;
import com.bank.movement.services.ParameterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@RestController
@RequestMapping("/api/movement")
public class MovementRestController
{
    @Autowired
    private MovementDao dao;

    @Autowired
    private ParameterService parameterService;
    private static final Logger log = LoggerFactory.getLogger(MovementRestController.class);

    @GetMapping
    public Mono<ResponseEntity<Object>> findAll()
    {
        log.info("[INI] findAll Movement");
        return dao.findAll()
                .doOnNext(movement -> log.info(movement.toString()))
                .collectList()
                .map(movements -> ResponseHandler.response("Done", HttpStatus.OK, movements))
                .onErrorResume(error -> Mono.just(ResponseHandler.response(error.getMessage(), HttpStatus.BAD_REQUEST, null)))
                .switchIfEmpty(Mono.just(ResponseHandler.response("Empty", HttpStatus.NO_CONTENT, null)))
                .doFinally(fin -> log.info("[END] findAll Movement"));
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Object>> find(@PathVariable String id)
    {
        log.info("[INI] find Movement");
        return dao.findById(id)
                .doOnNext(movement -> log.info(movement.toString()))
                .map(movement -> ResponseHandler.response("Done", HttpStatus.OK, movement))
                .onErrorResume(error -> Mono.just(ResponseHandler.response(error.getMessage(), HttpStatus.BAD_REQUEST, null)))
                .switchIfEmpty(Mono.just(ResponseHandler.response("Empty", HttpStatus.NO_CONTENT, null)))
                .doFinally(fin -> log.info("[END] find Movement"));
    }

    @PostMapping
    public Mono<ResponseEntity<Object>> create(@RequestBody Movement mov)
    {
        log.info("[INI] create Movement");
        mov.setCreated(LocalDateTime.now());



        return parameterService.getTypeParams(mov.getPasiveId())
                .flatMap(parameters -> {
                    log.info(parameters.toString());

                    List<Parameter> parametersData = parameters.getData();

                    mov.setTypePasiveMovement(TypePasiveMovement.fromInteger(parametersData.get(0).getCode()));

                    for (Parameter parameter: parametersData)
                    {
                        if(parameter.getValue().equals("1") && !parameter.getArgument().equals("0"))
                        {
                            float percentage = Float.parseFloat(parameter.getArgument());
                            mov.setComissionMont(mov.getMont()*percentage);
                        }
                        else if (parameter.getValue().equals("2") && !parameter.getArgument().equals("false"))
                        {
                            int day = Integer.parseInt(parameter.getArgument());

                            if(LocalDateTime.now().getDayOfMonth() != day)
                                return Mono.just(ResponseHandler.response("You can't make movements today", HttpStatus.BAD_REQUEST, null));
                        }
                        else if (parameter.getValue().equals("3") && !parameter.getArgument().equals("false"))
                        {
                            int maxMovement = Integer.parseInt(parameter.getArgument());
                            AtomicReference<Integer> currentMovement = new AtomicReference<>(0);

                            return dao.findAll().doOnNext(movement -> {
                                String dateCreated = movement.getCreated().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                                String dateNow = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                                if(dateCreated.equals(dateNow) && movement.getTypePasiveMovement() == mov.getTypePasiveMovement())
                                {
                                    currentMovement.getAndSet(currentMovement.get() + 1);
                                }
                            }).collectList().flatMap(movements -> {

                                if(currentMovement.get() >= maxMovement)
                                {
                                    return Mono.just(ResponseHandler.response("You can't do more movements", HttpStatus.BAD_REQUEST, null));
                                }
                                else
                                {
                                    return dao.save(mov)
                                            .doOnNext(movement -> log.info(movement.toString()))
                                            .map(movement -> ResponseHandler.response("Done", HttpStatus.OK, movement)                )
                                            .onErrorResume(error -> Mono.just(ResponseHandler.response(error.getMessage(), HttpStatus.BAD_REQUEST, null)))
                                            .doFinally(fin -> log.info("[END] create Movement"));
                                }
                            });


                        }
                    }

                    return dao.save(mov)
                            .doOnNext(movement -> log.info(movement.toString()))
                            .map(movement -> ResponseHandler.response("Done", HttpStatus.OK, movement)                )
                            .onErrorResume(error -> Mono.just(ResponseHandler.response(error.getMessage(), HttpStatus.BAD_REQUEST, null)))
                            .doFinally(fin -> log.info("[END] create Movement"));


                })
                .switchIfEmpty(Mono.just(ResponseHandler.response("Empty", HttpStatus.NO_CONTENT, null)));
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Object>> update(@PathVariable("id") String id, @RequestBody Movement mov)
    {
        log.info("[INI] update Movement");
        return dao.existsById(id).flatMap(check -> {
            if (check)
                return dao.save(mov)
                        .doOnNext(movement -> log.info(movement.toString()))
                        .map(movement -> ResponseHandler.response("Done", HttpStatus.OK, movement)                )
                        .onErrorResume(error -> Mono.just(ResponseHandler.response(error.getMessage(), HttpStatus.BAD_REQUEST, null)));
            else
                return Mono.just(ResponseHandler.response("Not found", HttpStatus.NOT_FOUND, null));

        }).doFinally(fin -> log.info("[END] update Movement"));
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Object>> delete(@PathVariable("id") String id)
    {
        log.info("[INI] delete Movement");
        log.info(id);

        return dao.existsById(id).flatMap(check -> {
            if (check)
                return dao.deleteById(id).then(Mono.just(ResponseHandler.response("Done", HttpStatus.OK, null)));
            else
                return Mono.just(ResponseHandler.response("Not found", HttpStatus.NOT_FOUND, null));
        }).doFinally(fin -> log.info("[END] delete Movement"));
    }
}
