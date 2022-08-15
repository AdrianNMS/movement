package com.bank.movement.models.utils.responses;

import com.bank.movement.models.utils.Mont;
import lombok.Data;

@Data
public class ResponseMont
{
    private Mont data;

    private String message;

    private String status;

}
