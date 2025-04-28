package com.orbis.skills.abilities.farming;

import com.orbis.skills.abilities.Ability;

/**
 * Ability that gives a chance for additional crop drops
 */
public class BountifulHarvestAbility extends Ability {

    /**
     * Create a new bountiful harvest ability
     * @param unlockLevel the level required to unlock this ability
     */
    public BountifulHarvestAbility(int unlockLevel) {
        super("bountifulharvest", unlockLevel, "Chance for additional drops when harvesting crops");

        // Set level effects (chance increases with level)
        setLevelEffect(unlockLevel, 0.10); // 10% at unlock level
        setLevelEffect(unlockLevel + 10, 0.20); // 20% at unlock+10
        setLevelEffect(unlockLevel + 20, 0.30); // 30% at unlock+20
        setLevelEffect(unlockLevel + 30, 0.40); // 40% at unlock+30
        setLevelEffect(unlockLevel + 40, 0.50); // 50% at unlock+40
        setLevelEffect(unlockLevel + 50, 0.60); // 60% at unlock+50
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