package com.orbis.skills.abilities.acrobatics;

import com.orbis.skills.abilities.Ability;

/**
 * Ability that gives a chance to reduce fall damage
 */
public class RollAbility extends Ability {

    /**
     * Create a new roll ability
     * @param unlockLevel the level required to unlock this ability
     */
    public RollAbility(int unlockLevel) {
        super("roll", unlockLevel, "Chance to reduce fall damage by 50%");

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
        return String.format("%.1f%% chance to reduce fall damage by 50%%", effect * 100);
    }
}