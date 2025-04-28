package com.orbis.skills.skills;

import com.orbis.skills.OrbisSkills;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Manager for custom skills
 */
public class CustomSkillManager {

    private final OrbisSkills plugin;
    private final Map<String, CustomSkill> customSkills = new HashMap<>();

    /**
     * Create a new custom skill manager
     * @param plugin the plugin instance
     */
    public CustomSkillManager(OrbisSkills plugin) {
        this.plugin = plugin;
    }

    /**
     * Load custom skills from configuration
     */
    public void loadCustomSkills() {
        // Create custom skills directory if it doesn't exist
        File skillsDir = new File(plugin.getDataFolder(), "config/custom_skills");
        if (!skillsDir.exists()) {
            if (skillsDir.mkdirs()) {
                // Create example skill file
                createExampleSkillFile(skillsDir);
            } else {
                plugin.getLogger().severe("Failed to create custom skills directory!");
                return;
            }
        }

        // Load all skill files
        File[] skillFiles = skillsDir.listFiles((dir, name) -> name.endsWith(".yml"));
        if (skillFiles == null || skillFiles.length == 0) {
            plugin.getLogger().info("No custom skills found");
            return;
        }

        // Clear existing skills
        customSkills.clear();

        // Load each skill
        for (File skillFile : skillFiles) {
            try {
                FileConfiguration config = YamlConfiguration.loadConfiguration(skillFile);
                String fileName = skillFile.getName().replace(".yml", "");

                // Skip if disabled
                if (!config.getBoolean("enabled", true)) {
                    plugin.getLogger().info("Skipping disabled custom skill: " + fileName);
                    continue;
                }

                // Create skill
                String skillName = config.getString("name", fileName);
                CustomSkill skill = new CustomSkill(plugin, skillName, config);

                // Register skill
                customSkills.put(skillName.toLowerCase(), skill);

                // Register with SkillManager if enabled in main config
                if (plugin.getConfig().getBoolean("skills.enabled.custom." + skillName.toLowerCase(), true)) {
                    plugin.getSkillManager().registerSkill(skill);
                    plugin.getLogger().info("Registered custom skill: " + skillName);
                } else {
                    plugin.getLogger().info("Custom skill loaded but not registered (disabled in main config): " + skillName);
                }

            } catch (Exception e) {
                plugin.getLogger().severe("Failed to load custom skill from file: " + skillFile.getName());
                e.printStackTrace();
            }
        }

        plugin.getLogger().info("Loaded " + customSkills.size() + " custom skills");
    }

    /**
     * Create an example skill file
     * @param directory the directory to create the file in
     */
    private void createExampleSkillFile(File directory) {
        File exampleFile = new File(directory, "example_skill.yml");

        try {
            FileConfiguration config = new YamlConfiguration();

            // Basic settings
            config.set("enabled", true);
            config.set("name", "Cooking");
            config.set("display-name", "&6Cooking");
            config.set("description", "Skill for cooking food items");
            config.set("base-exp", 5.0);

            // Sources - what game mechanics give this skill experience
            config.set("sources", new String[]{
                    "CRAFTING", "FURNACE", "CAMPFIRE", "SMOKER"
            });

            // Triggers - what events trigger this skill
            config.set("triggers", new String[]{
                    "CRAFT", "SMELT", "BREW"
            });

            // Materials that can trigger this skill
            config.set("trigger-materials", new String[]{
                    "COOKED_BEEF", "COOKED_CHICKEN", "COOKED_MUTTON", "COOKED_PORKCHOP",
                    "COOKED_RABBIT", "COOKED_COD", "COOKED_SALMON", "BAKED_POTATO",
                    "CAKE", "BREAD", "COOKIE", "PUMPKIN_PIE"
            });

            // Sample abilities section
            ConfigurationSection abilitiesSection = config.createSection("abilities");

            // First ability
            ConfigurationSection chefAbility = abilitiesSection.createSection("chef");
            chefAbility.set("unlock-level", 10);
            chefAbility.set("description", "Chance to get double food items when cooking");

            // Second ability
            ConfigurationSection gourmetAbility = abilitiesSection.createSection("gourmet");
            gourmetAbility.set("unlock-level", 25);
            gourmetAbility.set("description", "Cooked food gives extra hunger and saturation");

            // Save the config
            config.save(exampleFile);

            plugin.getLogger().info("Created example custom skill file: example_skill.yml");

        } catch (Exception e) {
            plugin.getLogger().severe("Failed to create example skill file");
            e.printStackTrace();
        }
    }

    /**
     * Handle custom skill trigger
     * @param player the player
     * @param triggerType the trigger type
     * @param material the material involved (can be null)
     */
    public void handleTrigger(Player player, String triggerType, String material) {
        for (CustomSkill skill : customSkills.values()) {
            skill.handleTrigger(player, triggerType, material);
        }
    }

    /**
     * Get a custom skill by name
     * @param name the skill name
     * @return the custom skill, or null if not found
     */
    public CustomSkill getCustomSkill(String name) {
        return customSkills.get(name.toLowerCase());
    }

    /**
     * Check if a custom skill exists
     * @param name the skill name
     * @return true if the custom skill exists
     */
    public boolean hasCustomSkill(String name) {
        return customSkills.containsKey(name.toLowerCase());
    }

    /**
     * Get all custom skills
     * @return all custom skills
     */
    public Map<String, CustomSkill> getAllCustomSkills() {
        return customSkills;
    }
}