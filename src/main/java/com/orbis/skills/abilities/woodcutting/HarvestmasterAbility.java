package com.orbis.skills.abilities.woodcutting;

import com.orbis.skills.abilities.Ability;



public class HarvestmasterAbility extends Ability {

    

    public HarvestmasterAbility(int unlockLevel) {
        super("harvestmaster", unlockLevel, "Increased chance for saplings from leaves");

       

        setLevelEffect(unlockLevel, 1.25);

        setLevelEffect(unlockLevel + 10, 1.50);

        setLevelEffect(unlockLevel + 20, 1.75);

        setLevelEffect(unlockLevel + 30, 2.00);

        setLevelEffect(unlockLevel + 40, 2.25);

        setLevelEffect(unlockLevel + 50, 2.50);

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