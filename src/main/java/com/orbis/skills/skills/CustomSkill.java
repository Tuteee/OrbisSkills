package com.orbis.skills.skills;

import com.orbis.skills.OrbisSkills;
import com.orbis.skills.abilities.Ability;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Represents a custom skill that can be defined in configuration
 */
public class CustomSkill extends Skill {

    private final List<String> sources = new ArrayList<>();
    private final List<String> triggers = new ArrayList<>();
    private final List<String> triggerMaterials = new ArrayList<>();
    private final double baseExpValue;
    private final Map<String, CustomAbilityInfo> customAbilityInfo = new HashMap<>();
    private final Random random = new Random();

    /**
     * Create a new custom skill
     * @param plugin the plugin instance
     * @param name the skill name
     * @param config the configuration section for this skill
     */
    public CustomSkill(OrbisSkills plugin, String name, ConfigurationSection config) {
        super(plugin, name);

        // Load sources and triggers
        if (config.isList("sources")) {
            sources.addAll(config.getStringList("sources"));
        }

        if (config.isList("triggers")) {
            triggers.addAll(config.getStringList("triggers"));
        }

        if (config.isList("trigger-materials")) {
            triggerMaterials.addAll(config.getStringList("trigger-materials"));
        }

        // Load base experience value
        baseExpValue = config.getDouble("base-exp", 5.0);
    }

    @Override
    protected void registerAbilities() {
        // Load abilities from config
        File file = new File(plugin.getDataFolder(), "config/abilities/" + name + "_abilities.yml");
        if (!file.exists()) {
            // Create default abilities file
            createDefaultAbilitiesFile(file);
        }

        // Load abilities from file
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection abilitiesSection = config.getConfigurationSection("abilities");

        if (abilitiesSection != null) {
            for (String key : abilitiesSection.getKeys(false)) {
                ConfigurationSection abilitySection = abilitiesSection.getConfigurationSection(key);
                if (abilitySection != null) {
                    if (!abilitySection.getBoolean("enabled", true)) {
                        continue;
                    }

                    // Load base ability
                    Ability ability = loadAbilityFromConfig(abilitySection);
                    if (ability != null) {
                        registerAbility(ability);

                        // Load custom ability info
                        CustomAbilityInfo info = new CustomAbilityInfo();
                        info.setName(abilitySection.getString("name", key));
                        info.setType(abilitySection.getString("type", "passive"));
                        info.setCooldown(abilitySection.getInt("cooldown", 0));
                        info.setRadius(abilitySection.getInt("radius", 0));
                        info.setDuration(abilitySection.getInt("duration", 0));

                        // Load messages
                        ConfigurationSection messagesSection = abilitySection.getConfigurationSection("messages");
                        if (messagesSection != null) {
                            for (String messageKey : messagesSection.getKeys(false)) {
                                info.addMessage(messageKey, messagesSection.getString(messageKey));
                            }
                        }

                        // Load additional parameters
                        ConfigurationSection paramsSection = abilitySection.getConfigurationSection("parameters");
                        if (paramsSection != null) {
                            for (String paramKey : paramsSection.getKeys(false)) {
                                info.addParameter(paramKey, paramsSection.getString(paramKey));
                            }
                        }

                        customAbilityInfo.put(key.toLowerCase(), info);
                    }
                }
            }
        }
    }

    /**
     * Create a default abilities file
     * @param file the file to create
     */
    private void createDefaultAbilitiesFile(File file) {
        try {
            // Create parent directories if they don't exist
            file.getParentFile().mkdirs();

            // Create default configuration
            FileConfiguration config = new YamlConfiguration();
            ConfigurationSection abilitiesSection = config.createSection("abilities");

            // Sample ability
            ConfigurationSection sampleAbility = abilitiesSection.createSection("sample");
            sampleAbility.set("enabled", true);
            sampleAbility.set("unlock-level", 10);
            sampleAbility.set("name", "&e" + getName() + " Sample Ability");
            sampleAbility.set("description", "A sample ability for the " + getDisplayName() + " skill");
            sampleAbility.set("type", "passive");

            // Sample effects
            ConfigurationSection effectsSection = sampleAbility.createSection("effects");
            effectsSection.set("10", 0.05);
            effectsSection.set("20", 0.10);
            effectsSection.set("30", 0.15);
            effectsSection.set("40", 0.20);
            effectsSection.set("50", 0.25);

            // Sample messages
            ConfigurationSection messagesSection = sampleAbility.createSection("messages");
            messagesSection.set("activate", "&aYour " + getName() + " ability activated!");
            messagesSection.set("cooldown", "&cYou must wait {time} before using this ability again!");

            // Save config
            config.save(file);

            plugin.getLogger().info("Created default abilities file for " + name);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to create default abilities file for " + name);
            e.printStackTrace();
        }
    }

    /**
     * Handle event trigger for custom skill
     * @param player the player
     * @param triggerType the trigger type
     * @param material the material involved (can be null)
     */
    public void handleTrigger(Player player, String triggerType, String material) {
        // Nothing to do if player is null
        if (player == null) {
            return;
        }

        // Check if trigger is applicable for this skill
        if (!triggers.contains(triggerType)) {
            return;
        }

        // Check material if specified
        if (material != null && !triggerMaterials.isEmpty() && !triggerMaterials.contains(material)) {
            return;
        }

        // Add base experience
        addExperience(player, baseExpValue);

        // Handle ability effects
        handleAbilityEffects(player, triggerType, material);
    }

    /**
     * Handle ability effects for a trigger
     * @param player the player
     * @param triggerType the trigger type
     * @param material the material involved (can be null)
     */
    private void handleAbilityEffects(Player player, String triggerType, String material) {
        int playerLevel = plugin.getDataManager().getPlayerData(player.getUniqueId()).getSkillLevel(name);

        // Check each ability
        for (String abilityName : abilities.keySet()) {
            Ability ability = abilities.get(abilityName);
            CustomAbilityInfo info = customAbilityInfo.get(abilityName);

            // Skip if no custom info or player doesn't have the required level
            if (info == null || playerLevel < ability.getUnlockLevel()) {
                continue;
            }

            // Handle different ability types
            if (info.getType().equalsIgnoreCase("passive")) {
                // Passive abilities have a chance to trigger
                double chance = ability.getEffectForLevel(playerLevel);
                if (random.nextDouble() < chance) {
                    if (ability.trigger(player, plugin, 0)) {
                        // Send activation message
                        String message = info.getMessage("activate");
                        if (message != null && !message.isEmpty()) {
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                        }

                        // Apply passive effects
                        applyPassiveEffect(player, info, triggerType, material);
                    }
                }
            } else if (info.getType().equalsIgnoreCase("active")) {
                // Active abilities require a cooldown
                int cooldown = info.getCooldown();
                if (cooldown > 0 && ability.canUse(player, plugin)) {
                    // Check conditions (player sneaking for active abilities)
                    if (player.isSneaking()) {
                        if (ability.trigger(player, plugin, cooldown)) {
                            // Send activation message
                            String message = info.getMessage("activate");
                            if (message != null && !message.isEmpty()) {
                                player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                            }

                            // Apply active effects
                            applyActiveEffect(player, info, triggerType, material);
                        }
                    }
                }
            } else if (info.getType().equalsIgnoreCase("aoe")) {
                // AOE abilities affect an area
                int cooldown = info.getCooldown();
                if (cooldown > 0 && ability.canUse(player, plugin)) {
                    // Check conditions
                    if (player.isSneaking()) {
                        if (ability.trigger(player, plugin, cooldown)) {
                            // Send activation message
                            String message = info.getMessage("activate");
                            if (message != null && !message.isEmpty()) {
                                player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                            }

                            // Apply AOE effects
                            applyAoeEffect(player, info, triggerType, material);
                        }
                    }
                }
            }
        }
    }

    /**
     * Apply passive effect
     * @param player the player
     * @param info the ability info
     * @param triggerType the trigger type
     * @param material the material involved
     */
    private void applyPassiveEffect(Player player, CustomAbilityInfo info, String triggerType, String material) {
        // Common passive effects
        if (triggerType.equals("BREAK") || triggerType.equals("HARVEST") ||
                triggerType.equals("CRAFT") || triggerType.equals("SMELT")) {
            // Double drops-style ability
            if (material != null) {
                try {
                    Material mat = Material.valueOf(material);
                    // Give extra item
                    player.getInventory().addItem(new ItemStack(mat, 1));
                } catch (IllegalArgumentException ignored) {
                    // Invalid material, do nothing
                }
            }
        } else if (triggerType.equals("CONSUME")) {
            // Food/potion effect enhancement
            // This would be better implemented with a more specific handler
            // but for demonstration, we'll add some generic effects
            player.setFoodLevel(Math.min(player.getFoodLevel() + 1, 20));
        }
    }

    /**
     * Apply active effect
     * @param player the player
     * @param info the ability info
     * @param triggerType the trigger type
     * @param material the material involved
     */
    private void applyActiveEffect(Player player, CustomAbilityInfo info, String triggerType, String material) {
        // Active effects are more complex and would depend on the specific ability
        // This would be better implemented in a dedicated handler

        int duration = info.getDuration();
        if (duration > 0) {
            // Example: Apply a temporary effect
            // This is just for demonstration - real implementation would be more robust
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                // Effect ends
                String message = info.getMessage("end");
                if (message != null && !message.isEmpty()) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                }
            }, duration * 20L);
        }
    }

    /**
     * Apply AOE effect
     * @param player the player
     * @param info the ability info
     * @param triggerType the trigger type
     * @param material the material involved
     */
    private void applyAoeEffect(Player player, CustomAbilityInfo info, String triggerType, String material) {
        int radius = info.getRadius();
        if (radius <= 0) {
            radius = 5; // Default radius
        }

        // Apply effects to nearby players
        player.getWorld().getNearbyEntities(player.getLocation(), radius, radius, radius).forEach(entity -> {
            if (entity instanceof Player && entity != player) {
                Player nearby = (Player) entity;
                // Apply effect to nearby player
                // This is just for demonstration
                String message = info.getParameter("nearbyMessage");
                if (message != null && !message.isEmpty()) {
                    nearby.sendMessage(ChatColor.translateAlternateColorCodes('&', message)
                            .replace("{player}", player.getName()));
                }
            }
        });
    }

    /**
     * Get the sources for this skill
     * @return the sources
     */
    public List<String> getSources() {
        return sources;
    }

    /**
     * Get the triggers for this skill
     * @return the triggers
     */
    public List<String> getTriggers() {
        return triggers;
    }

    /**
     * Get the trigger materials for this skill
     * @return the trigger materials
     */
    public List<String> getTriggerMaterials() {
        return triggerMaterials;
    }

    /**
     * Get the base experience value
     * @return the base experience value
     */
    public double getBaseExpValue() {
        return baseExpValue;
    }

    /**
     * Class to store custom ability information
     */
    private static class CustomAbilityInfo {
        private String name;
        private String type;
        private int cooldown;
        private int radius;
        private int duration;
        private final Map<String, String> messages = new HashMap<>();
        private final Map<String, String> parameters = new HashMap<>();

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }

        public void setCooldown(int cooldown) {
            this.cooldown = cooldown;
        }

        public int getCooldown() {
            return cooldown;
        }

        public void setRadius(int radius) {
            this.radius = radius;
        }

        public int getRadius() {
            return radius;
        }

        public void setDuration(int duration) {
            this.duration = duration;
        }

        public int getDuration() {
            return duration;
        }

        public void addMessage(String key, String message) {
            messages.put(key.toLowerCase(), message);
        }

        public String getMessage(String key) {
            return messages.get(key.toLowerCase());
        }

        public void addParameter(String key, String value) {
            parameters.put(key.toLowerCase(), value);
        }

        public String getParameter(String key) {
            return parameters.get(key.toLowerCase());
        }
    }
}