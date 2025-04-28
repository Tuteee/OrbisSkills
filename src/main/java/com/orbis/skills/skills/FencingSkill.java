package com.orbis.skills.skills;

import com.orbis.skills.OrbisSkills;
import com.orbis.skills.abilities.Ability;
import com.orbis.skills.abilities.fencing.BleedAbility;
import com.orbis.skills.abilities.fencing.CounterAttackAbility;
import com.orbis.skills.abilities.fencing.ParryAbility;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FencingSkill extends Skill {

    private final Random random = new Random();
    private final Map<EntityType, Double> mobExpValues = new HashMap<>();
    private final Map<UUID, Long> lastDamagedTime = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> lastAttacker = new ConcurrentHashMap<>();

    /**
     * Create a new fencing skill
     * @param plugin the plugin instance
     */
    public FencingSkill(OrbisSkills plugin) {
        super(plugin, "fencing");

        // Initialize mob experience values
        initMobExpValues();
    }

    /**
     * Initialize mob experience values
     */
    private void initMobExpValues() {
        // Default experience values
        mobExpValues.put(EntityType.ZOMBIE, 5.0);
        mobExpValues.put(EntityType.SKELETON, 6.0);
        mobExpValues.put(EntityType.CREEPER, 7.0);
        mobExpValues.put(EntityType.SPIDER, 5.0);
        mobExpValues.put(EntityType.ENDERMAN, 10.0);
        mobExpValues.put(EntityType.WITCH, 8.0);
        mobExpValues.put(EntityType.SLIME, 3.0);
        mobExpValues.put(EntityType.MAGMA_CUBE, 3.0);
        mobExpValues.put(EntityType.BLAZE, 7.0);
        mobExpValues.put(EntityType.PIGLIN, 6.0);
        mobExpValues.put(EntityType.PLAYER, 15.0); // PvP

        // Load custom values from config if available
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection expSection = config.getConfigurationSection("experience.fencing-values");

        if (expSection != null) {
            for (String key : expSection.getKeys(false)) {
                try {
                    EntityType entityType = EntityType.valueOf(key.toUpperCase());
                    double value = expSection.getDouble(key, 5.0);
                    mobExpValues.put(entityType, value);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid entity type in fencing-values: " + key);
                }
            }
        }
    }

    @Override
    protected void registerAbilities() {
        // Register default abilities
        registerAbility(new BleedAbility(10));
        registerAbility(new ParryAbility(30));
        registerAbility(new CounterAttackAbility(50));

        // Load additional abilities from config
        loadConfiguredAbilities();
    }

    /**
     * Load additional abilities from config
     */
    private void loadConfiguredAbilities() {
        FileConfiguration config = YamlConfiguration.loadConfiguration(
                new File(plugin.getDataFolder(), "config/abilities/fencing_abilities.yml"));

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
     * Handle entity damage for fencing
     * @param player the player
     * @param target the target entity
     * @param damage the damage dealt
     * @return the modified damage
     */
    public double handleDamage(Player player, Entity target, double damage) {
        if (!(target instanceof LivingEntity)) {
            return damage;
        }

        LivingEntity livingTarget = (LivingEntity) target;

        // Check if player is using a sword
        ItemStack handItem = player.getInventory().getItemInMainHand();
        if (!isSword(handItem.getType())) {
            return damage;
        }

        // Get entity type for experience
        EntityType entityType = target.getType();

        // Add base experience based on entity type and damage
        double baseExp = mobExpValues.getOrDefault(entityType, 5.0);
        double damageMultiplier = Math.min(damage / 5.0, 3.0);

        // Add experience
        addExperience(player, baseExp * damageMultiplier);

        // Get player level
        int level = plugin.getDataManager().getPlayerData(player.getUniqueId()).getSkillLevel(name);

        // Apply Bleed ability
        Ability bleedAbility = getAbility("bleed");
        if (bleedAbility != null && level >= bleedAbility.getUnlockLevel()) {
            double chance = bleedAbility.getEffectForLevel(level);

            if (random.nextDouble() < chance) {
                // Trigger ability
                if (bleedAbility.trigger(player, plugin, 0)) {
                    // Apply bleed effect
                    applyBleedEffect(livingTarget, level);
                }
            }
        }

        return damage;
    }

    /**
     * Apply bleed effect to target
     * @param target the target entity
     * @param level the player's skill level
     */
    private void applyBleedEffect(LivingEntity target, int level) {
        // Calculate duration and damage
        int duration = 3 + (level / 20); // 3-8 seconds based on level
        final double damagePerTick = 0.5 + (level / 100.0); // 0.5-1.5 damage per tick

        // Create bleed effect (damage over time)
        new BukkitRunnable() {
            int ticks = 0;
            final int maxTicks = duration * 2; // Every half-second

            @Override
            public void run() {
                if (ticks >= maxTicks || target.isDead() || !target.isValid()) {
                    this.cancel();
                    return;
                }

                // Apply damage
                double health = target.getHealth();
                if (health > damagePerTick) {
                    target.setHealth(health - damagePerTick);
                } else {
                    target.setHealth(0.1); // Don't kill directly, leave at minimal health
                }

                // Create blood particle effect
                target.getWorld().spawnParticle(Particle.DUST,
                        target.getLocation().add(0, 1, 0), 5, 0.3, 0.3, 0.3, 0,
                        new org.bukkit.Particle.DustOptions(org.bukkit.Color.RED, 1));

                ticks++;
            }
        }.runTaskTimer(plugin, 10L, 10L); // Every half-second
    }

    /**
     * Handle player being damaged (for parry and counter)
     * @param player the player
     * @param attacker the attacker entity
     * @param damage the damage received
     * @return the modified damage
     */
    public double handlePlayerDamaged(Player player, Entity attacker, double damage) {
        if (!(attacker instanceof LivingEntity)) {
            return damage;
        }

        LivingEntity livingAttacker = (LivingEntity) attacker;

        // Get player level
        int level = plugin.getDataManager().getPlayerData(player.getUniqueId()).getSkillLevel(name);
        UUID playerUuid = player.getUniqueId();

        // Store last attacker for counter ability
        lastAttacker.put(playerUuid, attacker.getUniqueId());
        lastDamagedTime.put(playerUuid, System.currentTimeMillis());

        // Check if player is using a sword
        ItemStack handItem = player.getInventory().getItemInMainHand();
        if (!isSword(handItem.getType())) {
            return damage;
        }

        // Apply Parry ability
        Ability parryAbility = getAbility("parry");
        if (parryAbility != null && level >= parryAbility.getUnlockLevel()) {
            double chance = parryAbility.getEffectForLevel(level);

            if (random.nextDouble() < chance) {
                // Trigger ability
                if (parryAbility.trigger(player, plugin, 0)) {
                    // Apply parry effect (reduce damage)
                    double reduction = 0.3 + (level / 200.0); // 30-80% reduction
                    reduction = Math.min(reduction, 0.8);

                    // Send message
                    player.sendMessage(plugin.getConfigManager().getColoredString("messages.ability-activate")
                            .replace("{ability}", "Parry")
                            .replace("{value}", String.format("%.0f%%", reduction * 100)));

                    return damage * (1 - reduction);
                }
            }
        }

        return damage;
    }

    /**
     * Handle counter attack
     * @param player the player
     * @param target the target entity
     */
    public void handleCounterAttack(Player player, Entity target) {
        UUID playerUuid = player.getUniqueId();

        // Check if this is a counter attack
        if (!lastAttacker.containsKey(playerUuid) || !lastDamagedTime.containsKey(playerUuid)) {
            return;
        }

        // Check if target is the last attacker
        if (!target.getUniqueId().equals(lastAttacker.get(playerUuid))) {
            return;
        }

        // Check time window (5 seconds)
        long currentTime = System.currentTimeMillis();
        long lastDamaged = lastDamagedTime.get(playerUuid);
        if (currentTime - lastDamaged > 5000) {
            return;
        }

        // Get player level
        int level = plugin.getDataManager().getPlayerData(playerUuid).getSkillLevel(name);

        // Apply CounterAttack ability
        Ability counterAttackAbility = getAbility("counterattack");
        if (counterAttackAbility != null && level >= counterAttackAbility.getUnlockLevel()) {
            // Trigger ability with a 30-second cooldown
            if (counterAttackAbility.trigger(player, plugin, 30)) {
                // Apply counter effect (bonus damage)
                double bonusDamage = 4.0 + (level / 10.0); // 4-14 bonus damage

                // We can't modify the damage directly, but we can apply it separately
                if (target instanceof LivingEntity) {
                    LivingEntity livingTarget = (LivingEntity) target;
                    if (livingTarget.getHealth() > bonusDamage) {
                        livingTarget.setHealth(livingTarget.getHealth() - bonusDamage);
                    } else {
                        livingTarget.setHealth(0.1);
                    }

                    // Apply weakness effect
                    livingTarget.addPotionEffect(new PotionEffect(
                            PotionEffectType.WEAKNESS,
                            5 * 20, // 5 seconds
                            1)); // level 2

                    // Send message
                    player.sendMessage(plugin.getConfigManager().getColoredString("messages.ability-activate")
                            .replace("{ability}", "Counter Attack")
                            .replace("{value}", String.format("%.1f", bonusDamage)));
                }
            }
        }
    }

    /**
     * Clean up data for player
     * @param uuid the player UUID
     */
    public void cleanupPlayer(UUID uuid) {
        lastAttacker.remove(uuid);
        lastDamagedTime.remove(uuid);
    }

    /**
     * Check if a material is a sword
     * @param type the material type
     * @return true if it's a sword
     */
    private boolean isSword(Material type) {
        switch (type) {
            case WOODEN_SWORD:
            case STONE_SWORD:
            case IRON_SWORD:
            case GOLDEN_SWORD:
            case DIAMOND_SWORD:
            case NETHERITE_SWORD:
                return true;
            default:
                return false;
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

    /**
     * Get combo multiplier for consecutive hits
     * @param player the player
     * @param consecutiveHits the number of consecutive hits
     * @return the damage multiplier
     */
    public double getComboMultiplier(Player player, int consecutiveHits) {
        int level = plugin.getDataManager().getPlayerData(player.getUniqueId()).getSkillLevel(name);

        // Base multiplier
        double baseMultiplier = 1.0;

        // Add bonus based on consecutive hits and level
        double bonus = Math.min(consecutiveHits * 0.05, 0.3); // Max 30% bonus

        // Level bonus (up to 20% at level 100)
        double levelBonus = level / 500.0; // 0-20%

        return baseMultiplier + bonus + levelBonus;
    }

    /**
     * Apply sword enchantment effects based on skill level
     * @param player the player
     * @param sword the sword item
     */
    public void applySkillEnchantmentEffects(Player player, ItemStack sword) {
        int level = plugin.getDataManager().getPlayerData(player.getUniqueId()).getSkillLevel(name);

        // This would be implemented to add temporary enchantment-like effects
        // based on the player's skill level

        // For real implementation, you might use attribute modifiers or
        // custom item meta to enhance the sword temporarily
    }
}