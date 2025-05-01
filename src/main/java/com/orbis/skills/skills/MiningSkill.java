package com.orbis.skills.skills;

import com.orbis.skills.OrbisSkills;
import com.orbis.skills.abilities.Ability;
import com.orbis.skills.abilities.mining.DoubleOreAbility;
import com.orbis.skills.abilities.mining.SuperBreakerAbility;
import com.orbis.skills.abilities.mining.VeinMinerAbility;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class MiningSkill extends Skill {

    private final Random random = new Random();
    private final Map<Material, Double> oreExpValues = new HashMap<>();
    private final Map<Material, Material> oreToDrop = new HashMap<>();

    

    public MiningSkill(OrbisSkills plugin) {
        super(plugin, "mining");

       

        initOreExpValues();

       

        initOreToDrop();
    }

    

    private void initOreExpValues() {
       

        oreExpValues.put(Material.COAL_ORE, 5.0);
        oreExpValues.put(Material.DEEPSLATE_COAL_ORE, 5.5);
        oreExpValues.put(Material.IRON_ORE, 7.0);
        oreExpValues.put(Material.DEEPSLATE_IRON_ORE, 7.5);
        oreExpValues.put(Material.COPPER_ORE, 6.0);
        oreExpValues.put(Material.DEEPSLATE_COPPER_ORE, 6.5);
        oreExpValues.put(Material.GOLD_ORE, 10.0);
        oreExpValues.put(Material.DEEPSLATE_GOLD_ORE, 10.5);
        oreExpValues.put(Material.REDSTONE_ORE, 8.0);
        oreExpValues.put(Material.DEEPSLATE_REDSTONE_ORE, 8.5);
        oreExpValues.put(Material.LAPIS_ORE, 10.0);
        oreExpValues.put(Material.DEEPSLATE_LAPIS_ORE, 10.5);
        oreExpValues.put(Material.DIAMOND_ORE, 15.0);
        oreExpValues.put(Material.DEEPSLATE_DIAMOND_ORE, 15.5);
        oreExpValues.put(Material.EMERALD_ORE, 20.0);
        oreExpValues.put(Material.DEEPSLATE_EMERALD_ORE, 20.5);
        oreExpValues.put(Material.NETHER_GOLD_ORE, 8.0);
        oreExpValues.put(Material.NETHER_QUARTZ_ORE, 6.0);
        oreExpValues.put(Material.ANCIENT_DEBRIS, 25.0);

       

        FileConfiguration config = plugin.getConfig();
        ConfigurationSection expSection = config.getConfigurationSection("experience.ore-values");

        if (expSection != null) {
            for (String key : expSection.getKeys(false)) {
                try {
                    Material material = Material.valueOf(key.toUpperCase());
                    double value = expSection.getDouble(key, 5.0);
                    oreExpValues.put(material, value);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid material in ore-values: " + key);
                }
            }
        }
    }

    

    private void initOreToDrop() {
       

        oreToDrop.put(Material.COAL_ORE, Material.COAL);
        oreToDrop.put(Material.DEEPSLATE_COAL_ORE, Material.COAL);
        oreToDrop.put(Material.IRON_ORE, Material.RAW_IRON);
        oreToDrop.put(Material.DEEPSLATE_IRON_ORE, Material.RAW_IRON);
        oreToDrop.put(Material.COPPER_ORE, Material.RAW_COPPER);
        oreToDrop.put(Material.DEEPSLATE_COPPER_ORE, Material.RAW_COPPER);
        oreToDrop.put(Material.GOLD_ORE, Material.RAW_GOLD);
        oreToDrop.put(Material.DEEPSLATE_GOLD_ORE, Material.RAW_GOLD);
        oreToDrop.put(Material.REDSTONE_ORE, Material.REDSTONE);
        oreToDrop.put(Material.DEEPSLATE_REDSTONE_ORE, Material.REDSTONE);
        oreToDrop.put(Material.LAPIS_ORE, Material.LAPIS_LAZULI);
        oreToDrop.put(Material.DEEPSLATE_LAPIS_ORE, Material.LAPIS_LAZULI);
        oreToDrop.put(Material.DIAMOND_ORE, Material.DIAMOND);
        oreToDrop.put(Material.DEEPSLATE_DIAMOND_ORE, Material.DIAMOND);
        oreToDrop.put(Material.EMERALD_ORE, Material.EMERALD);
        oreToDrop.put(Material.DEEPSLATE_EMERALD_ORE, Material.EMERALD);
        oreToDrop.put(Material.NETHER_GOLD_ORE, Material.GOLD_NUGGET);
        oreToDrop.put(Material.NETHER_QUARTZ_ORE, Material.QUARTZ);
        oreToDrop.put(Material.ANCIENT_DEBRIS, Material.NETHERITE_SCRAP);
    }

    @Override
    protected void registerAbilities() {
       

        registerAbility(new DoubleOreAbility(10));
        registerAbility(new SuperBreakerAbility(30));
        registerAbility(new VeinMinerAbility(50));

       

        loadConfiguredAbilities();
    }

    

    private void loadConfiguredAbilities() {
        FileConfiguration config = YamlConfiguration.loadConfiguration(
                new File(plugin.getDataFolder(), "config/abilities/mining_abilities.yml"));

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

       

        if (!oreExpValues.containsKey(blockType)) {
            return;
        }

       

        int level = plugin.getDataManager().getPlayerData(player.getUniqueId()).getSkillLevel(name);

       

        double baseExp = oreExpValues.getOrDefault(blockType, 5.0);
        addExperience(player, baseExp);

       

        Ability doubleOre = getAbility("doubleore");
        if (doubleOre != null && level >= doubleOre.getUnlockLevel()) {
            double chance = doubleOre.getEffectForLevel(level);

            if (random.nextDouble() < chance) {
               

                if (doubleOre.trigger(player, plugin, 0)) {
                   

                    Material dropType = oreToDrop.get(blockType);
                    if (dropType != null) {
                        player.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(dropType));
                    }
                }
            }
        }

       

        Ability superBreaker = getAbility("superbreaker");
        if (superBreaker != null && level >= superBreaker.getUnlockLevel()) {
           

            if (player.isSneaking()) {
               

                if (superBreaker.canUse(player, plugin)) {
                   

                    if (superBreaker.trigger(player, plugin, 180)) {
                       

                        int effectLevel = Math.min(level / 20, 2);

                        player.addPotionEffect(new PotionEffect(
                                PotionEffectType.HASTE,
                                20 * 30,

                                effectLevel));
                    }
                }
            }
        }

       

        Ability veinMiner = getAbility("veinminer");
        if (veinMiner != null && level >= veinMiner.getUnlockLevel()) {
           

            if (player.isSneaking()) {
               

                if (veinMiner.canUse(player, plugin)) {
                   

                    if (veinMiner.trigger(player, plugin, 120)) {
                       

                        Set<Block> ores = findConnectedOres(block, new HashSet<>(), 0,
                                getMaxOresForLevel(level), blockType);

                       

                        for (Block ore : ores) {
                           

                            ore.breakNaturally(player.getInventory().getItemInMainHand());
                            addExperience(player, baseExp * 0.25);
                        }
                    }
                }
            }
        }
    }

    

    private Set<Block> findConnectedOres(Block block, Set<Block> visited, int depth, int maxOres, Material targetType) {
       

        if (depth > 50 || visited.size() >= maxOres) {
            return visited;
        }

       

        Material type = block.getType();

       

        if (type == targetType) {
            visited.add(block);

           

            int[] dx = {0, 0, 0, 0, 1, -1};
            int[] dy = {1, -1, 0, 0, 0, 0};
            int[] dz = {0, 0, 1, -1, 0, 0};

            for (int i = 0; i < 6; i++) {
                Block neighbor = block.getRelative(dx[i], dy[i], dz[i]);

               

                if (visited.contains(neighbor)) {
                    continue;
                }

               

                findConnectedOres(neighbor, visited, depth + 1, maxOres, targetType);
            }
        }

        return visited;
    }

    

    private int getMaxOresForLevel(int level) {
       

        int maxOres = 5;

       

        maxOres += (level / 10);

       

        return Math.min(maxOres, 15);
    }

    

    public Ability getAbility(String abilityName) {
        return abilities.get(abilityName.toLowerCase());
    }
}