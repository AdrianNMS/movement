package com.bank.movement.models.documents;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ComissionReport
{
    private String idEmisor;
    private String idReceptor;
    private Float Mont;
    private Float MontComission;
}
