package com.orbis.skills.abilities.farming;

import com.orbis.skills.abilities.Ability;

/**
 * Ability that gives a chance for instant crop growth
 */
public class NaturesBlessingAbility extends Ability {

    /**
     * Create a new nature's blessing ability
     * @param unlockLevel the level required to unlock this ability
     */
    public NaturesBlessingAbility(int unlockLevel) {
        super("naturesblessing", unlockLevel, "Chance for instant crop growth when planting");

        // Set level effects (chance increases with level)
        setLevelEffect(unlockLevel, 0.05); // 5% at unlock level
        setLevelEffect(unlockLevel + 10, 0.10); // 10% at unlock+10
        setLevelEffect(unlockLevel + 20, 0.15); // 15% at unlock+20
        setLevelEffect(unlockLevel + 30, 0.20); // 20% at unlock+30
        setLevelEffect(unlockLevel + 40, 0.25); // 25% at unlock+40
        setLevelEffect(unlockLevel + 50, 0.30); // 30% at unlock+50
    }

    @Override
    public String getInfoForLevel(int level) {
        if (level < getUnlockLevel()) {
            return "Locked (Unlocks at level " + getUnlockLevel() + ")";
        }

        double effect = getEffectForLevel(level);
        return String.format("%.1f%% chance for instant crop growth", effect * 100);
    }
}