package com.orbis.skills.abilities.archery;

import com.orbis.skills.abilities.Ability;



public class BullseyeAbility extends Ability {

    

    public BullseyeAbility(int unlockLevel) {
        super("bullseye", unlockLevel, "Chance to deal 50% extra damage with arrows");

       

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
        return String.format("%.1f%% chance for 50%% extra damage", effect * 100);
    }
}