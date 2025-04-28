package com.orbis.skills.abilities.archery;

import com.orbis.skills.abilities.Ability;

/**
 * Ability that gives a chance to fire multiple arrows
 */
public class MultiShotAbility extends Ability {

    /**
     * Create a new multi shot ability
     * @param unlockLevel the level required to unlock this ability
     */
    public MultiShotAbility(int unlockLevel) {
        super("multishot", unlockLevel, "Fire multiple arrows at once while sneaking");

        // No level effects - cooldown based ability
        setLevelEffect(unlockLevel, 1.0); // Always active when sneaking
    }

    @Override
    public String getInfoForLevel(int level) {
        if (level < getUnlockLevel()) {
            return "Locked (Unlocks at level " + getUnlockLevel() + ")";
        }

        int arrowCount = 2 + (level / 25); // 2-6 arrows based on level
        arrowCount = Math.min(arrowCount, 6);

        return String.format("Fire %d arrows at once (30s cooldown)", arrowCount);
    }
}