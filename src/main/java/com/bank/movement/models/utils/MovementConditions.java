package com.bank.movement.models.utils;

import com.bank.movement.models.documents.Movement;
import com.bank.movement.models.documents.Parameter;
import com.bank.movement.models.emus.TypePasiveMovement;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MovementConditions
{
    private Parameter parameter;
    private Movement mov;
    private int movementPerMonth;
    private float currentPasiveMont;

    private boolean differentDates;
    private int movementPerAccount;
    private int maxMovementMonth;

    private Mont mont;

    public void init() {
        ComissionPercentage();
        setDifferentDates(CheckTransanctionDay());
        setMaxMovementMonth(MaxMovementsPerMont());
        SetParameterPasiveMovement();

        if(NeedToPayCommission())
          ComissionPercentageMaxMovement();

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
        return (getCurrentPasiveMont() - (getMov().getCurrentMont() - getMov().getComissionMaxMont())) >0;
    }

    public boolean canAffortUpdateMonth(Movement newMov, Movement oldMov)
    {
        float newMont = (oldMov.getCurrentMont() - newMov.getCurrentMont()) - getCurrentPasiveMont();

        getMont().setMont(newMont);

        return (newMont) > 0;
    }

    public Mont getMontReceiver()
    {
        Mont m = new Mont();
        m.setMont(mov.getMont());
        return m;
    }


}
