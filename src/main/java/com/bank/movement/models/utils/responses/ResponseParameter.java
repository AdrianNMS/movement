package com.bank.movement.models.utils.responses;

import com.bank.movement.models.documents.Parameter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ResponseParameter
{
    private Parameter data;

    private String message;

    private String status;

}
