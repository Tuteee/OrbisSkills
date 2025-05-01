package com.orbis.skills.abilities.fishing;

import com.orbis.skills.abilities.Ability;



public class ExperiencedFisherAbility extends Ability {

    

    public ExperiencedFisherAbility(int unlockLevel) {
        super("experiencedfisher", unlockLevel, "Gain additional experience from fishing");

       

        setLevelEffect(unlockLevel, 0.10);

        setLevelEffect(unlockLevel + 10, 0.20);

        setLevelEffect(unlockLevel + 20, 0.30);

        setLevelEffect(unlockLevel + 30, 0.40);

        setLevelEffect(unlockLevel + 40, 0.50);

        setLevelEffect(unlockLevel + 50, 0.75);

    }

    @Override
    public String getInfoForLevel(int level) {
        if (level < getUnlockLevel()) {
            return "Locked (Unlocks at level " + getUnlockLevel() + ")";
        }

        double effect = getEffectForLevel(level);
        return String.format("+%.0f%% fishing experience", effect * 100);
    }
}