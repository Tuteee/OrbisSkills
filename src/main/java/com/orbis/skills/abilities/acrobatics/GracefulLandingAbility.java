package com.orbis.skills.abilities.acrobatics;

import com.orbis.skills.abilities.Ability;



public class GracefulLandingAbility extends Ability {

    

    public GracefulLandingAbility(int unlockLevel) {
        super("gracefullanding", unlockLevel, "Gain experience based on fall damage survived");

       

        setLevelEffect(unlockLevel, 1.25);

        setLevelEffect(unlockLevel + 10, 1.50);

        setLevelEffect(unlockLevel + 20, 1.75);

        setLevelEffect(unlockLevel + 30, 2.00);

        setLevelEffect(unlockLevel + 40, 2.25);

        setLevelEffect(unlockLevel + 50, 2.50);

    }

    @Override
    public String getInfoForLevel(int level) {
        if (level < getUnlockLevel()) {
            return "Locked (Unlocks at level " + getUnlockLevel() + ")";
        }

        double effect = getEffectForLevel(level);
        return String.format("%.2fx experience from fall damage", effect);
    }
}