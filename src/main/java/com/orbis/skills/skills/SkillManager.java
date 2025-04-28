package com.orbis.skills.skills;

import com.orbis.skills.OrbisSkills;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SkillManager {

    private final OrbisSkills plugin;
    private final Map<String, Skill> skills = new HashMap<>();

    /**
     * Create a new skill manager
     * @param plugin the plugin instance
     */
    public SkillManager(OrbisSkills plugin) {
        this.plugin = plugin;
    }

    /**
     * Register all skills
     */
    public void registerSkills() {
        ConfigurationSection enabledSection = plugin.getConfig().getConfigurationSection("skills.enabled");
        if (enabledSection == null) {
            plugin.getLogger().warning("No enabled skills section found in config!");
            return;
        }

        // Register fishing skill
        if (enabledSection.getBoolean("fishing", true)) {
            registerSkill(new FishingSkill(plugin));
        }

        // Register fencing skill
        if (enabledSection.getBoolean("fencing", true)) {
            registerSkill(new FencingSkill(plugin));
        }

        // Register archery skill
        if (enabledSection.getBoolean("archery", true)) {
            registerSkill(new ArcherySkill(plugin));
        }

        // Register mining skill
        if (enabledSection.getBoolean("mining", true)) {
            registerSkill(new MiningSkill(plugin));
        }

        // Register woodcutting skill
        if (enabledSection.getBoolean("woodcutting", true)) {
            registerSkill(new WoodcuttingSkill(plugin));
        }

        // Register farming skill
        if (enabledSection.getBoolean("farming", true)) {
            registerSkill(new FarmingSkill(plugin));
        }

        // Register acrobatics skill
        if (enabledSection.getBoolean("acrobatics", true)) {
            registerSkill(new AcrobaticsSkill(plugin));
        }

        plugin.getLogger().info("Registered " + skills.size() + " skills!");
    }

    /**
     * Register a skill
     * @param skill the skill to register
     */
    public void registerSkill(Skill skill) {
        skills.put(skill.getName().toLowerCase(), skill);
    }

    /**
     * Get a skill by name
     * @param name the name of the skill
     * @return the skill, or null if not found
     */
    public Skill getSkill(String name) {
        return skills.get(name.toLowerCase());
    }

    /**
     * Get all registered skills
     * @return all registered skills
     */
    public Collection<Skill> getAllSkills() {
        return skills.values();
    }

    /**
     * Get the number of registered skills
     * @return the number of registered skills
     */
    public int getSkillCount() {
        return skills.size();
    }
}