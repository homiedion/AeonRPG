package com.gmail.alexdion93.aeonrpg.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.gmail.alexdion93.aeonrpg.data.type.RPGSkillType;

/**
 * A custom event designed to be thrown whenever a player
 * gains experience in a particular rpg skill.
 * @author Alex Dion
 *
 */
public class RPGSkillExperienceGainEvent extends Event implements Cancellable {

  private static HandlerList handlers = new HandlerList();
  private boolean cancelled;
  private final RPGSkillType skill;
  private final Player player;
  private int experience;

  /**
   * Returns the handlers tied to this event
   * @return The handlers tied to this event
   */
  public static HandlerList getHandlerList() {
    return handlers;
  }
  /**
   * Constructor
   * @param player The player who gained the experience
   * @param skill The skill that gained experience
   * @param experience The experience gained.
   */
  public RPGSkillExperienceGainEvent(Player player, RPGSkillType skill, int experience) {
    
    if (player == null) { throw new IllegalArgumentException("Player cannot be null"); }
    if (skill == null) { throw new IllegalArgumentException("Skill cannot be null"); }
    
    this.player = player;
    this.skill = skill;
    this.experience = experience;
  }

  @Override
  public HandlerList getHandlers() {
    return handlers;
  }
  
  /**
   * Returns the player who gained experience
   * @return The player who gained experience
   */
  public Player getPlayer() { return player; }
  
  /**
   * Returns the skill that experience was gained towards
   * @return The skill that experience was gained towards
   */
  public RPGSkillType getSkill() { return skill; }
  
  /**
   * Returns the experience that was gained
   * @return The experience that was gained
   */
  public int getExperience() { return experience; }
  
  /**
   * Modifies the experience being gained.
   * @param value The new value of the experience gained
   */
  public void setExperience(int value) { experience = value; }
  
  @Override public boolean isCancelled() { return cancelled; }
  @Override public void setCancelled(boolean cancel) { cancelled = cancel; }

}
