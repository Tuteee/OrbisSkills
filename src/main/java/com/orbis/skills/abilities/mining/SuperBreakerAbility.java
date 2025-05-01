package com.orbis.skills.abilities.mining;

import com.orbis.skills.abilities.Ability;



public class SuperBreakerAbility extends Ability {

    

    public SuperBreakerAbility(int unlockLevel) {
        super("superbreaker", unlockLevel, "Gain temporary haste effect when mining while sneaking");

        setLevelEffect(unlockLevel, 1.0);
    }

    @Override
    public String getInfoForLevel(int level) {
        if (level < getUnlockLevel()) {
            return "Locked (Unlocks at level " + getUnlockLevel() + ")";
        }

        int effectLevel = Math.min(level / 20, 2);
        return String.format("Haste %d for 30 seconds (3m cooldown)", effectLevel + 1);
    }
}