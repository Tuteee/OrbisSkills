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

    

    public FarmingSkill(OrbisSkills plugin) {
        super(plugin, "farming");

       

        initCropExpValues();
    }

    

    private void initCropExpValues() {
       

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
       

        registerAbility(new GreenThumbAbility(10));
        registerAbility(new BountifulHarvestAbility(30));
        registerAbility(new NaturesBlessingAbility(50));

       

        loadConfiguredAbilities();
    }

    

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

    

    public boolean handleHarvest(Player player, Block block) {
        Material cropType = block.getType();

       

        if (!cropExpValues.containsKey(cropType)) {
            return false;
        }

       

        if (block.getBlockData() instanceof Ageable) {
            Ageable ageable = (Ageable) block.getBlockData();
            if (ageable.getAge() < ageable.getMaximumAge()) {
                return false;
            }
        }

       

        int level = plugin.getDataManager().getPlayerData(player.getUniqueId()).getSkillLevel(name);

       

        double baseExp = cropExpValues.getOrDefault(cropType, 5.0);
        addExperience(player, baseExp);

       

        Ability greenThumb = getAbility("greenthumb");
        if (greenThumb != null && level >= greenThumb.getUnlockLevel()) {
            double chance = greenThumb.getEffectForLevel(level);

            if (random.nextDouble() < chance) {
               

                if (greenThumb.trigger(player, plugin, 0)) {
                   

                   

                    addExperience(player, baseExp * 0.5);
                }
            }
        }

       

        Ability bountifulHarvest = getAbility("bountifulharvest");
        if (bountifulHarvest != null && level >= bountifulHarvest.getUnlockLevel()) {
            double chance = bountifulHarvest.getEffectForLevel(level);

            if (random.nextDouble() < chance) {
               

                if (bountifulHarvest.trigger(player, plugin, 0)) {
                   

                   

                    addExperience(player, baseExp * 0.5);
                }
            }
        }

        return false;
    }

    

    public void handlePlanting(Player player, Block block, ItemStack item) {
       

        int level = plugin.getDataManager().getPlayerData(player.getUniqueId()).getSkillLevel(name);

       

        addExperience(player, 1.0);

       

        Ability naturesBlessing = getAbility("naturesblessing");
        if (naturesBlessing != null && level >= naturesBlessing.getUnlockLevel()) {
            double chance = naturesBlessing.getEffectForLevel(level);

            if (random.nextDouble() < chance) {
               

                if (naturesBlessing.trigger(player, plugin, 0)) {
                   

                   

                    addExperience(player, 5.0);
                }
            }
        }
    }

    

    public Ability getAbility(String abilityName) {
        return abilities.get(abilityName.toLowerCase());
    }
}