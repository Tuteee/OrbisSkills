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



public class CustomSkillListeners implements Listener {

    private final OrbisSkills plugin;

    

    public CustomSkillListeners(OrbisSkills plugin) {
        this.plugin = plugin;
    }

    

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCraftItem(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();

       

        if (isWorldDisabled(player)) {
            return;
        }

        ItemStack result = event.getRecipe().getResult();
        plugin.getCustomSkillManager().handleTrigger(player, "CRAFT", result.getType().name());
    }

    

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFurnaceExtract(FurnaceExtractEvent event) {
        Player player = event.getPlayer();

       

        if (isWorldDisabled(player)) {
            return;
        }

        plugin.getCustomSkillManager().handleTrigger(player, "SMELT", event.getItemType().name());
    }

    

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBrew(BrewEvent event) {
       

       


       

        plugin.getCustomSkillManager().handleTrigger(null, "BREW", null);
    }

    

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

       

        if (isWorldDisabled(player)) {
            return;
        }

        Material blockType = event.getBlock().getType();
        plugin.getCustomSkillManager().handleTrigger(player, "BREAK", blockType.name());
    }

    

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

       

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

    

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHarvest(PlayerHarvestBlockEvent event) {
        Player player = event.getPlayer();

       

        if (isWorldDisabled(player)) {
            return;
        }

        plugin.getCustomSkillManager().handleTrigger(
                player, "HARVEST", event.getHarvestedBlock().getType().name());
    }

    

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFish(PlayerFishEvent event) {
        Player player = event.getPlayer();

       

        if (isWorldDisabled(player)) {
            return;
        }

        plugin.getCustomSkillManager().handleTrigger(player, "FISH", event.getState().name());
    }

    

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreed(EntityBreedEvent event) {
        if (!(event.getBreeder() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getBreeder();

       

        if (isWorldDisabled(player)) {
            return;
        }

        plugin.getCustomSkillManager().handleTrigger(
                player, "BREED", event.getEntityType().name());
    }

    

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();

       

        if (isWorldDisabled(player)) {
            return;
        }

        plugin.getCustomSkillManager().handleTrigger(
                player, "CONSUME", event.getItem().getType().name());
    }

    

    private boolean isWorldDisabled(Player player) {
        return plugin.getConfig().getStringList("settings.disabled-worlds")
                .contains(player.getWorld().getName());
    }
}