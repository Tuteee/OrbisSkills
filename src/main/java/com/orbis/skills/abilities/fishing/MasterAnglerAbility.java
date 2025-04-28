package com.orbis.skills.abilities.fishing;

import com.orbis.skills.abilities.Ability;

/**
 * Ability that reduces fishing time
 */
public class MasterAnglerAbility extends Ability {

    /**
     * Create a new master angler ability
     * @param unlockLevel the level required to unlock this ability
     */
    public MasterAnglerAbility(int unlockLevel) {
        super("masterangler", unlockLevel, "Chance for instant catches while fishing");

        // Set level effects (chance increases with level)
        setLevelEffect(unlockLevel, 0.05); // 5% at unlock level
        setLevelEffect(unlockLevel + 10, 0.10); // 10% at unlock+10
        setLevelEffect(unlockLevel + 20, 0.15); // 15% at unlock+20
        setLevelEffect(unlockLevel + 30, 0.20); // 20% at unlock+30
        setLevelEffect(unlockLevel + 40, 0.25); // 25% at unlock+40
        setLevelEffect(unlockLevel + 50, 0.33); // 33% at unlock+50
    }

    @Override
    public String getInfoForLevel(int level) {
        if (level < getUnlockLevel()) {
            return "Locked (Unlocks at level " + getUnlockLevel() + ")";
        }

        double effect = getEffectForLevel(level);
        return String.format("%.1f%% chance for instant catches", effect * 100);
    }
}