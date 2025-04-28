package com.orbis.skills.abilities.fencing;

import com.orbis.skills.abilities.Ability;

/**
 * Ability that applies bonus damage when striking back after being hit
 */
public class CounterAttackAbility extends Ability {

    /**
     * Create a new counter attack ability
     * @param unlockLevel the level required to unlock this ability
     */
    public CounterAttackAbility(int unlockLevel) {
        super("counterattack", unlockLevel, "Deal bonus damage when striking back at an attacker");

        // No level effects - cooldown based ability
        setLevelEffect(unlockLevel, 1.0); // Always active when conditions are met
    }

    @Override
    public String getInfoForLevel(int level) {
        if (level < getUnlockLevel()) {
            return "Locked (Unlocks at level " + getUnlockLevel() + ")";
        }

        double bonusDamage = 4.0 + (level / 10.0); // 4-14 bonus damage
        return String.format("Deal %.1f bonus damage to attackers (30s cooldown)", bonusDamage);
    }
}