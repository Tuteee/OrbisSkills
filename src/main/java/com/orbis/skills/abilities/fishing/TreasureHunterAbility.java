package com.orbis.skills.abilities.fishing;

import com.orbis.skills.abilities.Ability;



public class TreasureHunterAbility extends Ability {

    

    public TreasureHunterAbility(int unlockLevel) {
        super("treasurehunter", unlockLevel, "Increased chance to find rare treasures while fishing");

       

        setLevelEffect(unlockLevel, 0.10);

        setLevelEffect(unlockLevel + 10, 0.20);

        setLevelEffect(unlockLevel + 20, 0.30);

        setLevelEffect(unlockLevel + 30, 0.50);

        setLevelEffect(unlockLevel + 40, 0.75);

        setLevelEffect(unlockLevel + 50, 1.00);

    }

    @Override
    public String getInfoForLevel(int level) {
        if (level < getUnlockLevel()) {
            return "Locked (Unlocks at level " + getUnlockLevel() + ")";
        }

        double effect = getEffectForLevel(level);
        return String.format("+%.0f%% treasure chance", effect * 100);
    }
}