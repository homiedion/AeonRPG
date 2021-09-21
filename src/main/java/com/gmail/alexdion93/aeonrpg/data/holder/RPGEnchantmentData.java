package com.gmail.alexdion93.aeonrpg.data.holder;

/**
 * A data container used to hold enchantment value
 * @author Alex Dion
 */
public class RPGEnchantmentData {
  private int level = 0;
  
  /**
   * Constructor
   * @param level The level
   */
  public RPGEnchantmentData(int level) {
    this.level = level;
  }
  
  /**
   * Returns the level of the enchantment.
   * @return the level of the enchantment.
   */
  public int getLevel() { return level; }
  
  /**
   * Modifies the level of the enchantment.
   * @param level The new level.
   */
  public void setLevel(int level) { this.level = level; }
}
