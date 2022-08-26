package com.bank.movement.models.utils.responses;

import com.bank.movement.models.utils.Mont;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ResponseMont
{
    private Mont data;

    private String message;

    private String status;

}
