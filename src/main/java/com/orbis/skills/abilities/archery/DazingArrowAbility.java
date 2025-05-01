package com.orbis.skills.abilities.archery;

import com.orbis.skills.abilities.Ability;



public class DazingArrowAbility extends Ability {

    

    public DazingArrowAbility(int unlockLevel) {
        super("dazingarrow", unlockLevel, "Chance to apply confusion effect to targets");

       

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
        int duration = 5 + (level / 10);

        return String.format("%.1f%% chance to apply confusion for %d seconds", effect * 100, duration);
    }
}
