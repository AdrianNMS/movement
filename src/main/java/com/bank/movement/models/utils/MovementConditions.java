package com.bank.movement.models.utils;

import com.bank.movement.models.documents.Movement;
import com.bank.movement.models.documents.Parameter;
import com.bank.movement.models.emus.TypePasiveMovement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class MovementConditions
{
    private Parameter parameter;
    private Movement mov;
    private int movementPerMonth=0;
    private float currentPasiveMont=0;

    private boolean differentDates;
    private int movementPerAccount=0;
    private int maxMovementMonth=0;

    private Mont mont;

    public void init() {
        ComissionPercentage();
        setDifferentDates(CheckTransanctionDay());
        setMaxMovementMonth(MaxMovementsPerMont());
        SetParameterPasiveMovement();

        if(NeedToPayCommission())
          ComissionPercentageMaxMovement();

        initMont();
    }

    public void initMont()
    {
        setMont(new Mont());
        getMont().setMont(mov.getCurrentMont());
    }

    private void ComissionPercentage()
    {
        float percentage = getParameter().getComissionPercentage();
        getMov().setComissionMont(getMov().getMont()*percentage);
    }

    private boolean CheckTransanctionDay()
    {
        String argument = getParameter().getTransactionDay();

        if (!argument.equals("false"))
        {
            int day = Integer.parseInt(argument);

            return (LocalDateTime.now().getDayOfMonth() == day);
        }
        else
            return true;
    }

    private int MaxMovementsPerMont()
    {

        String argument = getParameter().getMaxMovementPerMonth();

        if(!argument.equals("INFINITY"))
            return Integer.parseInt(argument);
        else
            return Integer.MAX_VALUE;
    }

    private boolean NeedToPayCommission()
    {
        return (parameter.getMaxMovement()<movementPerAccount);
    }

    private void ComissionPercentageMaxMovement()
    {

        float percentage = parameter.getPercentageMaxMovement();
        getMov().setComissionMaxMont(getMov().getMont()*percentage);

    }

    private void SetParameterPasiveMovement()
    {
        getMov().setTypePasiveMovement(TypePasiveMovement.fromInteger(getParameter().getCode()));
    }

    public boolean CheckContinueTransaction()
    {
        return (isDifferentDates() && getMovementPerMonth() < getMaxMovementMonth());
    }


    public boolean HaveEnoughCredit()
    {
        return (getCurrentPasiveMont() - (getMov().getCurrentMont() - getMov().getComissionMaxMont())) >=0;
    }

    public Mont getMontReceiver()
    {
        Mont m = new Mont();
        m.setMont(mov.getMont());
        return m;
    }


}
