package com.bank.movement.models.utils;

import com.bank.movement.models.documents.Parameter;
import lombok.Data;
import reactor.core.publisher.Flux;

import java.util.List;

@Data
public class ResponseParameter
{
    private List<Parameter> data;

    private String message;

    private String status;

}
