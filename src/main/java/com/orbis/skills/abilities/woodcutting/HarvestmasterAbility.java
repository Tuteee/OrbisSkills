package com.orbis.skills.abilities.woodcutting;

import com.orbis.skills.abilities.Ability;

/**
 * Ability that gives a chance for extra saplings from leaves
 */
public class HarvestmasterAbility extends Ability {

    /**
     * Create a new harvestmaster ability
     * @param unlockLevel the level required to unlock this ability
     */
    public HarvestmasterAbility(int unlockLevel) {
        super("harvestmaster", unlockLevel, "Increased chance for saplings from leaves");

        // Set level effects (chance multiplier increases with level)
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
        return String.format("%.2fx chance for saplings from leaves", effect);
    }
}