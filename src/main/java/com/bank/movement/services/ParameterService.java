package com.bank.movement.services;

import com.bank.movement.models.documents.Parameter;
import com.bank.movement.models.utils.ResponseParameter;
import reactor.core.publisher.Mono;

public interface ParameterService {

    Mono<ResponseParameter> getTypeParams(String idPasive);
}
