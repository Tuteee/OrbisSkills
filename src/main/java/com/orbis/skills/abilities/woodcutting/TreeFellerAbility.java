package com.orbis.skills.abilities.woodcutting;

import com.orbis.skills.abilities.Ability;

/**
 * Ability that cuts down entire trees at once
 */
public class TreeFellerAbility extends Ability {

    /**
     * Create a new tree feller ability
     * @param unlockLevel the level required to unlock this ability
     */
    public TreeFellerAbility(int unlockLevel) {
        super("treefeller", unlockLevel, "Cut down entire trees at once when sneaking");

        // No level effects - cooldown based ability
        setLevelEffect(unlockLevel, 1.0); // Always active when sneaking
    }

    @Override
    public String getInfoForLevel(int level) {
        if (level < getUnlockLevel()) {
            return "Locked (Unlocks at level " + getUnlockLevel() + ")";
        }

        int maxLogs = 10 + (level / 5); // 10-30 logs based on level
        maxLogs = Math.min(maxLogs, 30);

        return String.format("Cut up to %d connected logs (60s cooldown)", maxLogs);
    }
}