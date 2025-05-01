package com.orbis.skills.skills;

import com.orbis.skills.OrbisSkills;
import com.orbis.skills.abilities.Ability;
import com.orbis.skills.events.SkillLevelUpEvent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class Skill {

    protected final OrbisSkills plugin;
    protected final String name;
    protected final String displayName;
    protected final Map<String, Ability> abilities = new HashMap<>();

    public Skill(OrbisSkills plugin, String name) {
        this.plugin = plugin;
        this.name = name;
        this.displayName = plugin.getConfig().getString("skills.display-names." + name,
                name.substring(0, 1).toUpperCase() + name.substring(1));

        

        registerAbilities();
    }

    

    protected abstract void registerAbilities();

    

    public String getName() {
        return name;
    }

    

    public String getDisplayName() {
        return displayName;
    }

    

    public void addExperience(Player player, double amount) {
        UUID uuid = player.getUniqueId();
        int oldLevel = plugin.getDataManager().getPlayerData(uuid).getSkillLevel(name);

       

        double multiplier = plugin.getConfig().getDouble("settings.exp-multiplier", 1.0);
       

        if (player.hasPermission("orbisskills." + name + ".multiplier.2")) {
            multiplier = 2.0;
        } else if (player.hasPermission("orbisskills." + name + ".multiplier.1.5")) {
            multiplier = 1.5;
        }

       

        plugin.getDataManager().addExperience(uuid, name, amount * multiplier);

       

        int newLevel = plugin.getDataManager().getPlayerData(uuid).getSkillLevel(name);
        if (newLevel > oldLevel) {
            handleLevelUp(player, oldLevel, newLevel);
        }
    }

    

    protected void handleLevelUp(Player player, int oldLevel, int newLevel) {
       

        SkillLevelUpEvent event = new SkillLevelUpEvent(player, this, oldLevel, newLevel);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return;
        }

       

        if (plugin.getConfig().getBoolean("settings.level-up-messages", true)) {
            player.sendMessage(plugin.getConfigManager().getColoredString("messages.level-up")
                    .replace("{skill}", displayName)
                    .replace("{level}", String.valueOf(newLevel)));
        }

       

        if (plugin.getConfig().getBoolean("settings.level-up-titles", true)) {
            String title = plugin.getConfigManager().getColoredString("messages.level-up-title")
                    .replace("{skill}", displayName)
                    .replace("{level}", String.valueOf(newLevel));

            String subtitle = plugin.getConfigManager().getColoredString("messages.level-up-subtitle")
                    .replace("{skill}", displayName)
                    .replace("{level}", String.valueOf(newLevel));

            player.sendTitle(title, subtitle, 10, 70, 20);
        }

       

        if (plugin.getConfig().getBoolean("settings.level-up-sounds", true)) {
            String soundName = plugin.getConfig().getString("sounds.level-up", "ENTITY_PLAYER_LEVELUP");
            try {
                Sound sound = Sound.valueOf(soundName);
                player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid sound name in config: " + soundName);
            }
        }

       

        checkAbilityUnlocks(player, newLevel);
    }

    

    private void checkAbilityUnlocks(Player player, int level) {
        for (Ability ability : abilities.values()) {
            if (ability.getUnlockLevel() == level) {
                player.sendMessage(plugin.getConfigManager().getColoredString("messages.ability-unlock")
                        .replace("{ability}", ability.getName())
                        .replace("{skill}", displayName));
            }
        }
    }

    

    public boolean hasAbility(String abilityName) {
        return abilities.containsKey(abilityName.toLowerCase());
    }

    

    public Ability getAbility(String abilityName) {
        return abilities.get(abilityName.toLowerCase());
    }

    

    public String getAbilityInfo(Player player, String abilityName) {
        if (!hasAbility(abilityName)) {
            return "0";
        }

        Ability ability = abilities.get(abilityName.toLowerCase());
        int playerLevel = plugin.getDataManager().getPlayerData(player.getUniqueId()).getSkillLevel(name);

        if (playerLevel < ability.getUnlockLevel()) {
            return "Locked";
        }

        return ability.getInfoForLevel(playerLevel);
    }

    

    protected void registerAbility(Ability ability) {
        abilities.put(ability.getName().toLowerCase(), ability);
    }

    

    protected Ability loadAbilityFromConfig(ConfigurationSection section) {
        if (section == null) {
            return null;
        }

        String abilityName = section.getName();
        int unlockLevel = section.getInt("unlock-level", 1);
        String description = section.getString("description", "No description available");

       

        return new Ability(abilityName, unlockLevel, description);
    }
}