package com.orbis.skills.skills;

import com.orbis.skills.OrbisSkills;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SkillManager {

    private final OrbisSkills plugin;
    private final Map<String, Skill> skills = new HashMap<>();

    

    public SkillManager(OrbisSkills plugin) {
        this.plugin = plugin;
    }

    

    public void registerSkills() {
        ConfigurationSection enabledSection = plugin.getConfig().getConfigurationSection("skills.enabled");
        if (enabledSection == null) {
            plugin.getLogger().warning("No enabled skills section found in config!");
            return;
        }

       

        if (enabledSection.getBoolean("fishing", true)) {
            registerSkill(new FishingSkill(plugin));
        }

       

        if (enabledSection.getBoolean("fencing", true)) {
            registerSkill(new FencingSkill(plugin));
        }

       

        if (enabledSection.getBoolean("archery", true)) {
            registerSkill(new ArcherySkill(plugin));
        }

       

        if (enabledSection.getBoolean("mining", true)) {
            registerSkill(new MiningSkill(plugin));
        }

       

        if (enabledSection.getBoolean("woodcutting", true)) {
            registerSkill(new WoodcuttingSkill(plugin));
        }

       

        if (enabledSection.getBoolean("farming", true)) {
            registerSkill(new FarmingSkill(plugin));
        }

       

        if (enabledSection.getBoolean("acrobatics", true)) {
            registerSkill(new AcrobaticsSkill(plugin));
        }

        plugin.getLogger().info("Registered " + skills.size() + " skills!");
    }

    

    public void registerSkill(Skill skill) {
        skills.put(skill.getName().toLowerCase(), skill);
    }

    

    public Skill getSkill(String name) {
        return skills.get(name.toLowerCase());
    }

    

    public Collection<Skill> getAllSkills() {
        return skills.values();
    }

    

    public int getSkillCount() {
        return skills.size();
    }
}