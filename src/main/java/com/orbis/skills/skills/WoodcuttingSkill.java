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

    

    public WoodcuttingSkill(OrbisSkills plugin) {
        super(plugin, "woodcutting");

       

        initWoodExpValues();
    }

    

    private void initWoodExpValues() {
       

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
       

        registerAbility(new LumberjackAbility(10));
        registerAbility(new TreeFellerAbility(30));
        registerAbility(new HarvestmasterAbility(50));

       

        loadConfiguredAbilities();
    }

    

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

    

    public void handleBlockBreak(Player player, Block block) {
        Material blockType = block.getType();

       

        if (!woodExpValues.containsKey(blockType)) {
            return;
        }

       

        int level = plugin.getDataManager().getPlayerData(player.getUniqueId()).getSkillLevel(name);

       

        double baseExp = woodExpValues.getOrDefault(blockType, 5.0);
        addExperience(player, baseExp);

       

        Ability lumberjack = getAbility("lumberjack");
        if (lumberjack != null && level >= lumberjack.getUnlockLevel()) {
            double chance = lumberjack.getEffectForLevel(level);

            if (random.nextDouble() < chance) {
               

                if (lumberjack.trigger(player, plugin, 0)) {
                   

                   

                    addExperience(player, baseExp * 0.5);
                }
            }
        }

       

        Ability treeFeller = getAbility("treefeller");
        if (treeFeller != null && level >= treeFeller.getUnlockLevel()) {
           

            if (player.isSneaking()) {
               

                if (treeFeller.canUse(player, plugin)) {
                   

                    if (treeFeller.trigger(player, plugin, 60)) {
                       

                        Set<Block> logs = findConnectedLogs(block, new HashSet<>(), 0,
                                getMaxLogsForLevel(level));

                       

                        for (Block log : logs) {
                           

                           

                            log.breakNaturally(player.getInventory().getItemInMainHand());

                           

                            addExperience(player, baseExp * 0.25);
                        }
                    }
                }
            }
        }

       

       

    }

    

    private Set<Block> findConnectedLogs(Block block, Set<Block> visited, int depth, int maxLogs) {
       

        if (depth > 100 || visited.size() >= maxLogs) {
            return visited;
        }

       

        Material type = block.getType();

       

        if (isLog(type)) {
            visited.add(block);

           

            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                       

                        if (x == 0 && y == 0 && z == 0) {
                            continue;
                        }

                        Block neighbor = block.getRelative(x, y, z);

                       

                        if (visited.contains(neighbor)) {
                            continue;
                        }

                       

                        findConnectedLogs(neighbor, visited, depth + 1, maxLogs);
                    }
                }
            }
        }

        return visited;
    }

    

    private int getMaxLogsForLevel(int level) {
       

        int maxLogs = 10;

       

        maxLogs += (level / 5);

       

        return Math.min(maxLogs, 30);
    }

    

    private boolean isLog(Material type) {
        return woodExpValues.containsKey(type);
    }

    

    public Ability getAbility(String abilityName) {
        return abilities.get(abilityName.toLowerCase());
    }
}