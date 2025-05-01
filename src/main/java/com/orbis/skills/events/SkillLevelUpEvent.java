package com.orbis.skills.events;

import com.orbis.skills.skills.Skill;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;



public class SkillLevelUpEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Skill skill;
    private final int oldLevel;
    private final int newLevel;
    private boolean cancelled = false;

    

    public SkillLevelUpEvent(Player player, Skill skill, int oldLevel, int newLevel) {
        this.player = player;
        this.skill = skill;
        this.oldLevel = oldLevel;
        this.newLevel = newLevel;
    }

    

    public Player getPlayer() {
        return player;
    }

    

    public Skill getSkill() {
        return skill;
    }

    

    public int getOldLevel() {
        return oldLevel;
    }

    

    public int getNewLevel() {
        return newLevel;
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