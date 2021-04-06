package com.gmail.alexdion93.aeonrpg.data.interfaces;

import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;

public interface RPGDataValued {

  /**
   * Returns the default value for this data type
   *
   * @return the default value for this data type
   */
  public int getDefaultValue();

  /**
   * Triggers when the item is modifed
   *
   * @param meta     The meta being modified
   * @param material The material of the item
   * @param value    The value of this one.
   */
  public void modifyItem(ItemMeta meta, Material material, int value);

  /**
   * Generates the string displayed on an item's lore
   *
   * @param value The target value.
   * @return A string representing the type.
   */
  public String toLoreString(int value);
}
