package com.orbis.skills.abilities.fencing;

import com.orbis.skills.abilities.Ability;

/**
 * Ability that gives a chance to reduce incoming damage
 */
public class ParryAbility extends Ability {

    /**
     * Create a new parry ability
     * @param unlockLevel the level required to unlock this ability
     */
    public ParryAbility(int unlockLevel) {
        super("parry", unlockLevel, "Chance to reduce incoming damage");

        // Set level effects (damage reduction increases with level)
        setLevelEffect(unlockLevel, 0.30); // 30% at unlock level
        setLevelEffect(unlockLevel + 10, 0.40); // 40% at unlock+10
        setLevelEffect(unlockLevel + 20, 0.50); // 50% at unlock+20
        setLevelEffect(unlockLevel + 30, 0.60); // 60% at unlock+30
        setLevelEffect(unlockLevel + 40, 0.70); // 70% at unlock+40
        setLevelEffect(unlockLevel + 50, 0.80); // 80% at unlock+50
    }

    @Override
    public String getInfoForLevel(int level) {
        if (level < getUnlockLevel()) {
            return "Locked (Unlocks at level " + getUnlockLevel() + ")";
        }

        double effect = getEffectForLevel(level);
        return String.format("%.1f%% chance to reduce damage by %.0f%%",
                Math.min(level / 2.0, 30.0), // Chance: 15-30% based on level
                effect * 100); // Reduction percentage
    }
}