package com.orbis.skills.events;

import com.orbis.skills.skills.Skill;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event fired when a player's skill level increases
 */
public class SkillLevelUpEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Skill skill;
    private final int oldLevel;
    private final int newLevel;
    private boolean cancelled = false;

    /**
     * Create a new skill level-up event
     * @param player the player
     * @param skill the skill
     * @param oldLevel the old level
     * @param newLevel the new level
     */
    public SkillLevelUpEvent(Player player, Skill skill, int oldLevel, int newLevel) {
        this.player = player;
        this.skill = skill;
        this.oldLevel = oldLevel;
        this.newLevel = newLevel;
    }

    /**
     * Get the player
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Get the skill
     * @return the skill
     */
    public Skill getSkill() {
        return skill;
    }

    /**
     * Get the old level
     * @return the old level
     */
    public int getOldLevel() {
        return oldLevel;
    }

    /**
     * Get the new level
     * @return the new level
     */
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