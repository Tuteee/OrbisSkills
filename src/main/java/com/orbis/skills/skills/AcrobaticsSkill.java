package com.orbis.skills.skills;

import com.orbis.skills.OrbisSkills;
import com.orbis.skills.abilities.Ability;
import com.orbis.skills.abilities.acrobatics.GracefulLandingAbility;
import com.orbis.skills.abilities.acrobatics.RollAbility;
import com.orbis.skills.abilities.acrobatics.SafeFallAbility;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Random;

public class AcrobaticsSkill extends Skill {

    private final Random random = new Random();

    

    public AcrobaticsSkill(OrbisSkills plugin) {
        super(plugin, "acrobatics");
    }

    @Override
    protected void registerAbilities() {
       

        registerAbility(new RollAbility(10));
        registerAbility(new SafeFallAbility(30));
        registerAbility(new GracefulLandingAbility(50));

       

        loadConfiguredAbilities();
    }

    

    private void loadConfiguredAbilities() {
        FileConfiguration config = YamlConfiguration.loadConfiguration(
                new File(plugin.getDataFolder(), "config/abilities/acrobatics_abilities.yml"));

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

    

    public double handleFallDamage(Player player, double damage) {
        if (damage <= 0) {
            return 0;
        }

        int level = plugin.getDataManager().getPlayerData(player.getUniqueId()).getSkillLevel(name);

       

        double baseReduction = Math.min(0.005 * level, 0.25);
        damage = damage * (1 - baseReduction);

       

        Ability rollAbility = getAbility("roll");
        if (rollAbility != null && level >= rollAbility.getUnlockLevel()) {
            double rollChance = rollAbility.getEffectForLevel(level);

            if (random.nextDouble() < rollChance) {
               

                if (rollAbility.trigger(player, plugin, 0)) {
                   

                    damage *= 0.5;
                }
            }
        }

       

        Ability safeFallAbility = getAbility("safefall");
        if (safeFallAbility != null && level >= safeFallAbility.getUnlockLevel()) {
            double safeFallChance = safeFallAbility.getEffectForLevel(level);

            if (random.nextDouble() < safeFallChance) {
               

                if (safeFallAbility.trigger(player, plugin, 0)) {
                   

                    return 0;
                }
            }
        }

       

        Ability gracefulLandingAbility = getAbility("gracefullanding");
        if (gracefulLandingAbility != null && level >= gracefulLandingAbility.getUnlockLevel()) {
            double expMultiplier = gracefulLandingAbility.getEffectForLevel(level);

           

            if (damage > 0 && random.nextDouble() < 0.75) {
               

                if (gracefulLandingAbility.trigger(player, plugin, 0)) {
                   

                    double baseExp = plugin.getConfig().getDouble("experience.base-values.acrobatics", 5);
                    addExperience(player, baseExp * Math.min(damage, 10) * expMultiplier);
                }
            }
        }

        return Math.max(0, damage);
    }

    

    public Ability getAbility(String abilityName) {
        return abilities.get(abilityName.toLowerCase());
    }
}