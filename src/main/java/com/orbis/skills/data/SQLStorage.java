package com.orbis.skills.data;

import com.orbis.skills.OrbisSkills;
import org.bukkit.configuration.ConfigurationSection;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

public class SQLStorage implements Storage {

    private final OrbisSkills plugin;
    private final boolean useMySql;
    private final ExecutorService executor;
    private Connection connection;

    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;
    private final String tablePrefix;
    private final String sqliteFile;

    

    public SQLStorage(OrbisSkills plugin) {
        this.plugin = plugin;

       

        ConfigurationSection config = plugin.getConfig().getConfigurationSection("storage");
        String type = config.getString("type", "sqlite").toLowerCase();
        this.useMySql = type.equals("mysql");

        if (useMySql) {
            ConfigurationSection mysqlConfig = config.getConfigurationSection("mysql");
            this.host = mysqlConfig.getString("host", "localhost");
            this.port = mysqlConfig.getInt("port", 3306);
            this.database = mysqlConfig.getString("database", "orbisskills");
            this.username = mysqlConfig.getString("username", "root");
            this.password = mysqlConfig.getString("password", "password");
            this.tablePrefix = mysqlConfig.getString("table-prefix", "orbisskills_");
            this.sqliteFile = null;
        } else {
            ConfigurationSection sqliteConfig = config.getConfigurationSection("sqlite");
            this.host = null;
            this.port = 0;
            this.database = null;
            this.username = null;
            this.password = null;
            this.tablePrefix = "orbisskills_";
            this.sqliteFile = sqliteConfig.getString("file", "database.db");
        }

        this.executor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void initialize() {
        try {
           

            connect();

           

            createTables();

            plugin.getLogger().info("Connected to " + (useMySql ? "MySQL" : "SQLite") + " database!");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize database", e);
        }
    }

    

    private void connect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            return;
        }

        if (useMySql) {
           

            String url = "jdbc:mysql://" + host + ":" + port + "/" + database +
                    "?useSSL=false&autoReconnect=true";
            connection = DriverManager.getConnection(url, username, password);
        } else {
           

            String url = "jdbc:sqlite:" + new java.io.File(plugin.getDataFolder(), sqliteFile);
            connection = DriverManager.getConnection(url);
        }
    }

    

    private void createTables() throws SQLException {
        String skillsTable = "CREATE TABLE IF NOT EXISTS " + tablePrefix + "skills (" +
                "uuid VARCHAR(36) NOT NULL, " +
                "skill VARCHAR(32) NOT NULL, " +
                "level INT NOT NULL DEFAULT 0, " +
                "experience DOUBLE NOT NULL DEFAULT 0, " +
                "PRIMARY KEY (uuid, skill)" +
                ");";

        try (Statement statement = connection.createStatement()) {
            statement.execute(skillsTable);
        }
    }

    @Override
    public PlayerData loadPlayerData(UUID uuid) {
        Map<String, Object> data = new HashMap<>();
        Map<String, Integer> levels = new HashMap<>();
        Map<String, Double> experience = new HashMap<>();

        try {
           

            connect();

           

            String query = "SELECT skill, level, experience FROM " + tablePrefix + "skills " +
                    "WHERE uuid = ?;";

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, uuid.toString());

                try (ResultSet results = statement.executeQuery()) {
                    while (results.next()) {
                        String skill = results.getString("skill");
                        int level = results.getInt("level");
                        double exp = results.getDouble("experience");

                        levels.put(skill, level);
                        experience.put(skill, exp);
                    }
                }
            }

           

            data.put("levels", levels);
            data.put("experience", experience);

           

            return new PlayerData(uuid, data);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load player data for " + uuid, e);
            return new PlayerData(uuid);
        }
    }

    @Override
    public void savePlayerData(PlayerData data) {
       

        executor.submit(() -> savePlayerDataSync(data));
    }

    

    private void savePlayerDataSync(PlayerData data) {
        try {
           

            connect();

           

            String query = "INSERT INTO " + tablePrefix + "skills (uuid, skill, level, experience) " +
                    "VALUES (?, ?, ?, ?) " +
                    "ON " + (useMySql ? "DUPLICATE KEY" : "CONFLICT") + " UPDATE " +
                    "level = VALUES(level), experience = VALUES(experience);";

           

            Map<String, Object> serialized = data.serialize();
            Map<?, ?> levels = (Map<?, ?>) serialized.get("levels");
            Map<?, ?> experience = (Map<?, ?>) serialized.get("experience");

           

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                for (Map.Entry<?, ?> entry : levels.entrySet()) {
                    if (entry.getKey() instanceof String && entry.getValue() instanceof Integer) {
                        String skill = (String) entry.getKey();
                        int level = (Integer) entry.getValue();
                        double exp = 0.0;

                       

                        Object expObj = experience.get(skill);
                        if (expObj instanceof Number) {
                            exp = ((Number) expObj).doubleValue();
                        }

                       

                        statement.setString(1, data.getUuid().toString());
                        statement.setString(2, skill);
                        statement.setInt(3, level);
                        statement.setDouble(4, exp);

                       

                        statement.executeUpdate();
                    }
                }
            }

           

            data.setDirty(false);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save player data for " + data.getUuid(), e);
        }
    }

    @Override
    public void close() {
       

        executor.shutdown();

       

        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to close database connection", e);
        }
    }
}