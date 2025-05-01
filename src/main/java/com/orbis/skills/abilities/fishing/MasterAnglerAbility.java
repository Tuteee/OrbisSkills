package com.orbis.skills.abilities.fishing;

import com.orbis.skills.abilities.Ability;



public class MasterAnglerAbility extends Ability {

    

    public MasterAnglerAbility(int unlockLevel) {
        super("masterangler", unlockLevel, "Chance for instant catches while fishing");

       

        setLevelEffect(unlockLevel, 0.05);

        setLevelEffect(unlockLevel + 10, 0.10);

        setLevelEffect(unlockLevel + 20, 0.15);

        setLevelEffect(unlockLevel + 30, 0.20);

        setLevelEffect(unlockLevel + 40, 0.25);

        setLevelEffect(unlockLevel + 50, 0.33);

    }

    @Override
    public String getInfoForLevel(int level) {
        if (level < getUnlockLevel()) {
            return "Locked (Unlocks at level " + getUnlockLevel() + ")";
        }

        double effect = getEffectForLevel(level);
        return String.format("%.1f%% chance for instant catches", effect * 100);
    }
}