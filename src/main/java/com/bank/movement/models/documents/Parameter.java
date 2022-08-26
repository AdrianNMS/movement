package com.bank.movement.models.documents;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import javax.validation.constraints.NotNull;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Parameter {

    private String id;
    private Integer code;
    private Float comissionPercentage;
    private String transactionDay;
    private String maxMovementPerMonth;
    private Integer maxMovement;
    private Float percentageMaxMovement;

}
