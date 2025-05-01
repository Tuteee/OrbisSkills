package com.orbis.skills.abilities.woodcutting;

import com.orbis.skills.abilities.Ability;



public class TreeFellerAbility extends Ability {

    

    public TreeFellerAbility(int unlockLevel) {
        super("treefeller", unlockLevel, "Cut down entire trees at once when sneaking");

       

        setLevelEffect(unlockLevel, 1.0);

    }

    @Override
    public String getInfoForLevel(int level) {
        if (level < getUnlockLevel()) {
            return "Locked (Unlocks at level " + getUnlockLevel() + ")";
        }

        int maxLogs = 10 + (level / 5);

        maxLogs = Math.min(maxLogs, 30);

        return String.format("Cut up to %d connected logs (60s cooldown)", maxLogs);
    }
}