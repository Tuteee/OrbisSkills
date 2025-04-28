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

    /**
     * Create a new SQL storage
     * @param plugin the plugin instance
     */
    public SQLStorage(OrbisSkills plugin) {
        this.plugin = plugin;

        // Get config values
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
            // Connect to database
            connect();

            // Create tables
            createTables();

            plugin.getLogger().info("Connected to " + (useMySql ? "MySQL" : "SQLite") + " database!");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize database", e);
        }
    }

    /**
     * Connect to the database
     * @throws SQLException if connection fails
     */
    private void connect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            return;
        }

        if (useMySql) {
            // MySQL
            String url = "jdbc:mysql://" + host + ":" + port + "/" + database +
                    "?useSSL=false&autoReconnect=true";
            connection = DriverManager.getConnection(url, username, password);
        } else {
            // SQLite
            String url = "jdbc:sqlite:" + new java.io.File(plugin.getDataFolder(), sqliteFile);
            connection = DriverManager.getConnection(url);
        }
    }

    /**
     * Create database tables
     * @throws SQLException if query fails
     */
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
            // Ensure connection is open
            connect();

            // Query skill data
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

            // Add data to map
            data.put("levels", levels);
            data.put("experience", experience);

            // Create player data
            return new PlayerData(uuid, data);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load player data for " + uuid, e);
            return new PlayerData(uuid);
        }
    }

    @Override
    public void savePlayerData(PlayerData data) {
        // Run asynchronously
        executor.submit(() -> savePlayerDataSync(data));
    }

    /**
     * Save player data synchronously
     * @param data the player data
     */
    private void savePlayerDataSync(PlayerData data) {
        try {
            // Ensure connection is open
            connect();

            // Prepare query
            String query = "INSERT INTO " + tablePrefix + "skills (uuid, skill, level, experience) " +
                    "VALUES (?, ?, ?, ?) " +
                    "ON " + (useMySql ? "DUPLICATE KEY" : "CONFLICT") + " UPDATE " +
                    "level = VALUES(level), experience = VALUES(experience);";

            // Get serialized data
            Map<String, Object> serialized = data.serialize();
            Map<?, ?> levels = (Map<?, ?>) serialized.get("levels");
            Map<?, ?> experience = (Map<?, ?>) serialized.get("experience");

            // Insert or update data
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                for (Map.Entry<?, ?> entry : levels.entrySet()) {
                    if (entry.getKey() instanceof String && entry.getValue() instanceof Integer) {
                        String skill = (String) entry.getKey();
                        int level = (Integer) entry.getValue();
                        double exp = 0.0;

                        // Get experience
                        Object expObj = experience.get(skill);
                        if (expObj instanceof Number) {
                            exp = ((Number) expObj).doubleValue();
                        }

                        // Set parameters
                        statement.setString(1, data.getUuid().toString());
                        statement.setString(2, skill);
                        statement.setInt(3, level);
                        statement.setDouble(4, exp);

                        // Execute update
                        statement.executeUpdate();
                    }
                }
            }

            // Mark data as clean
            data.setDirty(false);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save player data for " + data.getUuid(), e);
        }
    }

    @Override
    public void close() {
        // Shutdown executor
        executor.shutdown();

        // Close connection
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to close database connection", e);
        }
    }
}