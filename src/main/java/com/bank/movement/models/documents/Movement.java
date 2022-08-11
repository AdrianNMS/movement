package com.bank.movement.models.documents;

import com.bank.movement.models.emus.TypeMovement;
import com.bank.movement.models.emus.TypePasiveMovement;
import com.bank.movement.models.utils.Audit;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "movements")
public class Movement extends Audit
{
    @Id
    private String id;
    private String pasiveId;
    private String clientId;
    private TypeMovement typeMovement;
    private TypePasiveMovement typePasiveMovement;
    private float mont;
    private float comissionMont;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy",timezone = "GMT-05:00")
    private LocalDateTime created;
}
