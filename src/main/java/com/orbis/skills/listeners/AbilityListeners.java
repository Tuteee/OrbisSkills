package com.orbis.skills.listeners;

import com.orbis.skills.OrbisSkills;
import com.orbis.skills.abilities.Ability;
import com.orbis.skills.abilities.fishing.DoubleDropAbility;
import com.orbis.skills.abilities.fishing.MasterAnglerAbility;
import com.orbis.skills.data.PlayerData;
import com.orbis.skills.events.AbilityUseEvent;
import com.orbis.skills.skills.Skill;
import org.bukkit.GameMode;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class AbilityListeners implements Listener {

    private final OrbisSkills plugin;
    private final Random random = new Random();
    private final Map<UUID, Long> lastFishingTime = new HashMap<>();

    /**
     * Create new ability listeners
     * @param plugin the plugin instance
     */
    public AbilityListeners(OrbisSkills plugin) {
        this.plugin = plugin;
    }

    /**
     * Handle player join event
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Load player data
        plugin.getDataManager().loadPlayerData(player.getUniqueId());
    }

    /**
     * Handle player quit event
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Schedule data save with a delay
        int delay = plugin.getConfig().getInt("settings.save-on-quit-delay", 20);
        plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            plugin.getDataManager().savePlayerData(uuid);
            plugin.getDataManager().unloadPlayerData(uuid);
        }, delay);

        // Clean up cooldowns
        lastFishingTime.remove(uuid);
    }

    /**
     * Handle ability use event
     */
    @EventHandler
    public void onAbilityUse(AbilityUseEvent event) {
        Player player = event.getPlayer();
        Ability ability = event.getAbility();

        // Check for ability message
        if (plugin.getConfig().getBoolean("settings.ability-messages", true)) {
            // Get the message from config
            String messagePath = ability.getName().toLowerCase() + ".messages.activate";
            String message = plugin.getConfigManager().getAbilitiesConfig(
                            getSkillNameForAbility(ability) + "_abilities.yml")
                    .getString("abilities." + messagePath);

            if (message != null && !message.isEmpty()) {
                player.sendMessage(plugin.getConfigManager().getColoredString("prefix") +
                        plugin.getConfigManager().getColoredString(messagePath));
            }
        }
    }

    /**
     * Handle fishing abilities
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerFish(PlayerFishEvent event) {
        Player player = event.getPlayer();

        // Check if this is a successful catch
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH ||
                !(event.getCaught() instanceof Item)) {
            return;
        }

        // Check permission
        if (!player.hasPermission("orbisskills.fishing")) {
            return;
        }

        // Check world restrictions
        if (isWorldDisabled(player)) {
            return;
        }

        // Check gamemode
        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        UUID uuid = player.getUniqueId();
        PlayerData playerData = plugin.getDataManager().getPlayerData(uuid);
        if (playerData == null) {
            return;
        }

        Item caughtItem = (Item) event.getCaught();
        ItemStack itemStack = caughtItem.getItemStack();

        // Handle fishing abilities
        Skill fishingSkill = plugin.getSkillManager().getSkill("fishing");
        if (fishingSkill == null) {
            return;
        }

        int fishingLevel = playerData.getSkillLevel("fishing");

        // Check for Master Angler ability (instant catches)
        if (handleMasterAnglerAbility(player, fishingLevel)) {
            // Schedule a new bite immediately
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                player.launchProjectile(org.bukkit.entity.FishHook.class);
            }, 5L);
        }

        // Check for Double Drop ability
        handleDoubleDropAbility(player, fishingLevel, itemStack);
    }

    /**
     * Handle Master Angler ability
     * @param player the player
     * @param fishingLevel the fishing level
     * @return true if ability triggered
     */
    private boolean handleMasterAnglerAbility(Player player, int fishingLevel) {
        Ability masterAnglerAbility = getAbility("fishing", "masterangler");
        if (masterAnglerAbility == null || !(masterAnglerAbility instanceof MasterAnglerAbility)) {
            return false;
        }

        // Check if player has the ability unlocked
        if (fishingLevel < masterAnglerAbility.getUnlockLevel()) {
            return false;
        }

        // Check cooldown
        UUID uuid = player.getUniqueId();
        if (lastFishingTime.containsKey(uuid)) {
            long lastTime = lastFishingTime.get(uuid);
            if (System.currentTimeMillis() - lastTime < 5000) {
                return false;
            }
        }

        // Record fishing time
        lastFishingTime.put(uuid, System.currentTimeMillis());

        // Check chance
        double chance = masterAnglerAbility.getEffectForLevel(fishingLevel);
        if (random.nextDouble() < chance) {
            // Trigger ability
            if (masterAnglerAbility.trigger(player, plugin, 0)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Handle Double Drop ability
     * @param player the player
     * @param fishingLevel the fishing level
     * @param itemStack the caught item
     */
    private void handleDoubleDropAbility(Player player, int fishingLevel, ItemStack itemStack) {
        Ability doubleDropAbility = getAbility("fishing", "doubledrop");
        if (doubleDropAbility == null || !(doubleDropAbility instanceof DoubleDropAbility)) {
            return;
        }

        // Check if player has the ability unlocked
        if (fishingLevel < doubleDropAbility.getUnlockLevel()) {
            return;
        }

        // Check chance
        double chance = doubleDropAbility.getEffectForLevel(fishingLevel);
        if (random.nextDouble() < chance) {
            // Trigger ability
            if (doubleDropAbility.trigger(player, plugin, 0)) {
                // Give player a copy of the item
                ItemStack extraItem = itemStack.clone();

                if (player.getInventory().firstEmpty() != -1) {
                    player.getInventory().addItem(extraItem);
                } else {
                    player.getWorld().dropItemNaturally(player.getLocation(), extraItem);
                }
            }
        }
    }

    /**
     * Get an ability
     * @param skillName the skill name
     * @param abilityName the ability name
     * @return the ability, or null if not found
     */
    private Ability getAbility(String skillName, String abilityName) {
        Skill skill = plugin.getSkillManager().getSkill(skillName);
        if (skill == null) {
            return null;
        }

        return skill.getAbility(abilityName);
    }

    /**
     * Get the skill name for an ability
     * @param ability the ability
     * @return the skill name
     */
    private String getSkillNameForAbility(Ability ability) {
        // This would be better implemented with a reference in the Ability class
        // For now, we'll use some hard-coded logic
        String abilityName = ability.getName().toLowerCase();

        if (abilityName.equals("doubledrop") || abilityName.equals("treasurehunter") ||
                abilityName.equals("experiencedfisher") || abilityName.equals("masterangler")) {
            return "fishing";
        }

        // Default to the ability name
        return abilityName;
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