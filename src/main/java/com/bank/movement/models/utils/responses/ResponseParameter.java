package com.bank.movement.models.utils.responses;

import com.bank.movement.models.documents.Parameter;
import lombok.Data;

@Data
public class ResponseParameter
{
    private Parameter data;

    private String message;

    private String status;

}
