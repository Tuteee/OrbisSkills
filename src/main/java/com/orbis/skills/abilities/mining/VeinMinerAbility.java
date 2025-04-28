package com.orbis.skills.abilities.mining;

import com.orbis.skills.abilities.Ability;

/**
 * Ability that mines connected ores
 */
public class VeinMinerAbility extends Ability {

    /**
     * Create a new vein miner ability
     * @param unlockLevel the level required to unlock this ability
     */
    public VeinMinerAbility(int unlockLevel) {
        super("veinminer", unlockLevel, "Mine entire veins of ore at once when sneaking");

        // No level effects - cooldown based ability
        setLevelEffect(unlockLevel, 1.0); // Always active when sneaking
    }

    @Override
    public String getInfoForLevel(int level) {
        if (level < getUnlockLevel()) {
            return "Locked (Unlocks at level " + getUnlockLevel() + ")";
        }

        int maxOres = 5 + (level / 10); // 5-15 ores based on level
        maxOres = Math.min(maxOres, 15);

        return String.format("Mine up to %d connected ores (2m cooldown)", maxOres);
    }
}