package com.bank.movement.services.impl;

import com.bank.movement.models.utils.Mont;
import com.bank.movement.models.utils.responses.ResponseMont;
import com.bank.movement.models.utils.responses.ResponseParameter;
import com.bank.movement.services.IPasiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


@Service
public class PasiveImpl implements IPasiveService {

    @Autowired
    WebClient webClient;

    @Override
    public Mono<ResponseParameter> getTypeParams(String idPasive)
    {
        return webClient.get()
                .uri("/api/pasive/type/"+ idPasive)
                .retrieve()
                .bodyToMono(ResponseParameter.class);
    }

    @Override
    public Mono<ResponseMont> getMont(String idPasive) {
        return webClient.get()
                .uri("/api/pasive/mont/"+ idPasive)
                .retrieve()
                .bodyToMono(ResponseMont.class);
    }

    @Override
    public Mono<ResponseMont> setMont(String idPasive, Mont mont) {
        return webClient.post()
                .uri("/api/pasive/mont/"+ idPasive)
                .body(Mono.just(mont), Mont.class)
                .retrieve()
                .bodyToMono(ResponseMont.class);
    }
}