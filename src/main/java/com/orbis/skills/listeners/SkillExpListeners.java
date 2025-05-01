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

    

    public SkillExpListeners(OrbisSkills plugin) {
        this.plugin = plugin;
    }

    

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerFish(PlayerFishEvent event) {
        Player player = event.getPlayer();

       

        if (isWorldDisabled(player)) {
            return;
        }

       

        if (!player.hasPermission("orbisskills.fishing")) {
            return;
        }

       

        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) {
            return;
        }

       

        if (!(event.getCaught() instanceof Item)) {
            return;
        }

        Item caughtItem = (Item) event.getCaught();
        ItemStack itemStack = caughtItem.getItemStack();

       

        FishingSkill fishingSkill = (FishingSkill) plugin.getSkillManager().getSkill("fishing");
        if (fishingSkill == null) {
            return;
        }

       

        double baseExp = getBaseFishingExp(itemStack.getType());

       

        String expFormula = plugin.getConfig().getString("experience.formula",
                "base * (1 + (level * 0.1)) * multiplier");
        int playerLevel = plugin.getDataManager().getPlayerData(player.getUniqueId()).getSkillLevel("fishing");
        double multiplier = plugin.getConfig().getDouble("settings.exp-multiplier", 1.0);

        double exp = ExperienceUtil.calculateExp(expFormula, baseExp, playerLevel, multiplier);

       

        fishingSkill.handleFishCaught(player, exp);

       

        ItemStack specialDrop = fishingSkill.handleSpecialDrop(player);
        if (specialDrop != null) {
           

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

    

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

       

        if (isWorldDisabled(player)) {
            return;
        }

       

        Material blockType = event.getBlock().getType();

        if (isOre(blockType) && player.hasPermission("orbisskills.mining")) {
           

            addMiningExp(player, blockType);
        } else if (isWood(blockType) && player.hasPermission("orbisskills.woodcutting")) {
           

            addWoodcuttingExp(player, blockType);
        }
    }

    

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHarvest(PlayerHarvestBlockEvent event) {
        Player player = event.getPlayer();

       

        if (isWorldDisabled(player)) {
            return;
        }

       

        if (!player.hasPermission("orbisskills.farming")) {
            return;
        }

       

        addFarmingExp(player, event.getHarvestedBlock().getType());
    }

    

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getDamager();

       

        if (isWorldDisabled(player)) {
            return;
        }

        ItemStack handItem = player.getInventory().getItemInMainHand();

       

        if (isSword(handItem.getType()) && player.hasPermission("orbisskills.fencing")) {
           

            addFencingExp(player, event.getFinalDamage());
        } else if (isBow(handItem.getType()) && player.hasPermission("orbisskills.archery")) {
           

            addArcheryExp(player, event.getFinalDamage());
        }
    }

    

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();

       

        if (isWorldDisabled(player)) {
            return;
        }

       

        if (!player.hasPermission("orbisskills.acrobatics")) {
            return;
        }

       

        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) {
            return;
        }

       

        addAcrobaticsExp(player, event.getDamage());
    }

    

    private boolean isWorldDisabled(Player player) {
        return plugin.getConfig().getStringList("settings.disabled-worlds")
                .contains(player.getWorld().getName());
    }

    

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

    

    private void addMiningExp(Player player, Material blockType) {
       

    }

    

    private void addWoodcuttingExp(Player player, Material blockType) {
       

    }

    

    private void addFarmingExp(Player player, Material blockType) {
       

    }

    

    private void addFencingExp(Player player, double damage) {
       

    }

    

    private void addArcheryExp(Player player, double damage) {
       

    }

    

    private void addAcrobaticsExp(Player player, double damage) {
       

    }

    

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

    

    private boolean isBow(Material type) {
        return type == Material.BOW || type == Material.CROSSBOW;
    }
}