package com.bank.movement.models.documents;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MovementRegister
{
    private String clientId;
    private Float mont;
    private String debitCardId;
}
