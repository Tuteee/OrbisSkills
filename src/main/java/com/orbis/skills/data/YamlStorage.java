package com.orbis.skills.data;

import com.orbis.skills.OrbisSkills;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

public class YamlStorage implements Storage {

    private final OrbisSkills plugin;
    private final File dataFolder;
    private final ExecutorService executor;

    /**
     * Create a new YAML storage
     * @param plugin the plugin instance
     */
    public YamlStorage(OrbisSkills plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "data/players");
        this.executor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void initialize() {
        // Create data folder if it doesn't exist
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            plugin.getLogger().severe("Failed to create data folder: " + dataFolder.getAbsolutePath());
        }
    }

    @Override
    public PlayerData loadPlayerData(UUID uuid) {
        File file = getPlayerFile(uuid);
        if (!file.exists()) {
            return null;
        }

        try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            Map<String, Object> data = config.getConfigurationSection("data").getValues(true);
            return new PlayerData(uuid, data);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load player data for " + uuid, e);
            return null;
        }
    }

    @Override
    public void savePlayerData(PlayerData data) {
        // Run asynchronously to avoid blocking main thread
        executor.submit(() -> savePlayerDataSync(data));
    }

    /**
     * Save player data synchronously
     * @param data the player data
     */
    private void savePlayerDataSync(PlayerData data) {
        File file = getPlayerFile(data.getUuid());
        FileConfiguration config = new YamlConfiguration();

        // Store data
        config.set("data", data.serialize());

        // Save to file
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save player data for " + data.getUuid(), e);
        }
    }

    @Override
    public void close() {
        executor.shutdown();
    }

    /**
     * Get the player file
     * @param uuid the player UUID
     * @return the player file
     */
    private File getPlayerFile(UUID uuid) {
        return new File(dataFolder, uuid.toString() + ".yml");
    }
}