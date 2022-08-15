package com.bank.movement.models.emus;

import lombok.AllArgsConstructor;
import lombok.Getter;


@AllArgsConstructor
@Getter
public enum TypePasiveMovement {

    SAVING(1000),
    ACCOUNT(1001),
    FIXEDTERM(1002),
    SAVINGVIP(1003),
    ACCOUNTPYME(1004);

    private final int value;

    public static TypePasiveMovement fromInteger(int val) {
        switch(val) {
            case 1000:
                return SAVING;
            case 1001:
                return ACCOUNT;
            case 1002:
                return SAVINGVIP;
            case 1003:
                return FIXEDTERM;
            case 1004:
                return ACCOUNTPYME;
        }
        return null;
    }

}

