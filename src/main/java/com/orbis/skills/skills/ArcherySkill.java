package com.orbis.skills.skills;

import com.orbis.skills.OrbisSkills;
import com.orbis.skills.abilities.Ability;
import com.orbis.skills.abilities.archery.BullseyeAbility;
import com.orbis.skills.abilities.archery.DazingArrowAbility;
import com.orbis.skills.abilities.archery.MultiShotAbility;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ArcherySkill extends Skill {

    private final Random random = new Random();
    private final Map<EntityType, Double> mobExpValues = new HashMap<>();

    

    public ArcherySkill(OrbisSkills plugin) {
        super(plugin, "archery");

       

        initMobExpValues();
    }

    

    private void initMobExpValues() {
       

        mobExpValues.put(EntityType.ZOMBIE, 8.0);
        mobExpValues.put(EntityType.SKELETON, 8.0);
        mobExpValues.put(EntityType.CREEPER, 10.0);
        mobExpValues.put(EntityType.SPIDER, 7.0);
        mobExpValues.put(EntityType.ENDERMAN, 15.0);
        mobExpValues.put(EntityType.WITCH, 12.0);
        mobExpValues.put(EntityType.SLIME, 5.0);
        mobExpValues.put(EntityType.MAGMA_CUBE, 5.0);
        mobExpValues.put(EntityType.BLAZE, 12.0);
        mobExpValues.put(EntityType.GHAST, 15.0);
        mobExpValues.put(EntityType.PHANTOM, 10.0);
        mobExpValues.put(EntityType.PLAYER, 20.0);


       

        FileConfiguration config = plugin.getConfig();
        ConfigurationSection expSection = config.getConfigurationSection("experience.archery-values");

        if (expSection != null) {
            for (String key : expSection.getKeys(false)) {
                try {
                    EntityType entityType = EntityType.valueOf(key.toUpperCase());
                    double value = expSection.getDouble(key, 5.0);
                    mobExpValues.put(entityType, value);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid entity type in archery-values: " + key);
                }
            }
        }
    }

    @Override
    protected void registerAbilities() {
       

        registerAbility(new BullseyeAbility(10));
        registerAbility(new DazingArrowAbility(30));
        registerAbility(new MultiShotAbility(50));

       

        loadConfiguredAbilities();
    }

    

    private void loadConfiguredAbilities() {
        FileConfiguration config = YamlConfiguration.loadConfiguration(
                new File(plugin.getDataFolder(), "config/abilities/archery_abilities.yml"));

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

    

    public void handleArrowHit(Player player, Arrow arrow, Entity target, double damage) {
        if (!(target instanceof LivingEntity)) {
            return;
        }

        LivingEntity livingTarget = (LivingEntity) target;

       

        EntityType entityType = target.getType();

       

        double baseExp = mobExpValues.getOrDefault(entityType, 5.0);
        double distance = arrow.getLocation().distance(player.getLocation());
        double distanceMultiplier = calculateDistanceMultiplier(distance);

       

        addExperience(player, baseExp * distanceMultiplier);

       

        int level = plugin.getDataManager().getPlayerData(player.getUniqueId()).getSkillLevel(name);

       

        Ability bullseye = getAbility("bullseye");
        if (bullseye != null && level >= bullseye.getUnlockLevel()) {
            double chance = bullseye.getEffectForLevel(level);

            if (random.nextDouble() < chance) {
               

                if (bullseye.trigger(player, plugin, 0)) {
                   

                    double extraDamage = damage * 0.5;


                   

                    if (livingTarget.getHealth() > extraDamage) {
                        livingTarget.setHealth(livingTarget.getHealth() - extraDamage);
                    } else {
                        livingTarget.setHealth(0);
                    }
                }
            }
        }

       

        Ability dazingArrow = getAbility("dazingarrow");
        if (dazingArrow != null && level >= dazingArrow.getUnlockLevel()) {
            double chance = dazingArrow.getEffectForLevel(level);

            if (random.nextDouble() < chance) {
               

                if (dazingArrow.trigger(player, plugin, 0)) {
                   

                    int duration = 5 + (level / 10);

                    livingTarget.addPotionEffect(new PotionEffect(
                            PotionEffectType.NAUSEA,
                            duration * 20,

                            0));

                }
            }
        }
    }

    

    public void handleBowDraw(Player player) {
       

        int level = plugin.getDataManager().getPlayerData(player.getUniqueId()).getSkillLevel(name);

       

        Ability multiShot = getAbility("multishot");
        if (multiShot != null && level >= multiShot.getUnlockLevel()) {
           

           

        }
    }

    

    public void handleArrowLaunch(Player player, Arrow arrow) {
       

        int level = plugin.getDataManager().getPlayerData(player.getUniqueId()).getSkillLevel(name);

       

        Ability multiShot = getAbility("multishot");
        if (multiShot != null && level >= multiShot.getUnlockLevel()) {
           

            if (player.isSneaking()) {
               

                if (multiShot.canUse(player, plugin)) {
                   

                    if (multiShot.trigger(player, plugin, 30)) {
                       

                        int arrowCount = 2 + (level / 25);

                        arrowCount = Math.min(arrowCount, 6);

                        for (int i = 1; i < arrowCount; i++) {
                           

                            double spread = 0.1 + (i * 0.05);

                           

                            Vector velocity = arrow.getVelocity().clone();
                            velocity.add(new Vector(
                                    (random.nextDouble() - 0.5) * spread,
                                    (random.nextDouble() - 0.5) * spread,
                                    (random.nextDouble() - 0.5) * spread
                            ));

                           

                            Arrow newArrow = (Arrow) player.getWorld().spawnEntity(
                                    arrow.getLocation(), EntityType.ARROW);
                            newArrow.setShooter(player);
                            newArrow.setVelocity(velocity);
                            newArrow.setCritical(arrow.isCritical());
                            newArrow.setPickupStatus(arrow.getPickupStatus());
                        }
                    }
                }
            }
        }
    }

    

    private double calculateDistanceMultiplier(double distance) {
        if (distance < 10) {
            return 1.0;
        } else if (distance < 25) {
            return 1.5;
        } else if (distance < 50) {
            return 2.0;
        } else if (distance < 75) {
            return 3.0;
        } else {
            return 4.0;
        }
    }

    

    public Ability getAbility(String abilityName) {
        return abilities.get(abilityName.toLowerCase());
    }
}