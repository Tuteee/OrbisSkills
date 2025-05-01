package com.orbis.skills.abilities.acrobatics;

import com.orbis.skills.abilities.Ability;



public class RollAbility extends Ability {

    

    public RollAbility(int unlockLevel) {
        super("roll", unlockLevel, "Chance to reduce fall damage by 50%");

       

        setLevelEffect(unlockLevel, 0.10);

        setLevelEffect(unlockLevel + 10, 0.20);

        setLevelEffect(unlockLevel + 20, 0.30);

        setLevelEffect(unlockLevel + 30, 0.40);

        setLevelEffect(unlockLevel + 40, 0.50);

        setLevelEffect(unlockLevel + 50, 0.60);

    }

    @Override
    public String getInfoForLevel(int level) {
        if (level < getUnlockLevel()) {
            return "Locked (Unlocks at level " + getUnlockLevel() + ")";
        }

        double effect = getEffectForLevel(level);
        return String.format("%.1f%% chance to reduce fall damage by 50%%", effect * 100);
    }
}