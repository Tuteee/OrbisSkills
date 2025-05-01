package com.orbis.skills.abilities.farming;

import com.orbis.skills.abilities.Ability;



public class BountifulHarvestAbility extends Ability {

    

    public BountifulHarvestAbility(int unlockLevel) {
        super("bountifulharvest", unlockLevel, "Chance for additional drops when harvesting crops");

       

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
        return String.format("%.1f%% chance for bonus crop drops", effect * 100);
    }
}