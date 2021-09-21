package com.gmail.alexdion93.aeonrpg.data.interfaces;

import org.bukkit.NamespacedKey;

/**
 * Denotes the implementing object as having a single string value.
 * @author Alex Dion
 */
public interface RPGDataString {

  /**
   * Returns the namespaced key tied to the string value.
   * @return the namespaced key tied to the string value.
   */
  public NamespacedKey getStringKey();
  
  /**
   * Returns the default value for the string data value.
   * @return the default value for the string data value.
   */
  public String getStringDefaultValue();
}
