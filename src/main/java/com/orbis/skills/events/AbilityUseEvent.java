package com.orbis.skills.events;

import com.orbis.skills.abilities.Ability;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;



public class AbilityUseEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Ability ability;
    private boolean cancelled = false;

    

    public AbilityUseEvent(Player player, Ability ability) {
        this.player = player;
        this.ability = ability;
    }

    

    public Player getPlayer() {
        return player;
    }

    

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