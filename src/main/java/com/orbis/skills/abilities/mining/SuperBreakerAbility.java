package com.orbis.skills.abilities.mining;

import com.orbis.skills.abilities.Ability;

/**
 * Ability that grants a haste effect when mining
 */
public class SuperBreakerAbility extends Ability {

    /**
     * Create a new super breaker ability
     * @param unlockLevel the level required to unlock this ability
     */
    public SuperBreakerAbility(int unlockLevel) {
        super("superbreaker", unlockLevel, "Gain temporary haste effect when mining while sneaking");

        // No level effects - cooldown based ability
        setLevelEffect(unlockLevel, 1.0); // Always active when sneaking
    }

    @Override
    public String getInfoForLevel(int level) {
        if (level < getUnlockLevel()) {
            return "Locked (Unlocks at level " + getUnlockLevel() + ")";
        }

        int effectLevel = Math.min(level / 20, 2); // Up to Haste III at level 60+
        return String.format("Haste %d for 30 seconds (3m cooldown)", effectLevel + 1);
    }
}