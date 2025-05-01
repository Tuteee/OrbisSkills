package com.orbis.skills;

import com.orbis.skills.commands.AdminCommand;
import com.orbis.skills.commands.SkillsCommand;
import com.orbis.skills.config.ConfigManager;
import com.orbis.skills.data.DataManager;
import com.orbis.skills.data.Storage;
import com.orbis.skills.data.YamlStorage;
import com.orbis.skills.data.SQLStorage;
import com.orbis.skills.listeners.AbilityListeners;
import com.orbis.skills.listeners.CustomSkillListeners;
import com.orbis.skills.listeners.SkillExpListeners;
import com.orbis.skills.skills.CustomSkillManager;
import com.orbis.skills.skills.SkillManager;
import org.bukkit.plugin.java.JavaPlugin;

public class OrbisSkills extends JavaPlugin {

    private static OrbisSkills instance;
    private ConfigManager configManager;
    private DataManager dataManager;
    private SkillManager skillManager;
    private CustomSkillManager customSkillManager;
    private Storage storage;

    @Override
    public void onEnable() {
       

        instance = this;

       

        String version = getServer().getBukkitVersion();
        if (!version.contains("1.21")) {
            getLogger().warning("OrbisSkills is designed for 1.21.3. You're running " + version);
            getLogger().warning("Some features may not work correctly!");
        }

       

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") == null) {
            getLogger().warning("PlaceholderAPI not found! Placeholder support will be disabled.");
        } else {
            getLogger().info("PlaceholderAPI found! Registering expansions...");
           

            new OrbisSkillsPlaceholderExpansion(this).register();
        }

       

        configManager = new ConfigManager(this);
        configManager.loadConfigs();

       

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

       

        customSkillManager = new CustomSkillManager(this);
        customSkillManager.loadCustomSkills();

       

        getServer().getPluginManager().registerEvents(new SkillExpListeners(this), this);
        getServer().getPluginManager().registerEvents(new AbilityListeners(this), this);
        getServer().getPluginManager().registerEvents(new CustomSkillListeners(this), this);

       

        getCommand("skills").setExecutor(new SkillsCommand(this));
        getCommand("skillsadmin").setExecutor(new AdminCommand(this));

        getLogger().info("OrbisSkills v" + getDescription().getVersion() + " has been enabled!");
    }

    @Override
    public void onDisable() {
       

        if (dataManager != null) {
            dataManager.saveAllData();
        }

       

        if (storage != null) {
            storage.close();
        }

        getLogger().info("OrbisSkills has been disabled!");
    }

    

    public static OrbisSkills getInstance() {
        return instance;
    }

    

    public ConfigManager getConfigManager() {
        return configManager;
    }

    

    public DataManager getDataManager() {
        return dataManager;
    }

    

    public SkillManager getSkillManager() {
        return skillManager;
    }

    

    public CustomSkillManager getCustomSkillManager() {
        return customSkillManager;
    }

    

    public Storage getStorage() {
        return storage;
    }
}