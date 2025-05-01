package com.orbis.skills.data;

import java.util.UUID;



public interface Storage {

    

    void initialize();

    

    PlayerData loadPlayerData(UUID uuid);

    

    void savePlayerData(PlayerData data);

    

    void close();
}