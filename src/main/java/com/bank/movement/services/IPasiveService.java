package com.bank.movement.services;

import com.bank.movement.models.utils.Mont;
import com.bank.movement.models.utils.responses.ResponseMont;
import com.bank.movement.models.utils.responses.ResponseParameter;
import reactor.core.publisher.Mono;

public interface IPasiveService {

    Mono<ResponseParameter> getTypeParams(String idPasive);
    Mono<ResponseMont> getMont(String idPasive);
    Mono<ResponseMont> setMont(String idPasive, Mont mont);
}
