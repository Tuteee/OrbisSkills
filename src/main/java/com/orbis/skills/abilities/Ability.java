package com.orbis.skills.abilities;

import com.orbis.skills.OrbisSkills;
import com.orbis.skills.events.AbilityUseEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Ability {

    private final String name;
    private final int unlockLevel;
    private final String description;
    private final Map<Integer, Double> levelEffects;
    private final Map<UUID, Long> cooldowns;

    

    public Ability(String name, int unlockLevel, String description) {
        this.name = name;
        this.unlockLevel = unlockLevel;
        this.description = description;
        this.levelEffects = new HashMap<>();
        this.cooldowns = new ConcurrentHashMap<>();

       

        for (int i = unlockLevel; i <= 100; i += 10) {
            levelEffects.put(i, (double) (i - unlockLevel + 10) / 100);
        }
    }

    

    public String getName() {
        return name;
    }

    

    public int getUnlockLevel() {
        return unlockLevel;
    }

    

    public String getDescription() {
        return description;
    }

    

    public double getEffectForLevel(int level) {
       

        int closestLevel = unlockLevel;
        for (int definedLevel : levelEffects.keySet()) {
            if (definedLevel <= level && definedLevel > closestLevel) {
                closestLevel = definedLevel;
            }
        }

        return levelEffects.getOrDefault(closestLevel, 0.0);
    }

    

    public String getInfoForLevel(int level) {
        if (level < unlockLevel) {
            return "Locked (Unlocks at level " + unlockLevel + ")";
        }

        double effect = getEffectForLevel(level);
        return String.format("%.1f%%", effect * 100);
    }

    

    public void setLevelEffect(int level, double value) {
        levelEffects.put(level, value);
    }

    

    public boolean canUse(Player player, OrbisSkills plugin) {
        UUID uuid = player.getUniqueId();

       

        if (cooldowns.containsKey(uuid)) {
            long cooldownTime = cooldowns.get(uuid);
            if (System.currentTimeMillis() < cooldownTime) {
                return false;
            }
        }

       

        int playerLevel = plugin.getDataManager().getPlayerData(uuid).getSkillLevel(getName());
        return playerLevel >= unlockLevel;
    }

    

    public boolean trigger(Player player, OrbisSkills plugin, int cooldownSeconds) {
        if (!canUse(player, plugin)) {
            return false;
        }

       

        AbilityUseEvent event = new AbilityUseEvent(player, this);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return false;
        }

       

        if (cooldownSeconds > 0) {
            cooldowns.put(player.getUniqueId(),
                    System.currentTimeMillis() + (cooldownSeconds * 1000L));
        }

        return true;
    }

    

    public int getRemainingCooldown(Player player) {
        UUID uuid = player.getUniqueId();
        if (!cooldowns.containsKey(uuid)) {
            return 0;
        }

        long cooldownTime = cooldowns.get(uuid);
        long remainingMillis = cooldownTime - System.currentTimeMillis();

        if (remainingMillis <= 0) {
            cooldowns.remove(uuid);
            return 0;
        }

        return (int) (remainingMillis / 1000);
    }
}