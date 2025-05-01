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

    

    public AbilityListeners(OrbisSkills plugin) {
        this.plugin = plugin;
    }

    

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

       

        plugin.getDataManager().loadPlayerData(player.getUniqueId());
    }

    

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

       

        int delay = plugin.getConfig().getInt("settings.save-on-quit-delay", 20);
        plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            plugin.getDataManager().savePlayerData(uuid);
            plugin.getDataManager().unloadPlayerData(uuid);
        }, delay);

       

        lastFishingTime.remove(uuid);
    }

    

    @EventHandler
    public void onAbilityUse(AbilityUseEvent event) {
        Player player = event.getPlayer();
        Ability ability = event.getAbility();

       

        if (plugin.getConfig().getBoolean("settings.ability-messages", true)) {
           

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

    

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerFish(PlayerFishEvent event) {
        Player player = event.getPlayer();

       

        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH ||
                !(event.getCaught() instanceof Item)) {
            return;
        }

       

        if (!player.hasPermission("orbisskills.fishing")) {
            return;
        }

       

        if (isWorldDisabled(player)) {
            return;
        }

       

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

       

        Skill fishingSkill = plugin.getSkillManager().getSkill("fishing");
        if (fishingSkill == null) {
            return;
        }

        int fishingLevel = playerData.getSkillLevel("fishing");

       

        if (handleMasterAnglerAbility(player, fishingLevel)) {
           

            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                player.launchProjectile(org.bukkit.entity.FishHook.class);
            }, 5L);
        }

       

        handleDoubleDropAbility(player, fishingLevel, itemStack);
    }

    

    private boolean handleMasterAnglerAbility(Player player, int fishingLevel) {
        Ability masterAnglerAbility = getAbility("fishing", "masterangler");
        if (masterAnglerAbility == null || !(masterAnglerAbility instanceof MasterAnglerAbility)) {
            return false;
        }

       

        if (fishingLevel < masterAnglerAbility.getUnlockLevel()) {
            return false;
        }

       

        UUID uuid = player.getUniqueId();
        if (lastFishingTime.containsKey(uuid)) {
            long lastTime = lastFishingTime.get(uuid);
            if (System.currentTimeMillis() - lastTime < 5000) {
                return false;
            }
        }

       

        lastFishingTime.put(uuid, System.currentTimeMillis());

       

        double chance = masterAnglerAbility.getEffectForLevel(fishingLevel);
        if (random.nextDouble() < chance) {
           

            if (masterAnglerAbility.trigger(player, plugin, 0)) {
                return true;
            }
        }

        return false;
    }

    

    private void handleDoubleDropAbility(Player player, int fishingLevel, ItemStack itemStack) {
        Ability doubleDropAbility = getAbility("fishing", "doubledrop");
        if (doubleDropAbility == null || !(doubleDropAbility instanceof DoubleDropAbility)) {
            return;
        }

       

        if (fishingLevel < doubleDropAbility.getUnlockLevel()) {
            return;
        }

       

        double chance = doubleDropAbility.getEffectForLevel(fishingLevel);
        if (random.nextDouble() < chance) {
           

            if (doubleDropAbility.trigger(player, plugin, 0)) {
               

                ItemStack extraItem = itemStack.clone();

                if (player.getInventory().firstEmpty() != -1) {
                    player.getInventory().addItem(extraItem);
                } else {
                    player.getWorld().dropItemNaturally(player.getLocation(), extraItem);
                }
            }
        }
    }

    

    private Ability getAbility(String skillName, String abilityName) {
        Skill skill = plugin.getSkillManager().getSkill(skillName);
        if (skill == null) {
            return null;
        }

        return skill.getAbility(abilityName);
    }

    

    private String getSkillNameForAbility(Ability ability) {
       

       

        String abilityName = ability.getName().toLowerCase();

        if (abilityName.equals("doubledrop") || abilityName.equals("treasurehunter") ||
                abilityName.equals("experiencedfisher") || abilityName.equals("masterangler")) {
            return "fishing";
        }

       

        return abilityName;
    }

    

    private boolean isWorldDisabled(Player player) {
        return plugin.getConfig().getStringList("settings.disabled-worlds")
                .contains(player.getWorld().getName());
    }
}