package com.gmail.alexdion93.aeonrpg.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.persistence.PersistentDataHolder;

import com.gmail.alexdion93.aeonrpg.data.type.RPGDataType;

/**
 * A custom event designed to be thrown whenever someone fetches
 * rpg data. This allows other sources to modify the final value
 * of the data.
 * @author Alex Dion
 *
 */
public class RPGDataFetchEvent extends Event {

  private static HandlerList handlers = new HandlerList();

  /**
   * Returns the handlers tied to this event
   * @return The handlers tied to this event
   */
  public static HandlerList getHandlerList() {
    return handlers;
  }

  private PersistentDataHolder holder;
  private RPGDataType type;
  private int value;

  /**
   * Constructor
   * @param type   The type being fetched.
   * @param holder The entity this is being fetched for
   * @param value  The initial value fetched from the type
   *
   * @throws NullPointerException if the type is null
   * @throws NullPointerException if the entity is null
   */
  public RPGDataFetchEvent(PersistentDataHolder holder, RPGDataType type, int value) {
    if (holder == null) { throw new NullPointerException("Provided entity cannot be null"); }
    if (type == null) { throw new NullPointerException("Provided type cannot be null"); }
    
    this.type = type;
    this.value = value;
    this.holder = holder;
  }

  @Override
  public HandlerList getHandlers() {
    return handlers;
  }

  /**
   * Returns the attached data holder
   *
   * @return the attached data holder
   */
  public PersistentDataHolder getHolder() {
    return holder;
  }

  /**
   * Returns the attached type
   *
   * @return the attached type
   */
  public RPGDataType getType() {
    return type;
  }

  /**
   * Returns the value of the attribute
   *
   * @return the value of the attribute
   */
  public int getValue() {
    return value;
  }

  /**
   * Sets the value of the attribute
   *
   * @param value the value of the attribute
   */
  public void setValue(int value) {
    this.value = value;
  }

}
