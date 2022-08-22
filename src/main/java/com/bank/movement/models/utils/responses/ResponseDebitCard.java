package com.bank.movement.models.utils.responses;

import lombok.Data;

@Data
public class ResponseDebitCard
{
    private Boolean data;
    private String message;

    private String status;
}
