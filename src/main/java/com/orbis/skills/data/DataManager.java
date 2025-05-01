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

    

    public DataManager(OrbisSkills plugin, Storage storage) {
        this.plugin = plugin;
        this.storage = storage;
    }

    

    public void initialize() {
       

        storage.initialize();

       

        for (Player player : Bukkit.getOnlinePlayers()) {
            loadPlayerData(player.getUniqueId());
        }

       

        int saveInterval = plugin.getConfig().getInt("settings.save-interval", 6000);
        autoSaveTask = Bukkit.getScheduler().runTaskTimerAsynchronously(
                plugin, this::saveAllData, saveInterval, saveInterval);

        plugin.getLogger().info("DataManager initialized with " + playerDataCache.size() + " players");
    }

    

    public PlayerData loadPlayerData(UUID uuid) {
       

        if (playerDataCache.containsKey(uuid)) {
            return playerDataCache.get(uuid);
        }

       

        PlayerData data = storage.loadPlayerData(uuid);
        if (data == null) {
           

            data = new PlayerData(uuid);

           

            for (Skill skill : plugin.getSkillManager().getAllSkills()) {
                data.initializeSkill(skill.getName());
            }
        }

       

        playerDataCache.put(uuid, data);
        return data;
    }

    

    public void savePlayerData(UUID uuid) {
        PlayerData data = playerDataCache.get(uuid);
        if (data != null && data.isDirty()) {
            storage.savePlayerData(data);
            data.setDirty(false);
        }
    }

    

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

    

    public void unloadPlayerData(UUID uuid) {
        PlayerData data = playerDataCache.get(uuid);
        if (data != null) {
            if (data.isDirty()) {
                storage.savePlayerData(data);
            }
            playerDataCache.remove(uuid);
        }
    }

    

    public PlayerData getPlayerData(UUID uuid) {
        return playerDataCache.get(uuid);
    }

    

    public boolean addExperience(UUID uuid, String skillName, double amount) {
        PlayerData data = getPlayerData(uuid);
        if (data == null) {
            return false;
        }

        return data.addSkillExp(skillName, amount);
    }

    

    public void setExperience(UUID uuid, String skillName, double amount) {
        PlayerData data = getPlayerData(uuid);
        if (data != null) {
            data.setSkillExp(skillName, amount);
        }
    }

    

    public void setLevel(UUID uuid, String skillName, int level) {
        PlayerData data = getPlayerData(uuid);
        if (data != null) {
            data.setSkillLevel(skillName, level);
        }
    }

    

    public void resetSkill(UUID uuid, String skillName) {
        PlayerData data = getPlayerData(uuid);
        if (data != null) {
            data.resetSkill(skillName);
        }
    }

    

    public void resetAllSkills(UUID uuid) {
        PlayerData data = getPlayerData(uuid);
        if (data != null) {
            data.resetAllSkills();
        }
    }

    

    public void shutdown() {
       

        if (autoSaveTask != null) {
            autoSaveTask.cancel();
        }

       

        saveAllData();

       

        playerDataCache.clear();

       

        storage.close();
    }
}