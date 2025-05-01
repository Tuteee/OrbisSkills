package com.orbis.skills.skills;

import com.orbis.skills.OrbisSkills;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;
import java.util.Map;



public class CustomSkillManager {

    private final OrbisSkills plugin;
    private final Map<String, CustomSkill> customSkills = new HashMap<>();

    

    public CustomSkillManager(OrbisSkills plugin) {
        this.plugin = plugin;
    }

    

    public void loadCustomSkills() {
       

        File skillsDir = new File(plugin.getDataFolder(), "config/custom_skills");
        if (!skillsDir.exists()) {
            if (skillsDir.mkdirs()) {
               

                createExampleSkillFile(skillsDir);
            } else {
                plugin.getLogger().severe("Failed to create custom skills directory!");
                return;
            }
        }

       

        File[] skillFiles = skillsDir.listFiles((dir, name) -> name.endsWith(".yml"));
        if (skillFiles == null || skillFiles.length == 0) {
            plugin.getLogger().info("No custom skills found");
            return;
        }

       

        customSkills.clear();

       

        for (File skillFile : skillFiles) {
            try {
                FileConfiguration config = YamlConfiguration.loadConfiguration(skillFile);
                String fileName = skillFile.getName().replace(".yml", "");

               

                if (!config.getBoolean("enabled", true)) {
                    plugin.getLogger().info("Skipping disabled custom skill: " + fileName);
                    continue;
                }

               

                String skillName = config.getString("name", fileName);
                CustomSkill skill = new CustomSkill(plugin, skillName, config);

               

                customSkills.put(skillName.toLowerCase(), skill);

               

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

    

    private void createExampleSkillFile(File directory) {
        File exampleFile = new File(directory, "example_skill.yml");

        try {
            FileConfiguration config = new YamlConfiguration();

           

            config.set("enabled", true);
            config.set("name", "Cooking");
            config.set("display-name", "&6Cooking");
            config.set("description", "Skill for cooking food items");
            config.set("base-exp", 5.0);

           

            config.set("sources", new String[]{
                    "CRAFTING", "FURNACE", "CAMPFIRE", "SMOKER"
            });

           

            config.set("triggers", new String[]{
                    "CRAFT", "SMELT", "BREW"
            });

           

            config.set("trigger-materials", new String[]{
                    "COOKED_BEEF", "COOKED_CHICKEN", "COOKED_MUTTON", "COOKED_PORKCHOP",
                    "COOKED_RABBIT", "COOKED_COD", "COOKED_SALMON", "BAKED_POTATO",
                    "CAKE", "BREAD", "COOKIE", "PUMPKIN_PIE"
            });

           

            ConfigurationSection abilitiesSection = config.createSection("abilities");

           

            ConfigurationSection chefAbility = abilitiesSection.createSection("chef");
            chefAbility.set("unlock-level", 10);
            chefAbility.set("description", "Chance to get double food items when cooking");

           

            ConfigurationSection gourmetAbility = abilitiesSection.createSection("gourmet");
            gourmetAbility.set("unlock-level", 25);
            gourmetAbility.set("description", "Cooked food gives extra hunger and saturation");

           

            config.save(exampleFile);

            plugin.getLogger().info("Created example custom skill file: example_skill.yml");

        } catch (Exception e) {
            plugin.getLogger().severe("Failed to create example skill file");
            e.printStackTrace();
        }
    }

    

    public void handleTrigger(Player player, String triggerType, String material) {
        for (CustomSkill skill : customSkills.values()) {
            skill.handleTrigger(player, triggerType, material);
        }
    }

    

    public CustomSkill getCustomSkill(String name) {
        return customSkills.get(name.toLowerCase());
    }

    

    public boolean hasCustomSkill(String name) {
        return customSkills.containsKey(name.toLowerCase());
    }

    

    public Map<String, CustomSkill> getAllCustomSkills() {
        return customSkills;
    }
}