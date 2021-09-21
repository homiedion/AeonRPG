package com.gmail.alexdion93.aeonrpg.data.holder;

/**
 * A data container used to hold skill values.
 * @author Alex Dion
 */
public class RPGSkillData {
  private int level = 0;
  private int experience = 0;
  
  /**
   * Constructor
   * @param level The level of skill.
   * @param experience The experience of the skill.
   */
  public RPGSkillData(int level, int experience) {
    this.level = level;
    this.experience = experience;
  }
  
  /**
   * Returns the level of the skill.
   * @return the level of the skill.
   */
  public int getLevel() { return level; }
  
  /**
   * Modifies the level of the skill.
   * @param level The new level.
   */
  public void setLevel(int level) { this.level = level; }
  
  /**
   * Returns the experience of the skill.
   * @return the experience of the skill.
   */
  public int getExperience() { return experience; }
  
  /**
   * Modifies the experience of the skill.
   * @param experience The new experience.
   */
  public void setExperience(int experience) { this.experience = experience; }
}
