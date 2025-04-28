package com.orbis.skills.abilities.acrobatics;

import com.orbis.skills.abilities.Ability;

/**
 * Ability that grants experience when taking fall damage
 */
public class GracefulLandingAbility extends Ability {

    /**
     * Create a new graceful landing ability
     * @param unlockLevel the level required to unlock this ability
     */
    public GracefulLandingAbility(int unlockLevel) {
        super("gracefullanding", unlockLevel, "Gain experience based on fall damage survived");

        // Set level effects (experience multiplier increases with level)
        setLevelEffect(unlockLevel, 1.25); // 1.25x at unlock level
        setLevelEffect(unlockLevel + 10, 1.50); // 1.5x at unlock+10
        setLevelEffect(unlockLevel + 20, 1.75); // 1.75x at unlock+20
        setLevelEffect(unlockLevel + 30, 2.00); // 2.0x at unlock+30
        setLevelEffect(unlockLevel + 40, 2.25); // 2.25x at unlock+40
        setLevelEffect(unlockLevel + 50, 2.50); // 2.5x at unlock+50
    }

    @Override
    public String getInfoForLevel(int level) {
        if (level < getUnlockLevel()) {
            return "Locked (Unlocks at level " + getUnlockLevel() + ")";
        }

        double effect = getEffectForLevel(level);
        return String.format("%.2fx experience from fall damage", effect);
    }
}