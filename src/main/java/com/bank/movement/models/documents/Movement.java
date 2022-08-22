package com.bank.movement.models.documents;

import com.bank.movement.models.emus.TypeMovement;
import com.bank.movement.models.emus.TypePasiveMovement;
import com.bank.movement.models.utils.Audit;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.Value;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Document(collection = "movements")
public class Movement extends Audit
{
    @Id
    private String id;
    @NotNull(message = "pasiveId must not be null")
    private String pasiveId;
    @NotNull(message = "clientId must not be null")
    private String clientId;
    @NotNull(message = "pasiveReceiverId must not be null")
    private String pasiveReceiverId;
    @NotNull(message = "typeMovement must not be null")
    private TypeMovement typeMovement;
    private TypePasiveMovement typePasiveMovement;
    @NotNull(message = "mont must not be null")
    @Min(1)
    private Float mont;

    private Float comissionMont=0f;
    private Float comissionMaxMont=0f;


    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy",timezone = "GMT-05:00")
    private LocalDateTime created;

    private Boolean isThirdPartyMovement;

    public float getCurrentMont()
    {
        return -(getMont() + (getComissionMont() + getComissionMaxMont()));
    }

}
