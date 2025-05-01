package com.orbis.skills.abilities.mining;

import com.orbis.skills.abilities.Ability;



public class VeinMinerAbility extends Ability {

    

    public VeinMinerAbility(int unlockLevel) {
        super("veinminer", unlockLevel, "Mine entire veins of ore at once when sneaking");

       

        setLevelEffect(unlockLevel, 1.0);

    }

    @Override
    public String getInfoForLevel(int level) {
        if (level < getUnlockLevel()) {
            return "Locked (Unlocks at level " + getUnlockLevel() + ")";
        }

        int maxOres = 5 + (level / 10);

        maxOres = Math.min(maxOres, 15);

        return String.format("Mine up to %d connected ores (2m cooldown)", maxOres);
    }
}