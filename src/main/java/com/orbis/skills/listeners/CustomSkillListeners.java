package com.orbis.skills.listeners;

import com.orbis.skills.OrbisSkills;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Listeners for custom skill events
 */
public class CustomSkillListeners implements Listener {

    private final OrbisSkills plugin;

    /**
     * Create new custom skill listeners
     * @param plugin the plugin instance
     */
    public CustomSkillListeners(OrbisSkills plugin) {
        this.plugin = plugin;
    }

    /**
     * Handle crafting events
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCraftItem(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();

        // Check world restrictions
        if (isWorldDisabled(player)) {
            return;
        }

        ItemStack result = event.getRecipe().getResult();
        plugin.getCustomSkillManager().handleTrigger(player, "CRAFT", result.getType().name());
    }

    /**
     * Handle furnace extraction
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFurnaceExtract(FurnaceExtractEvent event) {
        Player player = event.getPlayer();

        // Check world restrictions
        if (isWorldDisabled(player)) {
            return;
        }

        plugin.getCustomSkillManager().handleTrigger(player, "SMELT", event.getItemType().name());
    }

    /**
     * Handle brewing
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBrew(BrewEvent event) {
        // Note: BrewEvent doesn't provide the player, in a real implementation
        // you would track who placed the ingredients in the brewing stand

        // This is a simplified version for demonstration
        plugin.getCustomSkillManager().handleTrigger(null, "BREW", null);
    }

    /**
     * Handle block breaking
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        // Check world restrictions
        if (isWorldDisabled(player)) {
            return;
        }

        Material blockType = event.getBlock().getType();
        plugin.getCustomSkillManager().handleTrigger(player, "BREAK", blockType.name());
    }

    /**
     * Handle player interaction
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        // Check world restrictions
        if (isWorldDisabled(player)) {
            return;
        }

        if (event.hasItem()) {
            plugin.getCustomSkillManager().handleTrigger(
                    player, "INTERACT", event.getItem().getType().name());
        }

        if (event.hasBlock()) {
            plugin.getCustomSkillManager().handleTrigger(
                    player, "INTERACT_BLOCK", event.getClickedBlock().getType().name());
        }
    }

    /**
     * Handle harvesting
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHarvest(PlayerHarvestBlockEvent event) {
        Player player = event.getPlayer();

        // Check world restrictions
        if (isWorldDisabled(player)) {
            return;
        }

        plugin.getCustomSkillManager().handleTrigger(
                player, "HARVEST", event.getHarvestedBlock().getType().name());
    }

    /**
     * Handle fishing
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFish(PlayerFishEvent event) {
        Player player = event.getPlayer();

        // Check world restrictions
        if (isWorldDisabled(player)) {
            return;
        }

        plugin.getCustomSkillManager().handleTrigger(player, "FISH", event.getState().name());
    }

    /**
     * Handle breeding
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreed(EntityBreedEvent event) {
        if (!(event.getBreeder() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getBreeder();

        // Check world restrictions
        if (isWorldDisabled(player)) {
            return;
        }

        plugin.getCustomSkillManager().handleTrigger(
                player, "BREED", event.getEntityType().name());
    }

    /**
     * Handle item consumption
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();

        // Check world restrictions
        if (isWorldDisabled(player)) {
            return;
        }

        plugin.getCustomSkillManager().handleTrigger(
                player, "CONSUME", event.getItem().getType().name());
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
}