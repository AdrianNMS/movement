package com.bank.movement.services.impl;

import com.bank.movement.models.documents.Parameter;
import com.bank.movement.models.utils.ResponseParameter;
import com.bank.movement.services.ParameterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class ParameterImpl implements ParameterService {

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
}