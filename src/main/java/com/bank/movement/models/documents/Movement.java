package com.bank.movement.models.documents;

import com.bank.movement.models.emus.TypeMovement;
import com.bank.movement.models.utils.Audit;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "movements")
public class Movement extends Audit
{
    @Id
    private String id;
    private String pasiveId;
    private String clientId;
    private TypeMovement typeMovement;
    private float mont;
    private float comissionMont;
}
