package com.orbis.skills.config;

import com.orbis.skills.OrbisSkills;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class ConfigManager {

    private final OrbisSkills plugin;
    private final Map<String, FileConfiguration> configs = new HashMap<>();
    private final Map<String, File> configFiles = new HashMap<>();

    

    public ConfigManager(OrbisSkills plugin) {
        this.plugin = plugin;
    }

    

    public void loadConfigs() {
       

        if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) {
            plugin.getLogger().severe("Failed to create plugin directory!");
            return;
        }

       

        plugin.saveDefaultConfig();
        plugin.reloadConfig();

       

        loadConfig("messages.yml");

       

        File abilitiesDir = new File(plugin.getDataFolder(), "config/abilities");
        if (!abilitiesDir.exists() && !abilitiesDir.mkdirs()) {
            plugin.getLogger().severe("Failed to create abilities directory!");
        } else {
            loadConfig("config/abilities/fishing_abilities.yml");
            loadConfig("config/abilities/fencing_abilities.yml");
            loadConfig("config/abilities/archery_abilities.yml");
            loadConfig("config/abilities/mining_abilities.yml");
            loadConfig("config/abilities/woodcutting_abilities.yml");
            loadConfig("config/abilities/farming_abilities.yml");
            loadConfig("config/abilities/acrobatics_abilities.yml");
        }

       

        File dropsDir = new File(plugin.getDataFolder(), "config/drops");
        if (!dropsDir.exists() && !dropsDir.mkdirs()) {
            plugin.getLogger().severe("Failed to create drops directory!");
        } else {
            loadConfig("config/drops/fishing_drops.yml");
        }

        plugin.getLogger().info("Loaded " + configs.size() + " configurations");
    }

    

    private void loadConfig(String fileName) {
        File configFile = new File(plugin.getDataFolder(), fileName);

       

        if (!configFile.exists()) {
            plugin.saveResource(fileName, false);
        }

       

        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

       

        configs.put(fileName, config);
        configFiles.put(fileName, configFile);

        plugin.getLogger().info("Loaded config: " + fileName);
    }

    

    public FileConfiguration getConfig(String fileName) {
        return configs.get(fileName);
    }

    

    public FileConfiguration getMessagesConfig() {
        return getConfig("messages.yml");
    }

    

    public FileConfiguration getDropsConfig(String dropFileName) {
        return getConfig("config/drops/" + dropFileName);
    }

    

    public FileConfiguration getAbilitiesConfig(String abilityFileName) {
        return getConfig("config/abilities/" + abilityFileName);
    }

    

    public String getColoredString(String path) {
        FileConfiguration messagesConfig = getMessagesConfig();
        if (messagesConfig == null) {
            return ChatColor.RED + "Message not found: " + path;
        }

        String message = messagesConfig.getString(path);
        if (message == null) {
            return ChatColor.RED + "Message not found: " + path;
        }

        return ChatColor.translateAlternateColorCodes('&', message);
    }

    

    public void reload() {
       

        plugin.reloadConfig();

       

        for (Map.Entry<String, File> entry : configFiles.entrySet()) {
            String fileName = entry.getKey();
            File file = entry.getValue();

            FileConfiguration config = YamlConfiguration.loadConfiguration(file);

           

            InputStream defaultStream = plugin.getResource(fileName);
            if (defaultStream != null) {
                YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                        new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
                config.setDefaults(defaultConfig);
            }

           

            configs.put(fileName, config);
        }

        plugin.getLogger().info("Reloaded all configurations");
    }

    

    public void saveConfig(String fileName) {
        FileConfiguration config = configs.get(fileName);
        File file = configFiles.get(fileName);

        if (config == null || file == null) {
            plugin.getLogger().warning("Cannot save config: " + fileName);
            return;
        }

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save config to " + file, e);
        }
    }

    

    public void saveAllConfigs() {
        for (String fileName : configs.keySet()) {
            saveConfig(fileName);
        }
    }

    

    public static YamlConfiguration getDefaultMessagesConfig() {
        YamlConfiguration config = new YamlConfiguration();

       

        config.set("prefix", "&8[&bOrbisSkills&8] &7");

       

        config.set("level-up", "&aYour {skill} skill has increased to level {level}!");
        config.set("level-up-title", "&b{skill} Level Up!");
        config.set("level-up-subtitle", "&7You are now level &b{level}");
        config.set("ability-unlock", "&aYou have unlocked the &e{ability} &aability for your {skill} skill!");
        config.set("special-drop", "&aYou found a special item: &e{item}");
        config.set("special-drop-full-inv", "&aYou found a special item: &e{item} &abut your inventory is full!");

       

        config.set("command.no-permission", "&cYou don't have permission to use this command!");
        config.set("command.player-only", "&cThis command can only be used by players!");
        config.set("command.not-found", "&cCommand not found!");
        config.set("command.skill-not-found", "&cSkill not found: {skill}");

       

        config.set("admin.reset-skill", "&aReset {player}'s {skill} skill!");
        config.set("admin.set-level", "&aSet {player}'s {skill} level to {level}!");
        config.set("admin.add-exp", "&aAdded {amount} experience to {player}'s {skill} skill!");
        config.set("admin.remove-exp", "&aRemoved {amount} experience from {player}'s {skill} skill!");

        return config;
    }
}