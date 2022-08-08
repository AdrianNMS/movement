package com.bank.movement.controllers;

import com.bank.movement.handler.ResponseHandler;
import com.bank.movement.models.dao.MovementDao;
import com.bank.movement.models.documents.Movement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/movement")
public class MovementRestController
{
    @Autowired
    private MovementDao dao;
    private static final Logger log = LoggerFactory.getLogger(MovementRestController.class);

    @GetMapping
    public Mono<ResponseEntity<Object>> findAll()
    {
        return dao.findAll()
                .doOnNext(movement -> log.info(movement.toString()))
                .collectList()
                .map(movements -> ResponseHandler.response("Done", HttpStatus.OK, movements))
                .onErrorResume(error -> Mono.just(ResponseHandler.response(error.getMessage(), HttpStatus.BAD_REQUEST, null)));
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Object>> find(@PathVariable String id)
    {
        return dao.findById(id)
                .doOnNext(movement -> log.info(movement.toString()))
                .map(movement -> ResponseHandler.response("Done", HttpStatus.OK, movement))
                .onErrorResume(error -> Mono.just(ResponseHandler.response(error.getMessage(), HttpStatus.BAD_REQUEST, null)));
    }

    @PostMapping
    public Mono<ResponseEntity<Object>> create(@RequestBody Movement mov)
    {

        return dao.save(mov)
                .doOnNext(movement -> log.info(movement.toString()))
                .map(movement -> ResponseHandler.response("Done", HttpStatus.OK, movement)                )
                .onErrorResume(error -> Mono.just(ResponseHandler.response(error.getMessage(), HttpStatus.BAD_REQUEST, null)));
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Object>> update(@PathVariable("id") String id, @RequestBody Movement mov)
    {
        return dao.existsById(id).flatMap(check -> {
            if (check)
                return dao.save(mov)
                        .doOnNext(movement -> log.info(movement.toString()))
                        .map(movement -> ResponseHandler.response("Done", HttpStatus.OK, movement)                )
                        .onErrorResume(error -> Mono.just(ResponseHandler.response(error.getMessage(), HttpStatus.BAD_REQUEST, null)));
            else
                return Mono.just(ResponseHandler.response("Not found", HttpStatus.NOT_FOUND, null));

        });
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Object>> delete(@PathVariable("id") String id)
    {
        log.info(id);

        return dao.existsById(id).flatMap(check -> {
            if (check)
                return dao.deleteById(id).then(Mono.just(ResponseHandler.response("Done", HttpStatus.OK, null)));
            else
                return Mono.just(ResponseHandler.response("Not found", HttpStatus.NOT_FOUND, null));
        });
    }
}
