package com.orbis.skills.events;

import com.orbis.skills.abilities.Ability;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event fired when a player uses a skill ability
 */
public class AbilityUseEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Ability ability;
    private boolean cancelled = false;

    /**
     * Create a new ability use event
     * @param player the player
     * @param ability the ability
     */
    public AbilityUseEvent(Player player, Ability ability) {
        this.player = player;
        this.ability = ability;
    }

    /**
     * Get the player
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Get the ability
     * @return the ability
     */
    public Ability getAbility() {
        return ability;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}