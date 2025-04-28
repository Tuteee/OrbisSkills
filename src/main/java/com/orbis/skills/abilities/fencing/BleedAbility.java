package com.orbis.skills.abilities.fencing;

import com.orbis.skills.abilities.Ability;

/**
 * Ability that gives a chance to apply bleeding effect
 */
public class BleedAbility extends Ability {

    /**
     * Create a new bleed ability
     * @param unlockLevel the level required to unlock this ability
     */
    public BleedAbility(int unlockLevel) {
        super("bleed", unlockLevel, "Chance to apply a bleeding effect to enemies");

        // Set level effects (chance increases with level)
        setLevelEffect(unlockLevel, 0.10); // 10% at unlock level
        setLevelEffect(unlockLevel + 10, 0.15); // 15% at unlock+10
        setLevelEffect(unlockLevel + 20, 0.20); // 20% at unlock+20
        setLevelEffect(unlockLevel + 30, 0.25); // 25% at unlock+30
        setLevelEffect(unlockLevel + 40, 0.30); // 30% at unlock+40
        setLevelEffect(unlockLevel + 50, 0.35); // 35% at unlock+50
    }

    @Override
    public String getInfoForLevel(int level) {
        if (level < getUnlockLevel()) {
            return "Locked (Unlocks at level " + getUnlockLevel() + ")";
        }

        double effect = getEffectForLevel(level);
        int duration = 3 + (level / 20); // 3-8 seconds based on level
        return String.format("%.1f%% chance to cause bleeding for %d seconds", effect * 100, duration);
    }
}