package com.gmail.alexdion93.aeonrpg.data.interfaces;

public interface RPGDataLevelIndependent {

  /**
   * Returns if level doesn't modify affect this type. Types where this is true do
   * not display roman numerals (ie. "Silk Touch").
   *
   * @return True if level doesn't affect this type.
   */
  public boolean isLevelIndependent();
}
