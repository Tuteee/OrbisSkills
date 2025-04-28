package com.orbis.skills;

import com.orbis.skills.commands.AdminCommand;
import com.orbis.skills.commands.SkillsCommand;
import com.orbis.skills.config.ConfigManager;
import com.orbis.skills.data.DataManager;
import com.orbis.skills.data.Storage;
import com.orbis.skills.data.YamlStorage;
import com.orbis.skills.data.SQLStorage;
import com.orbis.skills.listeners.AbilityListeners;
import com.orbis.skills.listeners.SkillExpListeners;
import com.orbis.skills.skills.SkillManager;
import org.bukkit.plugin.java.JavaPlugin;

public class OrbisSkills extends JavaPlugin {

    private static OrbisSkills instance;
    private ConfigManager configManager;
    private DataManager dataManager;
    private SkillManager skillManager;
    private Storage storage;

    @Override
    public void onEnable() {
        // Set instance
        instance = this;

        // Check server version
        String version = getServer().getBukkitVersion();
        if (!version.contains("1.21")) {
            getLogger().warning("OrbisSkills is designed for 1.21.3. You're running " + version);
            getLogger().warning("Some features may not work correctly!");
        }

        // Check for PlaceholderAPI
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") == null) {
            getLogger().warning("PlaceholderAPI not found! Placeholder support will be disabled.");
        } else {
            getLogger().info("PlaceholderAPI found! Registering expansions...");
            // Register placeholder expansion
            new OrbisSkillsPlaceholderExpansion(this).register();
        }

        // Initialize managers
        configManager = new ConfigManager(this);
        configManager.loadConfigs();

        // Initialize storage based on config
        String storageType = getConfig().getString("storage.type", "yaml");
        if (storageType.equalsIgnoreCase("mysql") || storageType.equalsIgnoreCase("sqlite")) {
            storage = new SQLStorage(this);
        } else {
            storage = new YamlStorage(this);
        }

        dataManager = new DataManager(this, storage);
        dataManager.initialize();

        skillManager = new SkillManager(this);
        skillManager.registerSkills();

        // Register event listeners
        getServer().getPluginManager().registerEvents(new SkillExpListeners(this), this);
        getServer().getPluginManager().registerEvents(new AbilityListeners(this), this);

        // Register commands
        getCommand("skills").setExecutor(new SkillsCommand(this));
        getCommand("skillsadmin").setExecutor(new AdminCommand(this));

        getLogger().info("OrbisSkills v" + getDescription().getVersion() + " has been enabled!");
    }

    @Override
    public void onDisable() {
        // Save all player data
        if (dataManager != null) {
            dataManager.saveAllData();
        }

        // Close storage connections
        if (storage != null) {
            storage.close();
        }

        getLogger().info("OrbisSkills has been disabled!");
    }

    /**
     * Get the plugin instance
     * @return the plugin instance
     */
    public static OrbisSkills getInstance() {
        return instance;
    }

    /**
     * Get the config manager
     * @return the config manager
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }

    /**
     * Get the data manager
     * @return the data manager
     */
    public DataManager getDataManager() {
        return dataManager;
    }

    /**
     * Get the skill manager
     * @return the skill manager
     */
    public SkillManager getSkillManager() {
        return skillManager;
    }

    /**
     * Get the storage implementation
     * @return the storage implementation
     */
    public Storage getStorage() {
        return storage;
    }
}