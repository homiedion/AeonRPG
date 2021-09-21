package com.gmail.alexdion93.aeonrpg.data.interfaces;

import org.bukkit.NamespacedKey;

/**
 * Denotes the implementing object as having a single integer value.
 * @author Alex Dion
 */
public interface RPGDataOneValued {

  /**
   * Returns the namespaced key tied to the secondary value.
   * @return the namespaced key tied to the secondary value.
   */
  public NamespacedKey getPrimaryKey();
  
  /**
   * Returns the default value for the primary data value.
   * @return the default value for the primary data value.
   */
  public int getPrimaryDefaultValue();
}
