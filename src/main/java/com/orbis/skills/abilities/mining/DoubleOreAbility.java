package com.orbis.skills.abilities.mining;

import com.orbis.skills.abilities.Ability;



public class DoubleOreAbility extends Ability {

    

    public DoubleOreAbility(int unlockLevel) {
        super("doubleore", unlockLevel, "Chance to get double drops when mining ores");

       

        setLevelEffect(unlockLevel, 0.05);

        setLevelEffect(unlockLevel + 10, 0.10);

        setLevelEffect(unlockLevel + 20, 0.15);

        setLevelEffect(unlockLevel + 30, 0.20);

        setLevelEffect(unlockLevel + 40, 0.25);

        setLevelEffect(unlockLevel + 50, 0.30);

    }

    @Override
    public String getInfoForLevel(int level) {
        if (level < getUnlockLevel()) {
            return "Locked (Unlocks at level " + getUnlockLevel() + ")";
        }

        double effect = getEffectForLevel(level);
        return String.format("%.1f%% chance for double ore drops", effect * 100);
    }
}