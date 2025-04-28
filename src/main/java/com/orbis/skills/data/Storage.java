package com.orbis.skills.data;

import java.util.UUID;

/**
 * Interface for storage implementations
 */
public interface Storage {

    /**
     * Initialize the storage
     */
    void initialize();

    /**
     * Load player data
     * @param uuid the player UUID
     * @return the player data, or null if not found
     */
    PlayerData loadPlayerData(UUID uuid);

    /**
     * Save player data
     * @param data the player data
     */
    void savePlayerData(PlayerData data);

    /**
     * Close the storage
     */
    void close();
}