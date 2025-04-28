package com.orbis.skills.skills;

import com.orbis.skills.OrbisSkills;
import com.orbis.skills.abilities.Ability;
import com.orbis.skills.abilities.farming.BountifulHarvestAbility;
import com.orbis.skills.abilities.farming.GreenThumbAbility;
import com.orbis.skills.abilities.farming.NaturesBlessingAbility;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class FarmingSkill extends Skill {

    private final Random random = new Random();
    private final Map<Material, Double> cropExpValues = new HashMap<>();

    /**
     * Create a new farming skill
     * @param plugin the plugin instance
     */
    public FarmingSkill(OrbisSkills plugin) {
        super(plugin, "farming");

        // Initialize crop experience values
        initCropExpValues();
    }

    /**
     * Initialize crop experience values
     */
    private void initCropExpValues() {
        // Default experience values
        cropExpValues.put(Material.WHEAT, 7.5);
        cropExpValues.put(Material.POTATOES, 7.0);
        cropExpValues.put(Material.CARROTS, 7.0);
        cropExpValues.put(Material.BEETROOTS, 8.0);
        cropExpValues.put(Material.NETHER_WART, 10.0);
        cropExpValues.put(Material.COCOA, 8.0);
        cropExpValues.put(Material.SWEET_BERRY_BUSH, 6.0);
        cropExpValues.put(Material.MELON, 5.0);
        cropExpValues.put(Material.PUMPKIN, 5.0);
        cropExpValues.put(Material.SUGAR_CANE, 3.5);
        cropExpValues.put(Material.BAMBOO, 3.0);
        cropExpValues.put(Material.CACTUS, 4.0);
        cropExpValues.put(Material.KELP, 3.0);
        cropExpValues.put(Material.SEA_PICKLE, 5.0);

        // Load custom values from config if available
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection expSection = config.getConfigurationSection("experience.crop-values");

        if (expSection != null) {
            for (String key : expSection.getKeys(false)) {
                try {
                    Material material = Material.valueOf(key.toUpperCase());
                    double value = expSection.getDouble(key, 5.0);
                    cropExpValues.put(material, value);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid material in crop-values: " + key);
                }
            }
        }
    }

    @Override
    protected void registerAbilities() {
        // Register default abilities
        registerAbility(new GreenThumbAbility(10));
        registerAbility(new BountifulHarvestAbility(30));
        registerAbility(new NaturesBlessingAbility(50));

        // Load additional abilities from config
        loadConfiguredAbilities();
    }

    /**
     * Load additional abilities from config
     */
    private void loadConfiguredAbilities() {
        FileConfiguration config = YamlConfiguration.loadConfiguration(
                new File(plugin.getDataFolder(), "config/abilities/farming_abilities.yml"));

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
     * Handle crop harvesting
     * @param player the player
     * @param block the harvested block
     * @return true if the event should be cancelled
     */
    public boolean handleHarvest(Player player, Block block) {
        Material cropType = block.getType();

        // Check if it's a valid crop
        if (!cropExpValues.containsKey(cropType)) {
            return false;
        }

        // Check if crop is fully grown (for Ageable blocks)
        if (block.getBlockData() instanceof Ageable) {
            Ageable ageable = (Ageable) block.getBlockData();
            if (ageable.getAge() < ageable.getMaximumAge()) {
                return false;
            }
        }

        // Get player level
        int level = plugin.getDataManager().getPlayerData(player.getUniqueId()).getSkillLevel(name);

        // Add base experience
        double baseExp = cropExpValues.getOrDefault(cropType, 5.0);
        addExperience(player, baseExp);

        // Apply GreenThumb ability (chance for auto-replant)
        Ability greenThumb = getAbility("greenthumb");
        if (greenThumb != null && level >= greenThumb.getUnlockLevel()) {
            double chance = greenThumb.getEffectForLevel(level);

            if (random.nextDouble() < chance) {
                // Trigger ability
                if (greenThumb.trigger(player, plugin, 0)) {
                    // Auto-replant logic would go here
                    // For now, we'll just give some extra experience
                    addExperience(player, baseExp * 0.5);
                }
            }
        }

        // Apply BountifulHarvest ability (chance for extra drops)
        Ability bountifulHarvest = getAbility("bountifulharvest");
        if (bountifulHarvest != null && level >= bountifulHarvest.getUnlockLevel()) {
            double chance = bountifulHarvest.getEffectForLevel(level);

            if (random.nextDouble() < chance) {
                // Trigger ability
                if (bountifulHarvest.trigger(player, plugin, 0)) {
                    // Logic for extra drops would go here
                    // This would normally modify the drops
                    addExperience(player, baseExp * 0.5);
                }
            }
        }

        return false;
    }

    /**
     * Handle planting crops
     * @param player the player
     * @param block the block being planted on
     * @param item the item being planted
     */
    public void handlePlanting(Player player, Block block, ItemStack item) {
        // Get player level
        int level = plugin.getDataManager().getPlayerData(player.getUniqueId()).getSkillLevel(name);

        // Add a small amount of experience for planting
        addExperience(player, 1.0);

        // Apply NaturesBlessing ability (chance for instant growth)
        Ability naturesBlessing = getAbility("naturesblessing");
        if (naturesBlessing != null && level >= naturesBlessing.getUnlockLevel()) {
            double chance = naturesBlessing.getEffectForLevel(level);

            if (random.nextDouble() < chance) {
                // Trigger ability
                if (naturesBlessing.trigger(player, plugin, 0)) {
                    // Logic for instant growth would go here
                    // This would normally accelerate crop growth
                    addExperience(player, 5.0);
                }
            }
        }
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