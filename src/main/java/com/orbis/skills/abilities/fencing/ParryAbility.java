package com.orbis.skills.abilities.fencing;

import com.orbis.skills.abilities.Ability;



public class ParryAbility extends Ability {

    

    public ParryAbility(int unlockLevel) {
        super("parry", unlockLevel, "Chance to reduce incoming damage");

       

        setLevelEffect(unlockLevel, 0.30);

        setLevelEffect(unlockLevel + 10, 0.40);

        setLevelEffect(unlockLevel + 20, 0.50);

        setLevelEffect(unlockLevel + 30, 0.60);

        setLevelEffect(unlockLevel + 40, 0.70);

        setLevelEffect(unlockLevel + 50, 0.80);

    }

    @Override
    public String getInfoForLevel(int level) {
        if (level < getUnlockLevel()) {
            return "Locked (Unlocks at level " + getUnlockLevel() + ")";
        }

        double effect = getEffectForLevel(level);
        return String.format("%.1f%% chance to reduce damage by %.0f%%",
                Math.min(level / 2.0, 30.0),

                effect * 100);

    }
}