package com.orbis.skills.skills;

import com.orbis.skills.OrbisSkills;
import com.orbis.skills.abilities.Ability;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;



public class CustomSkill extends Skill {

    private final List<String> sources = new ArrayList<>();
    private final List<String> triggers = new ArrayList<>();
    private final List<String> triggerMaterials = new ArrayList<>();
    private final double baseExpValue;
    private final Map<String, CustomAbilityInfo> customAbilityInfo = new HashMap<>();
    private final Random random = new Random();

    

    public CustomSkill(OrbisSkills plugin, String name, ConfigurationSection config) {
        super(plugin, name);

       

        if (config.isList("sources")) {
            sources.addAll(config.getStringList("sources"));
        }

        if (config.isList("triggers")) {
            triggers.addAll(config.getStringList("triggers"));
        }

        if (config.isList("trigger-materials")) {
            triggerMaterials.addAll(config.getStringList("trigger-materials"));
        }

       

        baseExpValue = config.getDouble("base-exp", 5.0);
    }

    @Override
    protected void registerAbilities() {
       

        File file = new File(plugin.getDataFolder(), "config/abilities/" + name + "_abilities.yml");
        if (!file.exists()) {
           

            createDefaultAbilitiesFile(file);
        }

       

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection abilitiesSection = config.getConfigurationSection("abilities");

        if (abilitiesSection != null) {
            for (String key : abilitiesSection.getKeys(false)) {
                ConfigurationSection abilitySection = abilitiesSection.getConfigurationSection(key);
                if (abilitySection != null) {
                    if (!abilitySection.getBoolean("enabled", true)) {
                        continue;
                    }

                   

                    Ability ability = loadAbilityFromConfig(abilitySection);
                    if (ability != null) {
                        registerAbility(ability);

                       

                        CustomAbilityInfo info = new CustomAbilityInfo();
                        info.setName(abilitySection.getString("name", key));
                        info.setType(abilitySection.getString("type", "passive"));
                        info.setCooldown(abilitySection.getInt("cooldown", 0));
                        info.setRadius(abilitySection.getInt("radius", 0));
                        info.setDuration(abilitySection.getInt("duration", 0));

                       

                        ConfigurationSection messagesSection = abilitySection.getConfigurationSection("messages");
                        if (messagesSection != null) {
                            for (String messageKey : messagesSection.getKeys(false)) {
                                info.addMessage(messageKey, messagesSection.getString(messageKey));
                            }
                        }

                       

                        ConfigurationSection paramsSection = abilitySection.getConfigurationSection("parameters");
                        if (paramsSection != null) {
                            for (String paramKey : paramsSection.getKeys(false)) {
                                info.addParameter(paramKey, paramsSection.getString(paramKey));
                            }
                        }

                        customAbilityInfo.put(key.toLowerCase(), info);
                    }
                }
            }
        }
    }

    

    private void createDefaultAbilitiesFile(File file) {
        try {
           

            file.getParentFile().mkdirs();

           

            FileConfiguration config = new YamlConfiguration();
            ConfigurationSection abilitiesSection = config.createSection("abilities");

           

            ConfigurationSection sampleAbility = abilitiesSection.createSection("sample");
            sampleAbility.set("enabled", true);
            sampleAbility.set("unlock-level", 10);
            sampleAbility.set("name", "&e" + getName() + " Sample Ability");
            sampleAbility.set("description", "A sample ability for the " + getDisplayName() + " skill");
            sampleAbility.set("type", "passive");

           

            ConfigurationSection effectsSection = sampleAbility.createSection("effects");
            effectsSection.set("10", 0.05);
            effectsSection.set("20", 0.10);
            effectsSection.set("30", 0.15);
            effectsSection.set("40", 0.20);
            effectsSection.set("50", 0.25);

           

            ConfigurationSection messagesSection = sampleAbility.createSection("messages");
            messagesSection.set("activate", "&aYour " + getName() + " ability activated!");
            messagesSection.set("cooldown", "&cYou must wait {time} before using this ability again!");

           

            config.save(file);

            plugin.getLogger().info("Created default abilities file for " + name);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to create default abilities file for " + name);
            e.printStackTrace();
        }
    }

    

    public void handleTrigger(Player player, String triggerType, String material) {
       

        if (player == null) {
            return;
        }

       

        if (!triggers.contains(triggerType)) {
            return;
        }

       

        if (material != null && !triggerMaterials.isEmpty() && !triggerMaterials.contains(material)) {
            return;
        }

       

        addExperience(player, baseExpValue);

       

        handleAbilityEffects(player, triggerType, material);
    }

    

    private void handleAbilityEffects(Player player, String triggerType, String material) {
        int playerLevel = plugin.getDataManager().getPlayerData(player.getUniqueId()).getSkillLevel(name);

       

        for (String abilityName : abilities.keySet()) {
            Ability ability = abilities.get(abilityName);
            CustomAbilityInfo info = customAbilityInfo.get(abilityName);

           

            if (info == null || playerLevel < ability.getUnlockLevel()) {
                continue;
            }

           

            if (info.getType().equalsIgnoreCase("passive")) {
               

                double chance = ability.getEffectForLevel(playerLevel);
                if (random.nextDouble() < chance) {
                    if (ability.trigger(player, plugin, 0)) {
                       

                        String message = info.getMessage("activate");
                        if (message != null && !message.isEmpty()) {
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                        }

                       

                        applyPassiveEffect(player, info, triggerType, material);
                    }
                }
            } else if (info.getType().equalsIgnoreCase("active")) {
               

                int cooldown = info.getCooldown();
                if (cooldown > 0 && ability.canUse(player, plugin)) {
                   

                    if (player.isSneaking()) {
                        if (ability.trigger(player, plugin, cooldown)) {
                           

                            String message = info.getMessage("activate");
                            if (message != null && !message.isEmpty()) {
                                player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                            }

                           

                            applyActiveEffect(player, info, triggerType, material);
                        }
                    }
                }
            } else if (info.getType().equalsIgnoreCase("aoe")) {
               

                int cooldown = info.getCooldown();
                if (cooldown > 0 && ability.canUse(player, plugin)) {
                   

                    if (player.isSneaking()) {
                        if (ability.trigger(player, plugin, cooldown)) {
                           

                            String message = info.getMessage("activate");
                            if (message != null && !message.isEmpty()) {
                                player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                            }

                           

                            applyAoeEffect(player, info, triggerType, material);
                        }
                    }
                }
            }
        }
    }

    

    private void applyPassiveEffect(Player player, CustomAbilityInfo info, String triggerType, String material) {
       

        if (triggerType.equals("BREAK") || triggerType.equals("HARVEST") ||
                triggerType.equals("CRAFT") || triggerType.equals("SMELT")) {
           

            if (material != null) {
                try {
                    Material mat = Material.valueOf(material);
                   

                    player.getInventory().addItem(new ItemStack(mat, 1));
                } catch (IllegalArgumentException ignored) {
                   

                }
            }
        } else if (triggerType.equals("CONSUME")) {
           

           

           

            player.setFoodLevel(Math.min(player.getFoodLevel() + 1, 20));
        }
    }

    

    private void applyActiveEffect(Player player, CustomAbilityInfo info, String triggerType, String material) {
       

       


        int duration = info.getDuration();
        if (duration > 0) {
           

           

            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
               

                String message = info.getMessage("end");
                if (message != null && !message.isEmpty()) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                }
            }, duration * 20L);
        }
    }

    

    private void applyAoeEffect(Player player, CustomAbilityInfo info, String triggerType, String material) {
        int radius = info.getRadius();
        if (radius <= 0) {
            radius = 5;

        }

       

        player.getWorld().getNearbyEntities(player.getLocation(), radius, radius, radius).forEach(entity -> {
            if (entity instanceof Player && entity != player) {
                Player nearby = (Player) entity;
               

               

                String message = info.getParameter("nearbyMessage");
                if (message != null && !message.isEmpty()) {
                    nearby.sendMessage(ChatColor.translateAlternateColorCodes('&', message)
                            .replace("{player}", player.getName()));
                }
            }
        });
    }

    

    public List<String> getSources() {
        return sources;
    }

    

    public List<String> getTriggers() {
        return triggers;
    }

    

    public List<String> getTriggerMaterials() {
        return triggerMaterials;
    }

    

    public double getBaseExpValue() {
        return baseExpValue;
    }

    

    private static class CustomAbilityInfo {
        private String name;
        private String type;
        private int cooldown;
        private int radius;
        private int duration;
        private final Map<String, String> messages = new HashMap<>();
        private final Map<String, String> parameters = new HashMap<>();

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }

        public void setCooldown(int cooldown) {
            this.cooldown = cooldown;
        }

        public int getCooldown() {
            return cooldown;
        }

        public void setRadius(int radius) {
            this.radius = radius;
        }

        public int getRadius() {
            return radius;
        }

        public void setDuration(int duration) {
            this.duration = duration;
        }

        public int getDuration() {
            return duration;
        }

        public void addMessage(String key, String message) {
            messages.put(key.toLowerCase(), message);
        }

        public String getMessage(String key) {
            return messages.get(key.toLowerCase());
        }

        public void addParameter(String key, String value) {
            parameters.put(key.toLowerCase(), value);
        }

        public String getParameter(String key) {
            return parameters.get(key.toLowerCase());
        }
    }
}