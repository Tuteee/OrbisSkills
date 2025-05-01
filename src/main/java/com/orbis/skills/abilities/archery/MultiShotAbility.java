package com.orbis.skills.abilities.archery;

import com.orbis.skills.abilities.Ability;



public class MultiShotAbility extends Ability {

    

    public MultiShotAbility(int unlockLevel) {
        super("multishot", unlockLevel, "Fire multiple arrows at once while sneaking");

       

        setLevelEffect(unlockLevel, 1.0);

    }

    @Override
    public String getInfoForLevel(int level) {
        if (level < getUnlockLevel()) {
            return "Locked (Unlocks at level " + getUnlockLevel() + ")";
        }

        int arrowCount = 2 + (level / 25);

        arrowCount = Math.min(arrowCount, 6);

        return String.format("Fire %d arrows at once (30s cooldown)", arrowCount);
    }
}