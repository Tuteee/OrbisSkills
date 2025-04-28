package com.orbis.skills.skills;

import com.orbis.skills.OrbisSkills;
import com.orbis.skills.abilities.Ability;
import com.orbis.skills.events.SkillLevelUpEvent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class Skill {

    protected final OrbisSkills plugin;
    protected final String name;
    protected final String displayName;
    protected final Map<String, Ability> abilities = new HashMap<>();

    public Skill(OrbisSkills plugin, String name) {
        this.plugin = plugin;
        this.name = name;
        this.displayName = plugin.getConfig().getString("skills.display-names." + name,
                name.substring(0, 1).toUpperCase() + name.substring(1));

        // Register abilities
        registerAbilities();
    }

    /**
     * Register all abilities for this skill
     */
    protected abstract void registerAbilities();

    /**
     * Get the name of the skill
     * @return the name of the skill
     */
    public String getName() {
        return name;
    }

    /**
     * Get the display name of the skill
     * @return the display name of the skill
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Add experience to the player for this skill
     * @param player the player
     * @param amount the amount of experience
     */
    public void addExperience(Player player, double amount) {
        UUID uuid = player.getUniqueId();
        int oldLevel = plugin.getDataManager().getPlayerData(uuid).getSkillLevel(name);

        // Apply configured multiplier
        double multiplier = plugin.getConfig().getDouble("settings.exp-multiplier", 1.0);
        // Check for permission-based multipliers
        if (player.hasPermission("orbisskills." + name + ".multiplier.2")) {
            multiplier = 2.0;
        } else if (player.hasPermission("orbisskills." + name + ".multiplier.1.5")) {
            multiplier = 1.5;
        }

        // Add experience
        plugin.getDataManager().addExperience(uuid, name, amount * multiplier);

        // Check for level up
        int newLevel = plugin.getDataManager().getPlayerData(uuid).getSkillLevel(name);
        if (newLevel > oldLevel) {
            handleLevelUp(player, oldLevel, newLevel);
        }
    }

    /**
     * Handle player leveling up
     * @param player the player
     * @param oldLevel the old level
     * @param newLevel the new level
     */
    protected void handleLevelUp(Player player, int oldLevel, int newLevel) {
        // Fire level up event
        SkillLevelUpEvent event = new SkillLevelUpEvent(player, this, oldLevel, newLevel);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return;
        }

        // Display level up message
        if (plugin.getConfig().getBoolean("settings.level-up-messages", true)) {
            player.sendMessage(plugin.getConfigManager().getColoredString("messages.level-up")
                    .replace("{skill}", displayName)
                    .replace("{level}", String.valueOf(newLevel)));
        }

        // Show level up title
        if (plugin.getConfig().getBoolean("settings.level-up-titles", true)) {
            String title = plugin.getConfigManager().getColoredString("messages.level-up-title")
                    .replace("{skill}", displayName)
                    .replace("{level}", String.valueOf(newLevel));

            String subtitle = plugin.getConfigManager().getColoredString("messages.level-up-subtitle")
                    .replace("{skill}", displayName)
                    .replace("{level}", String.valueOf(newLevel));

            player.sendTitle(title, subtitle, 10, 70, 20);
        }

        // Play level up sound
        if (plugin.getConfig().getBoolean("settings.level-up-sounds", true)) {
            String soundName = plugin.getConfig().getString("sounds.level-up", "ENTITY_PLAYER_LEVELUP");
            try {
                Sound sound = Sound.valueOf(soundName);
                player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid sound name in config: " + soundName);
            }
        }

        // Check for ability unlocks at this level
        checkAbilityUnlocks(player, newLevel);
    }

    /**
     * Check for ability unlocks at the given level
     * @param player the player
     * @param level the level
     */
    private void checkAbilityUnlocks(Player player, int level) {
        for (Ability ability : abilities.values()) {
            if (ability.getUnlockLevel() == level) {
                player.sendMessage(plugin.getConfigManager().getColoredString("messages.ability-unlock")
                        .replace("{ability}", ability.getName())
                        .replace("{skill}", displayName));
            }
        }
    }

    /**
     * Check if this skill has an ability
     * @param abilityName the ability name
     * @return true if the skill has the ability
     */
    public boolean hasAbility(String abilityName) {
        return abilities.containsKey(abilityName.toLowerCase());
    }

    /**
     * Get an ability by name
     * @param abilityName the ability name
     * @return the ability, or null if not found
     */
    public Ability getAbility(String abilityName) {
        return abilities.get(abilityName.toLowerCase());
    }

    /**
     * Get ability info for a player
     * @param player the player
     * @param abilityName the ability name
     * @return the ability info
     */
    public String getAbilityInfo(Player player, String abilityName) {
        if (!hasAbility(abilityName)) {
            return "0";
        }

        Ability ability = abilities.get(abilityName.toLowerCase());
        int playerLevel = plugin.getDataManager().getPlayerData(player.getUniqueId()).getSkillLevel(name);

        if (playerLevel < ability.getUnlockLevel()) {
            return "Locked";
        }

        return ability.getInfoForLevel(playerLevel);
    }

    /**
     * Register an ability
     * @param ability the ability
     */
    protected void registerAbility(Ability ability) {
        abilities.put(ability.getName().toLowerCase(), ability);
    }

    /**
     * Load an ability from a configuration section
     * @param section the configuration section
     * @return the ability
     */
    protected Ability loadAbilityFromConfig(ConfigurationSection section) {
        if (section == null) {
            return null;
        }

        String abilityName = section.getName();
        int unlockLevel = section.getInt("unlock-level", 1);
        String description = section.getString("description", "No description available");

        // Create the ability (this would be extended in real implementation)
        return new Ability(abilityName, unlockLevel, description);
    }
}