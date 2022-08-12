package com.bank.movement.models.utils;

import com.bank.movement.models.documents.Movement;
import com.bank.movement.models.documents.Parameter;
import com.bank.movement.models.emus.TypePasiveMovement;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Data
public class MovementConditions
{
    private List<Parameter> parameters;
    private Movement mov;
    private int movementPerMonth;
    private float currentPasiveMont;

    private boolean differentDates;
    private int movementPerAccount;
    private int maxMovementMonth;
    private boolean settedParamType;

    private Mont mont;

    public void init()
    {
        ComissionPercentage();
        setDifferentDates(CheckTransanctionDay());
        setMaxMovementMonth(MaxMovements());
        setSettedParamType(SetParameterPasiveMovement());

        if(NeedToPayCommission())
            ComissionPercentageMaxMovement();


        setMont(new Mont());
        getMont().setMont(mov.getCurrentMont());
    }

    private void ComissionPercentage()
    {
        getParameters().stream()
                .filter(parameter -> "1".equals(parameter.getValue()))
                .findFirst()
                .ifPresent(parameter ->
                {
                    String argument = parameter.getArgument();

                    try
                    {
                        float percentage = Float.parseFloat(argument);
                        getMov().setComissionMont(getMov().getMont()*percentage);
                    }
                    catch (NumberFormatException e)
                    {
                        getMov().setComissionMont(0f);
                    }
                });
    }

    private boolean CheckTransanctionDay()
    {

        Optional<Parameter> param = getParameters().stream()
                .filter(parameter -> "2".equals(parameter.getValue()))
                .findFirst();

        if(param.isPresent())
        {
            String argument = param.get().getArgument();

            if (!argument.equals("false"))
            {
                int day = Integer.parseInt(argument);

                return (LocalDateTime.now().getDayOfMonth() == day);
            } else
                return true;
        }
        else
        {
            return false;
        }
    }

    private int MaxMovements()
    {
        Optional<Parameter> param = getParameters().stream()
                .filter(parameter -> "3".equals(parameter.getValue()))
                .findFirst();

        if(param.isPresent())
        {
            String argument = param.get().getArgument();

            if(!argument.equals("false"))
                return Integer.parseInt(argument);
            else
                return 99999999;
        }
        else
        {
            return 0;
        }
    }

    private void ComissionPercentageMaxMovement()
    {
        getParameters().stream()
                .filter(parameter -> "4".equals(parameter.getValue()))
                .findFirst()
                .ifPresent(parameter ->
                {
                    String argument = parameter.getArgument();

                    try
                    {
                        float percentage = Float.parseFloat(argument);
                        getMov().setComissionMaxMont(getMov().getMont()*percentage);
                    }
                    catch (NumberFormatException e)
                    {
                        getMov().setComissionMaxMont(0f);
                    }
                });
    }

    private boolean NeedToPayCommission()
    {
        Optional<Parameter> param = getParameters().stream()
                .filter(parameter -> "5".equals(parameter.getValue()))
                .findFirst();

        if(param.isPresent())
        {
            String argument = param.get().getArgument();

            try
            {
                float maxMovPerAccount = Integer.parseInt(argument);

                return (maxMovPerAccount<movementPerAccount);

            }
            catch (NumberFormatException e)
            {
                return true;
            }
        }
        else
        {
            return true;
        }

    }

    private boolean SetParameterPasiveMovement()
    {
        if(this.parameters.size()>=1)
        {
            getMov().setTypePasiveMovement(TypePasiveMovement.fromInteger(getParameters().get(0).getCode()));
            return true;
        }
        else
        {
            return false;
        }
    }

    public boolean CheckContinueTransaction()
    {
        return (isSettedParamType() &&(isDifferentDates() && getMovementPerMonth() < getMaxMovementMonth()));
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


}
