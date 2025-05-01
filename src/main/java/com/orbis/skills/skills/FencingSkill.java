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

    

    public FencingSkill(OrbisSkills plugin) {
        super(plugin, "fencing");

       

        initMobExpValues();
    }

    

    private void initMobExpValues() {
       

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
        mobExpValues.put(EntityType.PLAYER, 15.0);


       

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
       

        registerAbility(new BleedAbility(10));
        registerAbility(new ParryAbility(30));
        registerAbility(new CounterAttackAbility(50));

       

        loadConfiguredAbilities();
    }

    

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

    

    public double handleDamage(Player player, Entity target, double damage) {
        if (!(target instanceof LivingEntity)) {
            return damage;
        }

        LivingEntity livingTarget = (LivingEntity) target;

       

        ItemStack handItem = player.getInventory().getItemInMainHand();
        if (!isSword(handItem.getType())) {
            return damage;
        }

       

        EntityType entityType = target.getType();

       

        double baseExp = mobExpValues.getOrDefault(entityType, 5.0);
        double damageMultiplier = Math.min(damage / 5.0, 3.0);

       

        addExperience(player, baseExp * damageMultiplier);

       

        int level = plugin.getDataManager().getPlayerData(player.getUniqueId()).getSkillLevel(name);

       

        Ability bleedAbility = getAbility("bleed");
        if (bleedAbility != null && level >= bleedAbility.getUnlockLevel()) {
            double chance = bleedAbility.getEffectForLevel(level);

            if (random.nextDouble() < chance) {
               

                if (bleedAbility.trigger(player, plugin, 0)) {
                   

                    applyBleedEffect(livingTarget, level);
                }
            }
        }

        return damage;
    }

    

    private void applyBleedEffect(LivingEntity target, int level) {
       

        int duration = 3 + (level / 20);

        final double damagePerTick = 0.5 + (level / 100.0);


       

        new BukkitRunnable() {
            int ticks = 0;
            final int maxTicks = duration * 2;


            @Override
            public void run() {
                if (ticks >= maxTicks || target.isDead() || !target.isValid()) {
                    this.cancel();
                    return;
                }

               

                double health = target.getHealth();
                if (health > damagePerTick) {
                    target.setHealth(health - damagePerTick);
                } else {
                    target.setHealth(0.1);

                }

               

                target.getWorld().spawnParticle(Particle.DUST,
                        target.getLocation().add(0, 1, 0), 5, 0.3, 0.3, 0.3, 0,
                        new org.bukkit.Particle.DustOptions(org.bukkit.Color.RED, 1));

                ticks++;
            }
        }.runTaskTimer(plugin, 10L, 10L);

    }

    

    public double handlePlayerDamaged(Player player, Entity attacker, double damage) {
        if (!(attacker instanceof LivingEntity)) {
            return damage;
        }

        LivingEntity livingAttacker = (LivingEntity) attacker;

       

        int level = plugin.getDataManager().getPlayerData(player.getUniqueId()).getSkillLevel(name);
        UUID playerUuid = player.getUniqueId();

       

        lastAttacker.put(playerUuid, attacker.getUniqueId());
        lastDamagedTime.put(playerUuid, System.currentTimeMillis());

       

        ItemStack handItem = player.getInventory().getItemInMainHand();
        if (!isSword(handItem.getType())) {
            return damage;
        }

       

        Ability parryAbility = getAbility("parry");
        if (parryAbility != null && level >= parryAbility.getUnlockLevel()) {
            double chance = parryAbility.getEffectForLevel(level);

            if (random.nextDouble() < chance) {
               

                if (parryAbility.trigger(player, plugin, 0)) {
                   

                    double reduction = 0.3 + (level / 200.0);

                    reduction = Math.min(reduction, 0.8);

                   

                    player.sendMessage(plugin.getConfigManager().getColoredString("messages.ability-activate")
                            .replace("{ability}", "Parry")
                            .replace("{value}", String.format("%.0f%%", reduction * 100)));

                    return damage * (1 - reduction);
                }
            }
        }

        return damage;
    }

    

    public void handleCounterAttack(Player player, Entity target) {
        UUID playerUuid = player.getUniqueId();

       

        if (!lastAttacker.containsKey(playerUuid) || !lastDamagedTime.containsKey(playerUuid)) {
            return;
        }

       

        if (!target.getUniqueId().equals(lastAttacker.get(playerUuid))) {
            return;
        }

       

        long currentTime = System.currentTimeMillis();
        long lastDamaged = lastDamagedTime.get(playerUuid);
        if (currentTime - lastDamaged > 5000) {
            return;
        }

       

        int level = plugin.getDataManager().getPlayerData(playerUuid).getSkillLevel(name);

       

        Ability counterAttackAbility = getAbility("counterattack");
        if (counterAttackAbility != null && level >= counterAttackAbility.getUnlockLevel()) {
           

            if (counterAttackAbility.trigger(player, plugin, 30)) {
               

                double bonusDamage = 4.0 + (level / 10.0);


               

                if (target instanceof LivingEntity) {
                    LivingEntity livingTarget = (LivingEntity) target;
                    if (livingTarget.getHealth() > bonusDamage) {
                        livingTarget.setHealth(livingTarget.getHealth() - bonusDamage);
                    } else {
                        livingTarget.setHealth(0.1);
                    }

                   

                    livingTarget.addPotionEffect(new PotionEffect(
                            PotionEffectType.WEAKNESS,
                            5 * 20,

                            1));


                   

                    player.sendMessage(plugin.getConfigManager().getColoredString("messages.ability-activate")
                            .replace("{ability}", "Counter Attack")
                            .replace("{value}", String.format("%.1f", bonusDamage)));
                }
            }
        }
    }

    

    public void cleanupPlayer(UUID uuid) {
        lastAttacker.remove(uuid);
        lastDamagedTime.remove(uuid);
    }

    

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

    

    public Ability getAbility(String abilityName) {
        return abilities.get(abilityName.toLowerCase());
    }

    

    public double getComboMultiplier(Player player, int consecutiveHits) {
        int level = plugin.getDataManager().getPlayerData(player.getUniqueId()).getSkillLevel(name);

       

        double baseMultiplier = 1.0;

       

        double bonus = Math.min(consecutiveHits * 0.05, 0.3);


       

        double levelBonus = level / 500.0;


        return baseMultiplier + bonus + levelBonus;
    }

    

    public void applySkillEnchantmentEffects(Player player, ItemStack sword) {
        int level = plugin.getDataManager().getPlayerData(player.getUniqueId()).getSkillLevel(name);

       

       


       

       

    }
}