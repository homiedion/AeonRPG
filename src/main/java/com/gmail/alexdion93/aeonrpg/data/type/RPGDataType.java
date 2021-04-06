package com.gmail.alexdion93.aeonrpg.data.type;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import com.gmail.alexdion93.aeonrpg.data.interfaces.RPGDataAligned;
import com.gmail.alexdion93.aeonrpg.data.interfaces.RPGDataNegative;
import com.gmail.alexdion93.aeonrpg.data.interfaces.RPGDataString;
import com.gmail.alexdion93.aeonrpg.data.interfaces.RPGDataTwoValued;
import com.gmail.alexdion93.aeonrpg.data.interfaces.RPGDataValued;
import com.gmail.alexdion93.aeonrpg.events.RPGDataFatchEvent;
import com.gmail.alexdion93.inventoryequipevent.util.ItemUtil;

public abstract class RPGDataType implements RPGDataAligned, RPGDataNegative {

  private String displayName = null;
  private NamespacedKey key = null;
  private String description;

  /**
   * Constructor
   *
   * @param plugin  The target plugin.
   * @param key The key of this type.
   * @param displayName The display name of this type.
   * @param description The description of the data type
   */
  public RPGDataType(final JavaPlugin plugin, final String key, final String displayName, final String description) {
    // Data Validation
    if (plugin == null) {
      throw new IllegalArgumentException("Plugin cannot be null");
    }
    if (key == null) {
      throw new IllegalArgumentException("Key cannot be null");
    }
    if (displayName == null) {
      throw new IllegalArgumentException("Display name cannot be null");
    }

    // Assignment
    this.key = new NamespacedKey(plugin, key);
    this.displayName = displayName;
    this.description = description;
  }

  /**
   * Returns the description of this data type.
   *
   * @return The description of this data type.
   */
  public String getDescription() {
    return description;
  }

  /**
   * Returns the display name of this data type.
   *
   * @return The display name of this data type.
   */
  public String getDisplayName() {
    return displayName;
  }

  /**
   * Fetches a value from the entity but use logic for projectile weapons.
   * Projectile weapons use the stats from: All equipped armor The projectile
   * weapon itself (bow, crossbow, trident, etc.) The projectile weapon's ammo or
   * the unused hand (whichever hand the weapon isn't using)
   * 
   * @param entity The target entity
   * @param key The namespaced key we're searching for
   * @return An integer sum of all keys found
   */
  public int getEntityProjectileValue(Entity entity, NamespacedKey key) {

    // Null Check
    if (entity == null) {
      throw new NullPointerException("Entity cannot be null!");
    }
    if (key == null) {
      throw new NullPointerException("Key cannot be null!");
    }

    // Variables
    int result = 0;
    ItemStack primary = null;
    ItemStack secondary = null;
    EquipmentSlot slot = null;
    PersistentDataContainer data;

    // Check the entity for its own data
    data = entity.getPersistentDataContainer();
    if (data.has(key, PersistentDataType.INTEGER)) {
      result += data.get(key, PersistentDataType.INTEGER);
    }

    // Living Entity Check
    if (entity instanceof LivingEntity) {

      LivingEntity livingEntity = (LivingEntity) entity;

      // Loop Armor
      for (ItemStack i : livingEntity.getEquipment().getArmorContents()) {

        // Skip if null or air
        if ((i == null) || (i.getType() == Material.AIR)) {
          continue;
        }
        ItemMeta meta = i.getItemMeta();

        // Skip if no meta data
        if (meta == null) {
          continue;
        }
        data = meta.getPersistentDataContainer();

        // Fetch the data from this item
        if (data.has(key, PersistentDataType.INTEGER)) {
          result += data.get(key, PersistentDataType.INTEGER);
        }
      }

      // Determine the primary and secondary item
      /*
       * The primary item is the item being throw. The secondary item is one of the
       * following with earlier taking priority The ammunition for a shootable item
       * The other held item slot
       */

      primary = livingEntity.getEquipment().getItemInMainHand();
      slot = EquipmentSlot.HAND;

      // If the primary item isn't shootable and isn't throwable then we must be using
      // the off hand.
      if (!ItemUtil.isCategory(primary, ItemUtil.SHOOTABLE) && !ItemUtil.isCategory(primary, ItemUtil.THROWABLE)) {
        primary = livingEntity.getEquipment().getItemInOffHand();
        slot = EquipmentSlot.OFF_HAND;
      }

      // If the primary item is a shootable, fetch the arrow slot
      if (ItemUtil.isCategory(primary, ItemUtil.SHOOTABLE)) {

        // Get the crossbow's loaded arrow (or just the first valid one since multiple
        // can be added)
        if (primary.getType() == Material.CROSSBOW) {

          ItemMeta meta = primary.getItemMeta();
          if ((meta != null) && (meta instanceof CrossbowMeta)) {

            if (((CrossbowMeta) meta).hasChargedProjectiles()) {
              for (ItemStack item : ((CrossbowMeta) meta).getChargedProjectiles()) {
                if ((item == null) || (item.getItemMeta() == null)) {
                  continue;
                }
                secondary = item;
                break;
              }
            }
          }
        }

        // Check the off hand for arrows
        else if (ItemUtil.isCategory(livingEntity.getEquipment().getItemInOffHand(), ItemUtil.ARROWS)) {
          secondary = livingEntity.getEquipment().getItemInOffHand();
        }

        // Check the inventory for arrows
        else if (livingEntity instanceof InventoryHolder) {

          InventoryHolder holder = (InventoryHolder) livingEntity;
          for (ItemStack item : holder.getInventory().getContents()) {
            if (ItemUtil.isCategory(item, ItemUtil.ARROWS)) {
              secondary = item;
              break;
            }
          }
        }
      }

      // Otherwise just use the other hand (this is mainly for throwable items)
      else {
        secondary = (slot == EquipmentSlot.HAND ? livingEntity.getEquipment().getItemInOffHand()
            : livingEntity.getEquipment().getItemInMainHand());
      }

      // Primary Item
      if ((primary != null) && (primary.getItemMeta() != null)) {
        data = primary.getItemMeta().getPersistentDataContainer();
        if (data.has(key, PersistentDataType.INTEGER)) {
          result += data.get(key, PersistentDataType.INTEGER);
        }
      }

      // Secondary Item
      if ((secondary != null) && (secondary.getItemMeta() != null)) {
        data = secondary.getItemMeta().getPersistentDataContainer();
        if (data.has(key, PersistentDataType.INTEGER)) {
          result += data.get(key, PersistentDataType.INTEGER);
        }
      }
    }

    // Throw an event to allow modifications
    RPGDataFatchEvent event = new RPGDataFatchEvent(entity, this, result);
    Bukkit.getPluginManager().callEvent(event);

    // Return the value after any modifications
    return event.getValue();
  }

  /**
   * Returns the amount of this data is present within an entity and its
   * equipment.
   *
   * @param holder The persistent data holder.
   * @param key The namespaced key we're searching for
   * @return The gathered value or 0.
   * @throws NullPointerException If the entity is null;
   */
  public int getEntityValue(PersistentDataHolder holder, NamespacedKey key) {

    // Null Check
    if (holder == null) {
      throw new NullPointerException("Holder cannot be null!");
    }
    if (key == null) {
      throw new NullPointerException("Key cannot be null!");
    }

    // Variables
    int result = 0;
    ItemStack item;
    PersistentDataContainer data;

    // Check the entity for its own data
    data = holder.getPersistentDataContainer();
    if (data.has(key, PersistentDataType.INTEGER)) {
      result += data.get(key, PersistentDataType.INTEGER);
    }

    // Living Entity Check
    if (holder instanceof LivingEntity) {

      LivingEntity livingEntity = (LivingEntity) holder;

      // Loop Armor
      for (ItemStack i : livingEntity.getEquipment().getArmorContents()) {

        // Skip if null or air
        if ((i == null) || (i.getType() == Material.AIR)) {
          continue;
        }
        ItemMeta meta = i.getItemMeta();

        // Skip if no meta data
        if (meta == null) {
          continue;
        }
        data = meta.getPersistentDataContainer();

        // Fetch the data from this item
        if (data.has(key, PersistentDataType.INTEGER)) {
          result += data.get(key, PersistentDataType.INTEGER);
        }
      }

      // Main Hand
      item = livingEntity.getEquipment().getItemInMainHand();
      if (!isIgnoredItem(item) && (item.getItemMeta() != null)) {

        // Variables
        ItemMeta meta = item.getItemMeta();
        data = meta.getPersistentDataContainer();

        // Fetch the data from this item
        if (data.has(key, PersistentDataType.INTEGER)) {
          result += data.get(key, PersistentDataType.INTEGER);
        }
      }

      // Off Hand
      item = livingEntity.getEquipment().getItemInOffHand();
      if (!isIgnoredItem(item) && (item.getItemMeta() != null)) {
        // Variables
        ItemMeta meta = item.getItemMeta();
        data = meta.getPersistentDataContainer();

        // Fetch the data from this item
        if (data.has(key, PersistentDataType.INTEGER)) {
          result += data.get(key, PersistentDataType.INTEGER);
        }
      }
    }

    // Throw an event to allow modifications
    RPGDataFatchEvent event = new RPGDataFatchEvent(holder, this, result);
    Bukkit.getPluginManager().callEvent(event);

    // Return the value after any modifications
    return event.getValue();
  }

  /**
   * Returns the key tied to this data type.
   *
   * @return The key tied to this data type.
   */
  public NamespacedKey getNamespacedKey() {
    return key;
  }

  /**
   * Returns if the item is ignored
   *
   * @param item The target item
   * @return true if this is ignored
   */
  private boolean isIgnoredItem(ItemStack item) {
    return (ItemUtil.isCategory(item, ItemUtil.HELMETS) || ItemUtil.isCategory(item, ItemUtil.CHESTPLATES)
        || ItemUtil.isCategory(item, ItemUtil.LEGGINGS) || ItemUtil.isCategory(item, ItemUtil.BOOTS)
        || ItemUtil.isCategory(item, ItemUtil.ARROWS));
  }

  /**
   * Modifies an entity to give them specified key and value
   *
   * @param key    The provided key
   * @param holder The persistent data holder.
   * @param value  The amount we're setting the value to.
   */
  public void modifyTarget(NamespacedKey key, PersistentDataHolder holder, int value) {
    // Variables
    PersistentDataContainer data = holder.getPersistentDataContainer();
    PersistentDataType<Integer, Integer> type = PersistentDataType.INTEGER;

    // No value
    if (value == 0) {
      if (data.has(key, type)) {
        data.remove(key);
      }
      return;
    }

    // Add the data
    data.set(key, type, value);
  }

  /**
   * Modifies an entity to give them specified key and value
   *
   * @param key    The provided key
   * @param holder The persistent data holder.
   * @param value  The amount we're setting the value to.
   */
  public void modifyTarget(NamespacedKey key, PersistentDataHolder holder, String value) {
    // Variables
    PersistentDataContainer data = holder.getPersistentDataContainer();
    PersistentDataType<String, String> type = PersistentDataType.STRING;

    // No value
    if (value == null) {
      if (data.has(key, type)) {
        data.remove(key);
      }
      return;
    }

    // Add the data
    data.set(key, type, value);
  }

  /**
   * Modifies an entity to give them specified value
   *
   * @param holder The persistent data holder.
   * @param value  The amount we're setting the value to.
   */
  public void modifyTarget(PersistentDataHolder holder, int value) {
    modifyTarget(getNamespacedKey(), holder, value);
  }

  /**
   * Modifies an entity to give them specified value
   *
   * @param holder The persistent data holder.
   * @param value  The string we're setting the value to.
   */
  public void modifyTarget(PersistentDataHolder holder, String value) {
    modifyTarget(getNamespacedKey(), holder, value);
  }

  /**
   * Modifies an entity to give them specified key and value if they don't have
   * one.
   *
   * @param key    The provided key
   * @param holder The persistent data holder.
   * @param value  The amount we're setting the value to.
   */
  public void modifyTargetDefault(NamespacedKey key, PersistentDataHolder holder, int value) {
    // Variables
    PersistentDataContainer data = holder.getPersistentDataContainer();
    PersistentDataType<Integer, Integer> type = PersistentDataType.INTEGER;

    // No value
    if (value == 0) {
      if (data.has(key, type)) {
        data.remove(key);
      }
      return;
    }

    // Add the data if not already present
    if (!data.has(key, type)) {
      data.set(key, type, value);
    }
  }

  /**
   * Modifies an entity to give them specified key and value if they don't have
   * one.
   *
   * @param key    The provided key
   * @param holder The persistent data holder.
   * @param value  The string we're setting the value to.
   */
  public void modifyTargetDefault(NamespacedKey key, PersistentDataHolder holder, String value) {
    // Variables
    PersistentDataContainer data = holder.getPersistentDataContainer();
    PersistentDataType<String, String> type = PersistentDataType.STRING;

    // No value
    if (value == null) {
      if (data.has(key, type)) {
        data.remove(key);
      }
      return;
    }

    // Add the data if not already present
    if (!data.has(key, type)) {
      data.set(key, type, value);
    }
  }

  /**
   * Modifies an entity to give them specified key and value if they don't have
   * one.
   *
   * @param holder The persistent data holder.
   * @param value The amount we're setting the value to.
   */
  public void modifyTargetDefault(PersistentDataHolder holder, int value) {
    modifyTargetDefault(getNamespacedKey(), holder, value);
  }

  /**
   * Modifies an entity to give them specified key and value if they don't have
   * one.
   *
   * @param holder The persistent data holder.
   * @param value  The string we're setting the value to.
   */
  public void modifyTargetDefault(PersistentDataHolder holder, String value) {
    modifyTargetDefault(getNamespacedKey(), holder, value);
  }

  /**
   * Removes the data from the entity
   *
   * @param holder The persistent data holder.
   */
  public void removeFrom(PersistentDataHolder holder) {
    // Variables
    PersistentDataContainer data = holder.getPersistentDataContainer();
    NamespacedKey key = getNamespacedKey();

    // Remove if present
    if (this instanceof RPGDataString) {
      if (data.has(key, PersistentDataType.STRING)) {
        data.remove(key);
      }
    } else if (this instanceof RPGDataTwoValued) {
      NamespacedKey alt = ((RPGDataTwoValued) this).getAltNamespacedKey();
      if (data.has(key, PersistentDataType.INTEGER)) {
        data.remove(key);
      }
      if (data.has(alt, PersistentDataType.INTEGER)) {
        data.remove(alt);
      }
    } else if (this instanceof RPGDataValued) {
      if (data.has(key, PersistentDataType.INTEGER)) {
        data.remove(key);
      }
    }
  }
}
