package com.orbis.skills.listeners;

import com.orbis.skills.OrbisSkills;
import com.orbis.skills.skills.FishingSkill;
import com.orbis.skills.util.ExperienceUtil;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class SkillExpListeners implements Listener {

    private final OrbisSkills plugin;
    private final Random random = new Random();

    /**
     * Create new skill experience listeners
     * @param plugin the plugin instance
     */
    public SkillExpListeners(OrbisSkills plugin) {
        this.plugin = plugin;
    }

    /**
     * Handle fishing events
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerFish(PlayerFishEvent event) {
        Player player = event.getPlayer();

        // Check world restrictions
        if (isWorldDisabled(player)) {
            return;
        }

        // Check if user has permission
        if (!player.hasPermission("orbisskills.fishing")) {
            return;
        }

        // Only handle successful catches
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) {
            return;
        }

        // Get the caught entity (fish)
        if (!(event.getCaught() instanceof Item)) {
            return;
        }

        Item caughtItem = (Item) event.getCaught();
        ItemStack itemStack = caughtItem.getItemStack();

        // Get fishing skill
        FishingSkill fishingSkill = (FishingSkill) plugin.getSkillManager().getSkill("fishing");
        if (fishingSkill == null) {
            return;
        }

        // Calculate base experience based on what was caught
        double baseExp = getBaseFishingExp(itemStack.getType());

        // Apply formula from config
        String expFormula = plugin.getConfig().getString("experience.formula",
                "base * (1 + (level * 0.1)) * multiplier");
        int playerLevel = plugin.getDataManager().getPlayerData(player.getUniqueId()).getSkillLevel("fishing");
        double multiplier = plugin.getConfig().getDouble("settings.exp-multiplier", 1.0);

        double exp = ExperienceUtil.calculateExp(expFormula, baseExp, playerLevel, multiplier);

        // Add experience
        fishingSkill.handleFishCaught(player, exp);

        // Check for special drops
        ItemStack specialDrop = fishingSkill.handleSpecialDrop(player);
        if (specialDrop != null) {
            // Add the special drop to player's inventory or drop it
            if (player.getInventory().firstEmpty() != -1) {
                player.getInventory().addItem(specialDrop);
                player.sendMessage(plugin.getConfigManager().getColoredString("messages.special-drop")
                        .replace("{item}", specialDrop.getType().name()));
            } else {
                player.getWorld().dropItemNaturally(player.getLocation(), specialDrop);
                player.sendMessage(plugin.getConfigManager().getColoredString("messages.special-drop-full-inv")
                        .replace("{item}", specialDrop.getType().name()));
            }
        }
    }

    /**
     * Handle mining events
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        // Check world restrictions
        if (isWorldDisabled(player)) {
            return;
        }

        // Check block type and add experience for appropriate skills
        Material blockType = event.getBlock().getType();

        if (isOre(blockType) && player.hasPermission("orbisskills.mining")) {
            // Mining skill
            addMiningExp(player, blockType);
        } else if (isWood(blockType) && player.hasPermission("orbisskills.woodcutting")) {
            // Woodcutting skill
            addWoodcuttingExp(player, blockType);
        }
    }

    /**
     * Handle farming events
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHarvest(PlayerHarvestBlockEvent event) {
        Player player = event.getPlayer();

        // Check world restrictions
        if (isWorldDisabled(player)) {
            return;
        }

        // Check if user has permission
        if (!player.hasPermission("orbisskills.farming")) {
            return;
        }

        // Add farming experience
        addFarmingExp(player, event.getHarvestedBlock().getType());
    }

    /**
     * Handle combat events for fencing skill
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getDamager();

        // Check world restrictions
        if (isWorldDisabled(player)) {
            return;
        }

        ItemStack handItem = player.getInventory().getItemInMainHand();

        // Check weapon type and add experience for appropriate skills
        if (isSword(handItem.getType()) && player.hasPermission("orbisskills.fencing")) {
            // Fencing skill
            addFencingExp(player, event.getFinalDamage());
        } else if (isBow(handItem.getType()) && player.hasPermission("orbisskills.archery")) {
            // Archery skill (this would be better handled with ProjectileHitEvent)
            addArcheryExp(player, event.getFinalDamage());
        }
    }

    /**
     * Handle acrobatics events
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();

        // Check world restrictions
        if (isWorldDisabled(player)) {
            return;
        }

        // Check if user has permission
        if (!player.hasPermission("orbisskills.acrobatics")) {
            return;
        }

        // Only handle fall damage
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) {
            return;
        }

        // Add acrobatics experience
        addAcrobaticsExp(player, event.getDamage());
    }

    /**
     * Check if a world is disabled for skills
     * @param player the player
     * @return true if the world is disabled
     */
    private boolean isWorldDisabled(Player player) {
        return plugin.getConfig().getStringList("settings.disabled-worlds")
                .contains(player.getWorld().getName());
    }

    /**
     * Get base fishing experience based on item type
     * @param type the item type
     * @return the base experience
     */
    private double getBaseFishingExp(Material type) {
        switch (type) {
            case COD:
                return 25;
            case SALMON:
                return 35;
            case TROPICAL_FISH:
                return 45;
            case PUFFERFISH:
                return 55;
            default:
                return 15;
        }
    }

    /**
     * Add mining experience
     * @param player the player
     * @param blockType the block type
     */
    private void addMiningExp(Player player, Material blockType) {
        // Implementation would go here
    }

    /**
     * Add woodcutting experience
     * @param player the player
     * @param blockType the block type
     */
    private void addWoodcuttingExp(Player player, Material blockType) {
        // Implementation would go here
    }

    /**
     * Add farming experience
     * @param player the player
     * @param blockType the block type
     */
    private void addFarmingExp(Player player, Material blockType) {
        // Implementation would go here
    }

    /**
     * Add fencing experience
     * @param player the player
     * @param damage the damage dealt
     */
    private void addFencingExp(Player player, double damage) {
        // Implementation would go here
    }

    /**
     * Add archery experience
     * @param player the player
     * @param damage the damage dealt
     */
    private void addArcheryExp(Player player, double damage) {
        // Implementation would go here
    }

    /**
     * Add acrobatics experience
     * @param player the player
     * @param damage the damage taken
     */
    private void addAcrobaticsExp(Player player, double damage) {
        // Implementation would go here
    }

    /**
     * Check if a material is an ore
     * @param type the material type
     * @return true if it's an ore
     */
    private boolean isOre(Material type) {
        switch (type) {
            case COAL_ORE:
            case DEEPSLATE_COAL_ORE:
            case COPPER_ORE:
            case DEEPSLATE_COPPER_ORE:
            case IRON_ORE:
            case DEEPSLATE_IRON_ORE:
            case GOLD_ORE:
            case DEEPSLATE_GOLD_ORE:
            case REDSTONE_ORE:
            case DEEPSLATE_REDSTONE_ORE:
            case LAPIS_ORE:
            case DEEPSLATE_LAPIS_ORE:
            case DIAMOND_ORE:
            case DEEPSLATE_DIAMOND_ORE:
            case EMERALD_ORE:
            case DEEPSLATE_EMERALD_ORE:
            case NETHER_GOLD_ORE:
            case NETHER_QUARTZ_ORE:
            case ANCIENT_DEBRIS:
                return true;
            default:
                return false;
        }
    }

    /**
     * Check if a material is wood
     * @param type the material type
     * @return true if it's wood
     */
    private boolean isWood(Material type) {
        switch (type) {
            case OAK_LOG:
            case SPRUCE_LOG:
            case BIRCH_LOG:
            case JUNGLE_LOG:
            case ACACIA_LOG:
            case DARK_OAK_LOG:
            case CHERRY_LOG:
            case MANGROVE_LOG:
            case CRIMSON_STEM:
            case WARPED_STEM:
            case BAMBOO:
                return true;
            default:
                return false;
        }
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
     * Check if a material is a bow
     * @param type the material type
     * @return true if it's a bow
     */
    private boolean isBow(Material type) {
        return type == Material.BOW || type == Material.CROSSBOW;
    }
}