package com.gmail.alexdion93.aeonrpg.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.gmail.alexdion93.aeonrpg.data.type.RPGSkillType;

/**
 * A custom event designed to be thrown whenever someone fetches
 * rpg data. This allows other sources to modify the final value
 * of the data.
 * @author Alex Dion
 *
 */
public class RPGSkillLevelUpEvent extends Event {

  private static HandlerList handlers = new HandlerList();
  private final RPGSkillType skill;
  private final Player player;
  private int newLevel;
  private int oldLevel;

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
   * @param oldLevel The level we're transitioning from
   * @param newLevel The level we're transitioning to
   */
  public RPGSkillLevelUpEvent(Player player, RPGSkillType skill, int oldLevel, int newLevel) {
    
    if (player == null) { throw new IllegalArgumentException("Player cannot be null"); }
    if (skill == null) { throw new IllegalArgumentException("Skill cannot be null"); }
    
    this.player = player;
    this.skill = skill;
    this.oldLevel = oldLevel;
    this.newLevel = newLevel;
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
   * Returns the level we're transitioning from
   * @return The level we're transitioning from
   */
  public int getOldLevel() { return oldLevel; }
  
  /**
   * Returns the level we're transitioning to
   * @return The level we're transitioning to
   */
  public int getNewLevel() { return newLevel; }

}
