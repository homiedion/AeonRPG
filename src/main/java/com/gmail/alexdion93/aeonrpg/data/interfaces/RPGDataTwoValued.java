package com.gmail.alexdion93.aeonrpg.data.interfaces;

import org.bukkit.NamespacedKey;

/**
 * Denotes the implementing object as having a two integer values.
 * @author Alex Dion
 */
public interface RPGDataTwoValued extends RPGDataOneValued {
  
  /**
   * Returns the namespaced key tied to the secondary value.
   * @return the namespaced key tied to the secondary value.
   */
  public NamespacedKey getSecondaryKey();
  
  /**
   * Returns the default value for the primary data value.
   * @return the default value for the primary data value.
   */
  public int getSecondaryDefaultValue();
}
