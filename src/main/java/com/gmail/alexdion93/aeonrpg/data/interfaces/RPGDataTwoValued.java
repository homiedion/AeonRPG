package com.gmail.alexdion93.aeonrpg.data.interfaces;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.meta.ItemMeta;

public interface RPGDataTwoValued extends RPGDataValued {

  /**
   * Returns the alt namespaced key used for the secondary value
   *
   * @return The alt namespaced key used for the secondary value
   */
  public NamespacedKey getAltNamespacedKey();

  /**
   * Returns the default alt value for this data type
   *
   * @return the default alt value for this data type
   */
  public int getDefaultAltValue();

  /**
   * Modifies an entity to grant them this type
   *
   * @param entity   The target entity
   * @param value    The primary value
   * @param altValue The secondary value
   */
  public void modifyEntity(Entity entity, int value, int altValue);

  /**
   * Modifies an entity to grant them this type if they don't have it.
   *
   * @param entity   The target entity
   * @param value    The primary value
   * @param altValue The secondary value
   */
  public void modifyEntityDefault(Entity entity, int value, int altValue);

  /**
   * Triggers when the item is modifed
   *
   * @param meta     The meta being modified
   * @param material The material of the item
   * @param value    The value of this one.
   * @param altValue The alt value of this one.
   */
  public abstract void modifyItem(ItemMeta meta, Material material, int value, int altValue);

  /**
   * Displays the string used as item lore.
   *
   * @param value    The primary value tied to the lore string.
   * @param altValue The secondary value tied to the lore string.
   * @return A formatted string.
   */
  public String toLoreString(int value, int altValue);
}
