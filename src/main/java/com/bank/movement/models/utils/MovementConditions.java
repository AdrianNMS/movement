package com.bank.movement.models.utils;

import com.bank.movement.models.documents.Movement;
import com.bank.movement.models.documents.Parameter;
import com.bank.movement.models.emus.TypePasiveMovement;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Getter
@Setter
public class MovementConditions
{
    private List<Parameter> parameters;
    private Movement mov;
    private int currentMovement;
    private float pasiveMont;

    private boolean differentDates;
    private int maxMovement;
    private boolean settedParamType;

    private Mont mont;

    public void init()
    {
        ComissionPercentage();
        differentDates = CheckTransanctionDay();
        maxMovement = MaxMovements();
        settedParamType = SetParameterPasiveMovement();

        this.mont = new Mont();
        mont.setMont(mov.getCurrentMont());
        mont.setIdPasive(mov.getPasiveId());
    }

    private void ComissionPercentage()
    {
        this.parameters.stream()
                .filter(parameter -> "1".equals(parameter.getValue()))
                .findFirst()
                .ifPresent(parameter ->
                {
                    String argument = parameter.getArgument();

                    try
                    {
                        float percentage = Float.parseFloat(argument);
                        this.mov.setComissionMont(this.mov.getMont()*percentage);
                    }
                    catch (NumberFormatException e)
                    {
                        this.mov.setComissionMont(0);
                    }
                });
    }

    private boolean CheckTransanctionDay()
    {

        Optional<Parameter> param = this.parameters.stream()
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
        Optional<Parameter> param = this.parameters.stream()
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

    private boolean SetParameterPasiveMovement()
    {
        if(this.parameters.size()>=1)
        {
            this.mov.setTypePasiveMovement(TypePasiveMovement.fromInteger(this.parameters.get(0).getCode()));
            return true;
        }
        else
        {
            return false;
        }
    }

    public boolean CheckContinueTransaction()
    {
        return (settedParamType &&(differentDates && currentMovement < maxMovement));
    }

    public boolean HaveEnoughCredit()
    {
        return (getPasiveMont() - getMov().getCurrentMont()) >0;
    }

}
