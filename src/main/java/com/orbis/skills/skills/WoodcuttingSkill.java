package com.orbis.skills.skills;

import com.orbis.skills.OrbisSkills;
import com.orbis.skills.abilities.Ability;
import com.orbis.skills.abilities.woodcutting.HarvestmasterAbility;
import com.orbis.skills.abilities.woodcutting.LumberjackAbility;
import com.orbis.skills.abilities.woodcutting.TreeFellerAbility;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.HashSet;

public class WoodcuttingSkill extends Skill {

    private final Random random = new Random();
    private final Map<Material, Double> woodExpValues = new HashMap<>();

    /**
     * Create a new woodcutting skill
     * @param plugin the plugin instance
     */
    public WoodcuttingSkill(OrbisSkills plugin) {
        super(plugin, "woodcutting");

        // Initialize wood experience values
        initWoodExpValues();
    }

    /**
     * Initialize wood experience values
     */
    private void initWoodExpValues() {
        // Default experience values
        woodExpValues.put(Material.OAK_LOG, 5.0);
        woodExpValues.put(Material.SPRUCE_LOG, 5.0);
        woodExpValues.put(Material.BIRCH_LOG, 5.0);
        woodExpValues.put(Material.JUNGLE_LOG, 7.0);
        woodExpValues.put(Material.ACACIA_LOG, 6.0);
        woodExpValues.put(Material.DARK_OAK_LOG, 6.0);
        woodExpValues.put(Material.CHERRY_LOG, 7.0);
        woodExpValues.put(Material.MANGROVE_LOG, 7.0);
        woodExpValues.put(Material.CRIMSON_STEM, 8.0);
        woodExpValues.put(Material.WARPED_STEM, 8.0);
        woodExpValues.put(Material.BAMBOO, 2.0);

        // Load custom values from config if available
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection expSection = config.getConfigurationSection("experience.wood-values");

        if (expSection != null) {
            for (String key : expSection.getKeys(false)) {
                try {
                    Material material = Material.valueOf(key.toUpperCase());
                    double value = expSection.getDouble(key, 5.0);
                    woodExpValues.put(material, value);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid material in wood-values: " + key);
                }
            }
        }
    }

    @Override
    protected void registerAbilities() {
        // Register default abilities
        registerAbility(new LumberjackAbility(10));
        registerAbility(new TreeFellerAbility(30));
        registerAbility(new HarvestmasterAbility(50));

        // Load additional abilities from config
        loadConfiguredAbilities();
    }

    /**
     * Load additional abilities from config
     */
    private void loadConfiguredAbilities() {
        FileConfiguration config = YamlConfiguration.loadConfiguration(
                new File(plugin.getDataFolder(), "config/abilities/woodcutting_abilities.yml"));

        ConfigurationSection abilitiesSection = config.getConfigurationSection("abilities");
        if (abilitiesSection == null) {
            return;
        }

        for (String key : abilitiesSection.getKeys(false)) {
            ConfigurationSection abilitySection = abilitiesSection.getConfigurationSection(key);
            if (abilitySection == null) {
                continue;
            }

            Ability ability = loadAbilityFromConfig(abilitySection);
            if (ability != null) {
                registerAbility(ability);
            }
        }
    }

    /**
     * Handle block break for woodcutting
     * @param player the player
     * @param block the broken block
     */
    public void handleBlockBreak(Player player, Block block) {
        Material blockType = block.getType();

        // Check if it's a valid wood type
        if (!woodExpValues.containsKey(blockType)) {
            return;
        }

        // Get player level
        int level = plugin.getDataManager().getPlayerData(player.getUniqueId()).getSkillLevel(name);

        // Add base experience
        double baseExp = woodExpValues.getOrDefault(blockType, 5.0);
        addExperience(player, baseExp);

        // Apply Lumberjack ability (chance for double drops)
        Ability lumberjack = getAbility("lumberjack");
        if (lumberjack != null && level >= lumberjack.getUnlockLevel()) {
            double chance = lumberjack.getEffectForLevel(level);

            if (random.nextDouble() < chance) {
                // Trigger ability
                if (lumberjack.trigger(player, plugin, 0)) {
                    // Double drops logic would go here
                    // This would normally add extra items to the drops
                    addExperience(player, baseExp * 0.5);
                }
            }
        }

        // Apply TreeFeller ability (break connected logs)
        Ability treeFeller = getAbility("treefeller");
        if (treeFeller != null && level >= treeFeller.getUnlockLevel()) {
            // Only activate if player is sneaking (to avoid accidental triggers)
            if (player.isSneaking()) {
                // Check cooldown from ability
                if (treeFeller.canUse(player, plugin)) {
                    // Trigger ability with a fairly long cooldown
                    if (treeFeller.trigger(player, plugin, 60)) {
                        // Find connected logs
                        Set<Block> logs = findConnectedLogs(block, new HashSet<>(), 0,
                                getMaxLogsForLevel(level));

                        // Break all logs (would actually implement this differently)
                        for (Block log : logs) {
                            // This is a simple implementation
                            // In a real plugin, you'd handle drops and animation
                            log.breakNaturally(player.getInventory().getItemInMainHand());

                            // Add a small amount of experience for each log
                            addExperience(player, baseExp * 0.25);
                        }
                    }
                }
            }
        }

        // Apply Harvestmaster ability (saplings from leaves)
        // This would be implemented in a leaf decay listener
    }

    /**
     * Find connected logs of the same type
     * @param block the starting block
     * @param visited set of visited blocks
     * @param depth current recursion depth
     * @param maxLogs maximum number of logs to break
     * @return set of connected logs
     */
    private Set<Block> findConnectedLogs(Block block, Set<Block> visited, int depth, int maxLogs) {
        // Limit recursion depth and total logs
        if (depth > 100 || visited.size() >= maxLogs) {
            return visited;
        }

        // Get block material
        Material type = block.getType();

        // Check if this is a log
        if (isLog(type)) {
            visited.add(block);

            // Check surrounding blocks
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        // Skip center block (already added)
                        if (x == 0 && y == 0 && z == 0) {
                            continue;
                        }

                        Block neighbor = block.getRelative(x, y, z);

                        // Skip already visited blocks
                        if (visited.contains(neighbor)) {
                            continue;
                        }

                        // Recursively check neighbor
                        findConnectedLogs(neighbor, visited, depth + 1, maxLogs);
                    }
                }
            }
        }

        return visited;
    }

    /**
     * Get the maximum number of logs to break based on level
     * @param level the player's level
     * @return the maximum number of logs
     */
    private int getMaxLogsForLevel(int level) {
        // Base number is 10 logs
        int maxLogs = 10;

        // Add 1 log per 5 levels
        maxLogs += (level / 5);

        // Cap at 30 logs
        return Math.min(maxLogs, 30);
    }

    /**
     * Check if a material is a log
     * @param type the material type
     * @return true if it's a log
     */
    private boolean isLog(Material type) {
        return woodExpValues.containsKey(type);
    }

    /**
     * Get an ability
     * @param abilityName the ability name
     * @return the ability, or null if not found
     */
    public Ability getAbility(String abilityName) {
        return abilities.get(abilityName.toLowerCase());
    }
}