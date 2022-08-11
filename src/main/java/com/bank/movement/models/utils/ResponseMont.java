package com.bank.movement.models.utils;

import com.bank.movement.models.documents.Parameter;
import lombok.Data;

import java.util.List;

@Data
public class ResponseMont
{
    private Mont data;

    private String message;

    private String status;

}
