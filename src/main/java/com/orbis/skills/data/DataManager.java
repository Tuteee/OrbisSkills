package com.orbis.skills.data;

import com.orbis.skills.OrbisSkills;
import com.orbis.skills.skills.Skill;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DataManager {

    private final OrbisSkills plugin;
    private final Storage storage;
    private final Map<UUID, PlayerData> playerDataCache = new ConcurrentHashMap<>();
    private BukkitTask autoSaveTask;

    /**
     * Create a new data manager
     * @param plugin the plugin instance
     * @param storage the storage implementation
     */
    public DataManager(OrbisSkills plugin, Storage storage) {
        this.plugin = plugin;
        this.storage = storage;
    }

    /**
     * Initialize the data manager
     */
    public void initialize() {
        // Initialize storage
        storage.initialize();

        // Load online players
        for (Player player : Bukkit.getOnlinePlayers()) {
            loadPlayerData(player.getUniqueId());
        }

        // Schedule auto-save task
        int saveInterval = plugin.getConfig().getInt("settings.save-interval", 6000);
        autoSaveTask = Bukkit.getScheduler().runTaskTimerAsynchronously(
                plugin, this::saveAllData, saveInterval, saveInterval);

        plugin.getLogger().info("DataManager initialized with " + playerDataCache.size() + " players");
    }

    /**
     * Load player data
     * @param uuid the player UUID
     * @return the player data
     */
    public PlayerData loadPlayerData(UUID uuid) {
        // Check cache first
        if (playerDataCache.containsKey(uuid)) {
            return playerDataCache.get(uuid);
        }

        // Load from storage
        PlayerData data = storage.loadPlayerData(uuid);
        if (data == null) {
            // Create new data
            data = new PlayerData(uuid);

            // Initialize all skills
            for (Skill skill : plugin.getSkillManager().getAllSkills()) {
                data.initializeSkill(skill.getName());
            }
        }

        // Cache the data
        playerDataCache.put(uuid, data);
        return data;
    }

    /**
     * Save player data
     * @param uuid the player UUID
     */
    public void savePlayerData(UUID uuid) {
        PlayerData data = playerDataCache.get(uuid);
        if (data != null && data.isDirty()) {
            storage.savePlayerData(data);
            data.setDirty(false);
        }
    }

    /**
     * Save all cached player data
     */
    public void saveAllData() {
        int savedCount = 0;
        for (Map.Entry<UUID, PlayerData> entry : playerDataCache.entrySet()) {
            PlayerData data = entry.getValue();
            if (data.isDirty()) {
                storage.savePlayerData(data);
                data.setDirty(false);
                savedCount++;
            }
        }

        if (savedCount > 0) {
            plugin.getLogger().info("Saved " + savedCount + " player data entries");
        }
    }

    /**
     * Unload player data
     * @param uuid the player UUID
     */
    public void unloadPlayerData(UUID uuid) {
        PlayerData data = playerDataCache.get(uuid);
        if (data != null) {
            if (data.isDirty()) {
                storage.savePlayerData(data);
            }
            playerDataCache.remove(uuid);
        }
    }

    /**
     * Get player data
     * @param uuid the player UUID
     * @return the player data, or null if not loaded
     */
    public PlayerData getPlayerData(UUID uuid) {
        return playerDataCache.get(uuid);
    }

    /**
     * Add experience to a player for a skill
     * @param uuid the player UUID
     * @param skillName the skill name
     * @param amount the amount to add
     * @return true if player leveled up
     */
    public boolean addExperience(UUID uuid, String skillName, double amount) {
        PlayerData data = getPlayerData(uuid);
        if (data == null) {
            return false;
        }

        return data.addSkillExp(skillName, amount);
    }

    /**
     * Set experience for a player's skill
     * @param uuid the player UUID
     * @param skillName the skill name
     * @param amount the amount to set
     */
    public void setExperience(UUID uuid, String skillName, double amount) {
        PlayerData data = getPlayerData(uuid);
        if (data != null) {
            data.setSkillExp(skillName, amount);
        }
    }

    /**
     * Set level for a player's skill
     * @param uuid the player UUID
     * @param skillName the skill name
     * @param level the level to set
     */
    public void setLevel(UUID uuid, String skillName, int level) {
        PlayerData data = getPlayerData(uuid);
        if (data != null) {
            data.setSkillLevel(skillName, level);
        }
    }

    /**
     * Reset a player's skill
     * @param uuid the player UUID
     * @param skillName the skill name
     */
    public void resetSkill(UUID uuid, String skillName) {
        PlayerData data = getPlayerData(uuid);
        if (data != null) {
            data.resetSkill(skillName);
        }
    }

    /**
     * Reset all player's skills
     * @param uuid the player UUID
     */
    public void resetAllSkills(UUID uuid) {
        PlayerData data = getPlayerData(uuid);
        if (data != null) {
            data.resetAllSkills();
        }
    }

    /**
     * Shutdown the data manager
     */
    public void shutdown() {
        // Cancel auto-save task
        if (autoSaveTask != null) {
            autoSaveTask.cancel();
        }

        // Save all data
        saveAllData();

        // Clear cache
        playerDataCache.clear();

        // Close storage
        storage.close();
    }
}