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

    /**
     * Create a new archery skill
     * @param plugin the plugin instance
     */
    public ArcherySkill(OrbisSkills plugin) {
        super(plugin, "archery");

        // Initialize mob experience values
        initMobExpValues();
    }

    /**
     * Initialize mob experience values
     */
    private void initMobExpValues() {
        // Default experience values
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
        mobExpValues.put(EntityType.PLAYER, 20.0); // PvP

        // Load custom values from config if available
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
        // Register default abilities
        registerAbility(new BullseyeAbility(10));
        registerAbility(new DazingArrowAbility(30));
        registerAbility(new MultiShotAbility(50));

        // Load additional abilities from config
        loadConfiguredAbilities();
    }

    /**
     * Load additional abilities from config
     */
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

    /**
     * Handle arrow hit event
     * @param player the player
     * @param arrow the arrow
     * @param target the target entity
     * @param damage the damage dealt
     */
    public void handleArrowHit(Player player, Arrow arrow, Entity target, double damage) {
        if (!(target instanceof LivingEntity)) {
            return;
        }

        LivingEntity livingTarget = (LivingEntity) target;

        // Get entity type for experience
        EntityType entityType = target.getType();

        // Add base experience based on entity type and distance
        double baseExp = mobExpValues.getOrDefault(entityType, 5.0);
        double distance = arrow.getLocation().distance(player.getLocation());
        double distanceMultiplier = calculateDistanceMultiplier(distance);

        // Add experience
        addExperience(player, baseExp * distanceMultiplier);

        // Get player level
        int level = plugin.getDataManager().getPlayerData(player.getUniqueId()).getSkillLevel(name);

        // Apply Bullseye ability (critical hit damage)
        Ability bullseye = getAbility("bullseye");
        if (bullseye != null && level >= bullseye.getUnlockLevel()) {
            double chance = bullseye.getEffectForLevel(level);

            if (random.nextDouble() < chance) {
                // Trigger ability
                if (bullseye.trigger(player, plugin, 0)) {
                    // Apply extra damage
                    double extraDamage = damage * 0.5; // 50% extra damage

                    // We can't modify the damage directly, but we can apply it separately
                    if (livingTarget.getHealth() > extraDamage) {
                        livingTarget.setHealth(livingTarget.getHealth() - extraDamage);
                    } else {
                        livingTarget.setHealth(0);
                    }
                }
            }
        }

        // Apply DazingArrow ability (confusion effect)
        Ability dazingArrow = getAbility("dazingarrow");
        if (dazingArrow != null && level >= dazingArrow.getUnlockLevel()) {
            double chance = dazingArrow.getEffectForLevel(level);

            if (random.nextDouble() < chance) {
                // Trigger ability
                if (dazingArrow.trigger(player, plugin, 0)) {
                    // Apply confusion effect
                    int duration = 5 + (level / 10); // 5-15 seconds based on level
                    livingTarget.addPotionEffect(new PotionEffect(
                            PotionEffectType.NAUSEA,
                            duration * 20, // ticks
                            0)); // level 1
                }
            }
        }
    }

    /**
     * Handle bow draw start
     * @param player the player
     */
    public void handleBowDraw(Player player) {
        // Get player level
        int level = plugin.getDataManager().getPlayerData(player.getUniqueId()).getSkillLevel(name);

        // Apply MultiShot ability (fire multiple arrows)
        Ability multiShot = getAbility("multishot");
        if (multiShot != null && level >= multiShot.getUnlockLevel()) {
            // Only track draw, the actual firing happens in ArrowLaunchEvent
            // We would store the player in a set to track who has MultiShot active
        }
    }

    /**
     * Handle arrow launch for MultiShot
     * @param player the player
     * @param arrow the original arrow
     */
    public void handleArrowLaunch(Player player, Arrow arrow) {
        // Get player level
        int level = plugin.getDataManager().getPlayerData(player.getUniqueId()).getSkillLevel(name);

        // Apply MultiShot ability (fire multiple arrows)
        Ability multiShot = getAbility("multishot");
        if (multiShot != null && level >= multiShot.getUnlockLevel()) {
            // Check if player is sneaking (to activate)
            if (player.isSneaking()) {
                // Check cooldown
                if (multiShot.canUse(player, plugin)) {
                    // Trigger ability with a 30-second cooldown
                    if (multiShot.trigger(player, plugin, 30)) {
                        // Fire additional arrows
                        int arrowCount = 2 + (level / 25); // 2-6 arrows based on level
                        arrowCount = Math.min(arrowCount, 6);

                        for (int i = 1; i < arrowCount; i++) {
                            // Calculate spread
                            double spread = 0.1 + (i * 0.05);

                            // Clone arrow velocity and add spread
                            Vector velocity = arrow.getVelocity().clone();
                            velocity.add(new Vector(
                                    (random.nextDouble() - 0.5) * spread,
                                    (random.nextDouble() - 0.5) * spread,
                                    (random.nextDouble() - 0.5) * spread
                            ));

                            // Spawn new arrow
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

    /**
     * Calculate a multiplier based on shot distance
     * @param distance the distance in blocks
     * @return the experience multiplier
     */
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

    /**
     * Get an ability
     * @param abilityName the ability name
     * @return the ability, or null if not found
     */
    public Ability getAbility(String abilityName) {
        return abilities.get(abilityName.toLowerCase());
    }
}