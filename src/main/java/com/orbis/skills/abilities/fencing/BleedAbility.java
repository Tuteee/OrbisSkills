package com.orbis.skills.abilities.fencing;

import com.orbis.skills.abilities.Ability;



public class BleedAbility extends Ability {

    

    public BleedAbility(int unlockLevel) {
        super("bleed", unlockLevel, "Chance to apply a bleeding effect to enemies");

       

        setLevelEffect(unlockLevel, 0.10);

        setLevelEffect(unlockLevel + 10, 0.15);

        setLevelEffect(unlockLevel + 20, 0.20);

        setLevelEffect(unlockLevel + 30, 0.25);

        setLevelEffect(unlockLevel + 40, 0.30);

        setLevelEffect(unlockLevel + 50, 0.35);

    }

    @Override
    public String getInfoForLevel(int level) {
        if (level < getUnlockLevel()) {
            return "Locked (Unlocks at level " + getUnlockLevel() + ")";
        }

        double effect = getEffectForLevel(level);
        int duration = 3 + (level / 20);

        return String.format("%.1f%% chance to cause bleeding for %d seconds", effect * 100, duration);
    }
}