package com.orbis.skills.skills;

import com.orbis.skills.OrbisSkills;
import com.orbis.skills.abilities.Ability;
import com.orbis.skills.abilities.fishing.DoubleDropAbility;
import com.orbis.skills.abilities.fishing.ExperiencedFisherAbility;
import com.orbis.skills.abilities.fishing.MasterAnglerAbility;
import com.orbis.skills.abilities.fishing.TreasureHunterAbility;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class FishingSkill extends Skill {

    private final Random random = new Random();
    private final Map<String, Map<Material, Double>> tierDrops = new HashMap<>();
    private final Map<String, Map<String, CustomDrop>> tierCustomDrops = new HashMap<>();

    

    public FishingSkill(OrbisSkills plugin) {
        super(plugin, "fishing");

       

        loadFishingDrops();
    }

    @Override
    protected void registerAbilities() {
       

        registerAbility(new DoubleDropAbility(10));
        registerAbility(new ExperiencedFisherAbility(20));
        registerAbility(new TreasureHunterAbility(30));
        registerAbility(new MasterAnglerAbility(50));

       

        loadConfiguredAbilities();
    }

    

    private void loadConfiguredAbilities() {
        FileConfiguration config = YamlConfiguration.loadConfiguration(
                new File(plugin.getDataFolder(), "config/abilities/fishing_abilities.yml"));

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

    

    private void loadFishingDrops() {
        FileConfiguration dropsConfig = YamlConfiguration.loadConfiguration(
                new File(plugin.getDataFolder(), "config/drops/fishing_drops.yml"));

        ConfigurationSection specialDropsSection = dropsConfig.getConfigurationSection("special-drops");
        if (specialDropsSection == null) {
            plugin.getLogger().warning("No special drops section found in fishing_drops.yml!");
            return;
        }

        for (String tierKey : specialDropsSection.getKeys(false)) {
            ConfigurationSection tierSection = specialDropsSection.getConfigurationSection(tierKey);
            if (tierSection == null) {
                continue;
            }

            Map<Material, Double> materialDrops = new HashMap<>();
            Map<String, CustomDrop> customDrops = new HashMap<>();

           

            String levelRange = tierSection.getString("level-range", "1-100");
            String[] parts = levelRange.split("-");
            int minLevel = Integer.parseInt(parts[0]);
            int maxLevel = Integer.parseInt(parts[1]);

           

            ConfigurationSection dropsSection = tierSection.getConfigurationSection("drops");
            if (dropsSection != null) {
                for (String materialName : dropsSection.getKeys(false)) {
                    if (materialName.equals("custom-items")) {
                        continue;
                    }

                    try {
                        Material material = Material.valueOf(materialName);
                        double chance = dropsSection.getDouble(materialName, 0);
                        materialDrops.put(material, chance);
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Invalid material name in fishing drops: " + materialName);
                    }
                }
            }

           

            ConfigurationSection customItemsSection = dropsSection != null ?
                    dropsSection.getConfigurationSection("custom-items") : null;

            if (customItemsSection != null) {
                for (String itemKey : customItemsSection.getKeys(false)) {
                    ConfigurationSection itemSection = customItemsSection.getConfigurationSection(itemKey);
                    if (itemSection == null) {
                        continue;
                    }

                    String materialName = itemSection.getString("material", "CHEST");
                    Material material;
                    try {
                        material = Material.valueOf(materialName);
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Invalid material for custom item: " + materialName);
                        continue;
                    }

                    double chance = itemSection.getDouble("chance", 1.0);
                    String name = itemSection.getString("name", itemKey);

                    CustomDrop customDrop = new CustomDrop(material, chance, name);

                   

                    if (itemSection.isList("lore")) {
                        for (String loreLine : itemSection.getStringList("lore")) {
                            customDrop.addLoreLine(loreLine);
                        }
                    }

                   

                    ConfigurationSection nbtSection = itemSection.getConfigurationSection("nbt");
                    if (nbtSection != null) {
                        for (String nbtKey : nbtSection.getKeys(false)) {
                            customDrop.addNbtTag(nbtKey, nbtSection.getString(nbtKey));
                        }
                    }

                    customDrops.put(itemKey, customDrop);
                }
            }

            tierDrops.put(tierKey, materialDrops);
            tierCustomDrops.put(tierKey, customDrops);

            plugin.getLogger().info("Loaded " + materialDrops.size() + " material drops and "
                    + customDrops.size() + " custom drops for tier " + tierKey
                    + " (levels " + minLevel + "-" + maxLevel + ")");
        }
    }

    

    public void handleFishCaught(Player player, double exp) {
       

        addExperience(player, exp);

       

        if (abilities.containsKey("doubledrop")) {
            Ability doubleDropAbility = abilities.get("doubledrop");
            int playerLevel = plugin.getDataManager().getPlayerData(player.getUniqueId()).getSkillLevel(name);

            if (playerLevel >= doubleDropAbility.getUnlockLevel() &&
                    random.nextDouble() < doubleDropAbility.getEffectForLevel(playerLevel)) {
               

            }
        }
    }

    

    public ItemStack handleSpecialDrop(Player player) {
        int playerLevel = plugin.getDataManager().getPlayerData(player.getUniqueId()).getSkillLevel(name);

       

        String tierToUse = null;
        for (String tierKey : tierDrops.keySet()) {
           

            ConfigurationSection tierSection = plugin.getConfigManager()
                    .getDropsConfig("fishing_drops").getConfigurationSection("special-drops." + tierKey);

            if (tierSection == null) {
                continue;
            }

            String levelRange = tierSection.getString("level-range", "1-100");
            String[] parts = levelRange.split("-");
            int minLevel = Integer.parseInt(parts[0]);
            int maxLevel = Integer.parseInt(parts[1]);

            if (playerLevel >= minLevel && playerLevel <= maxLevel) {
                tierToUse = tierKey;
                break;
            }
        }

        if (tierToUse == null || !tierDrops.containsKey(tierToUse)) {
            return null;
        }

       

        double dropChanceMultiplier = 1.0;
        if (abilities.containsKey("treasurehunter")) {
            Ability treasureHunterAbility = abilities.get("treasurehunter");
            if (playerLevel >= treasureHunterAbility.getUnlockLevel()) {
                dropChanceMultiplier += treasureHunterAbility.getEffectForLevel(playerLevel);
            }
        }

       

        Map<Material, Double> drops = tierDrops.get(tierToUse);
        for (Map.Entry<Material, Double> entry : drops.entrySet()) {
            double chance = entry.getValue() * dropChanceMultiplier;
            if (random.nextDouble() * 100 < chance) {
                return new ItemStack(entry.getKey());
            }
        }

       

        Map<String, CustomDrop> customDrops = tierCustomDrops.get(tierToUse);
        for (CustomDrop customDrop : customDrops.values()) {
            double chance = customDrop.getChance() * dropChanceMultiplier;
            if (random.nextDouble() * 100 < chance) {
                return customDrop.createItemStack();
            }
        }

        return null;
    }

    

    private static class CustomDrop {
        private final Material material;
        private final double chance;
        private final String name;
        private final Map<String, String> nbtTags = new HashMap<>();
        private final Map<Integer, String> loreLines = new HashMap<>();
        private int loreIndex = 0;

        public CustomDrop(Material material, double chance, String name) {
            this.material = material;
            this.chance = chance;
            this.name = name;
        }

        public Material getMaterial() {
            return material;
        }

        public double getChance() {
            return chance;
        }

        public String getName() {
            return name;
        }

        public void addNbtTag(String key, String value) {
            nbtTags.put(key, value);
        }

        public void addLoreLine(String line) {
            loreLines.put(loreIndex++, line);
        }

        public ItemStack createItemStack() {
            ItemStack item = new ItemStack(material);
           

           

            return item;
        }
    }
}