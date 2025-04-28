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

    /**
     * Create a new ability
     * @param name the name of the ability
     * @param unlockLevel the level required to unlock
     * @param description the description
     */
    public Ability(String name, int unlockLevel, String description) {
        this.name = name;
        this.unlockLevel = unlockLevel;
        this.description = description;
        this.levelEffects = new HashMap<>();
        this.cooldowns = new ConcurrentHashMap<>();

        // Default level effects (could be loaded from config)
        for (int i = unlockLevel; i <= 100; i += 10) {
            levelEffects.put(i, (double) (i - unlockLevel + 10) / 100);
        }
    }

    /**
     * Get the name of the ability
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the level required to unlock the ability
     * @return the unlock level
     */
    public int getUnlockLevel() {
        return unlockLevel;
    }

    /**
     * Get the description of the ability
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get the effect value for a specific level
     * @param level the level
     * @return the effect value
     */
    public double getEffectForLevel(int level) {
        // Find the closest level effect defined
        int closestLevel = unlockLevel;
        for (int definedLevel : levelEffects.keySet()) {
            if (definedLevel <= level && definedLevel > closestLevel) {
                closestLevel = definedLevel;
            }
        }

        return levelEffects.getOrDefault(closestLevel, 0.0);
    }

    /**
     * Get the info string for a specific level
     * @param level the level
     * @return the info string
     */
    public String getInfoForLevel(int level) {
        if (level < unlockLevel) {
            return "Locked (Unlocks at level " + unlockLevel + ")";
        }

        double effect = getEffectForLevel(level);
        return String.format("%.1f%%", effect * 100);
    }

    /**
     * Set the effect value for a specific level
     * @param level the level
     * @param value the effect value
     */
    public void setLevelEffect(int level, double value) {
        levelEffects.put(level, value);
    }

    /**
     * Check if the player can use this ability
     * @param player the player
     * @param plugin the plugin instance
     * @return true if the player can use the ability
     */
    public boolean canUse(Player player, OrbisSkills plugin) {
        UUID uuid = player.getUniqueId();

        // Check cooldown
        if (cooldowns.containsKey(uuid)) {
            long cooldownTime = cooldowns.get(uuid);
            if (System.currentTimeMillis() < cooldownTime) {
                return false;
            }
        }

        // Check level
        int playerLevel = plugin.getDataManager().getPlayerData(uuid).getSkillLevel(getName());
        return playerLevel >= unlockLevel;
    }

    /**
     * Trigger the ability
     * @param player the player
     * @param plugin the plugin instance
     * @param cooldownSeconds the cooldown in seconds
     * @return true if the ability was triggered successfully
     */
    public boolean trigger(Player player, OrbisSkills plugin, int cooldownSeconds) {
        if (!canUse(player, plugin)) {
            return false;
        }

        // Fire ability use event
        AbilityUseEvent event = new AbilityUseEvent(player, this);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return false;
        }

        // Apply cooldown
        if (cooldownSeconds > 0) {
            cooldowns.put(player.getUniqueId(),
                    System.currentTimeMillis() + (cooldownSeconds * 1000L));
        }

        return true;
    }

    /**
     * Get the remaining cooldown in seconds
     * @param player the player
     * @return the remaining cooldown in seconds, or 0 if no cooldown
     */
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