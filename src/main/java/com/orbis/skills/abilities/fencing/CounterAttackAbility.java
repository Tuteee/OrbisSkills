package com.orbis.skills.abilities.fencing;

import com.orbis.skills.abilities.Ability;



public class CounterAttackAbility extends Ability {

    

    public CounterAttackAbility(int unlockLevel) {
        super("counterattack", unlockLevel, "Deal bonus damage when striking back at an attacker");

       

        setLevelEffect(unlockLevel, 1.0);

    }

    @Override
    public String getInfoForLevel(int level) {
        if (level < getUnlockLevel()) {
            return "Locked (Unlocks at level " + getUnlockLevel() + ")";
        }

        double bonusDamage = 4.0 + (level / 10.0);

        return String.format("Deal %.1f bonus damage to attackers (30s cooldown)", bonusDamage);
    }
}