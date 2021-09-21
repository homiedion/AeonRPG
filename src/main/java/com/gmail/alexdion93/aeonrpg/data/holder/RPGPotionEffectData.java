package com.gmail.alexdion93.aeonrpg.data.holder;

/**
 * A data container used to hold potion effect values
 * @author Alex Dion
 */
public class RPGPotionEffectData {
  private int level = 0;
  private int duration = 0;
  
  /**
   * Constructor
   * @param level The level of potion effect.
   * @param duration The duration of the potion effect.
   */
  public RPGPotionEffectData(int level, int duration) {
    this.level = level;
    this.duration = duration;
  }
  
  /**
   * Returns the level of the potion effect.
   * @return the level of the potion effect.
   */
  public int getLevel() { return level; }
  
  /**
   * Modifies the level of the potion effect.
   * @param level The new level.
   */
  public void setLevel(int level) { this.level = level; }
  
  /**
   * Returns the duration of the potion effect.
   * @return the duration of the potion effect.
   */
  public int getDuration() { return duration; }
  
  /**
   * Modifies the duration of the potion effect.
   * @param duration The new duration.
   */
  public void setDuration(int duration) { this.duration = duration; }
}
