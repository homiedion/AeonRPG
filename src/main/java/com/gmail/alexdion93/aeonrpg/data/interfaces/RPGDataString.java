package com.gmail.alexdion93.aeonrpg.data.interfaces;

import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;

public interface RPGDataString {
  /**
   * Returns the default value of this type.
   *
   * @return The default value of this type.
   */
  public String getDefaultValue();

  /**
   * Triggers when the item is modifed
   *
   * @param meta  The meta being modified
   * @param value The value of this one.
   */
  public abstract void modifyItem(ItemMeta meta, Material material, String value);

  /**
   * Displays the string used as item lore.
   *
   * @param value The primary value tied to the lore string.
   * @return A formatted string.
   */
  public String toLoreString(String value);
}
