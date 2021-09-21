package com.gmail.alexdion93.aeonrpg.data.type;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.bukkit.inventory.meta.ItemMeta;

import com.gmail.alexdion93.aeonrpg.events.RPGDataFetchEvent;
import com.gmail.alexdion93.inventoryequipevent.util.ItemUtil;

/**
 * A base container for information regarding specific rpg data.
 * @author Alex Dion
 */
public abstract class RPGDataType {
  
  /**
   * An enum that represents whether or not this Data Type is positive, negative or neutral.
   * @author Alex Dion
   */
  public enum RPGDataAlignment { NEGATIVE, NEUTRAL, POSITIVE; }
  
  //Member Variables
  private String displayName = null;
  private String description;
  private String key;
  
  /**
   * Constructor
   *
   * @param plugin  The target plugin.
   * @param key The key of this type.
   * @param displayName The display name of this type.
   * @param description The description of the data type
   */
  public RPGDataType(JavaPlugin plugin, String key, String displayName, String description) {
    // Data Validation
    if (plugin == null) {
      throw new IllegalArgumentException("Plugin cannot be null");
    }
    if (displayName == null) {
      throw new IllegalArgumentException("Display name cannot be null");
    }

    // Assignment
    this.displayName = displayName;
    this.description = description;
    this.key = key;
  }
  
  /**
   * Gets the value present in the data container.
   * @param <T> The complex data type.
   * @param <Z> The primitive data type.
   * @param container The persistent data container.
   * @param key The namespaced key we're setting.
   * @param type The persistent data type.
   * @param value The default value if nothing is found.
   * @return The value if present or a default value provided
   */
  public <T, Z> Z get(PersistentDataContainer container, NamespacedKey key, PersistentDataType<T, Z> type, Z value) { 
    return container.getOrDefault(key, type, value);
  }
  
  /**
   * Gets the value present in the data holder.
   * @param <T> The complex data type.
   * @param <Z> The primitive data type.
   * @param holder The persistent data holder.
   * @param key The namespaced key we're setting.
   * @param type The persistent data type.
   * @param value The default value if nothing is found.
   * @return The value if present or a default value provided
   */
  public <T, Z> Z get(PersistentDataHolder holder, NamespacedKey key, PersistentDataType<T, Z> type, Z value) {
    return get(holder.getPersistentDataContainer(), key, type, value);
  }
  
  /**
   * Returns the alignment of this object.
   *
   * @return The alignment of this object.
   */
  public abstract RPGDataAlignment getAlignment();
  
  /**
   * Returns the description of this data type.
   *
   * @return The description of this data type.
   */
  public String getDescription() { return description; }
  
  /**
   * Returns the display name of this data type.
   *
   * @return The display name of this data type.
   */
  public String getDisplayName() { return displayName; }
  
  /**
   * Returns the sum of all integer values across an entity's equipment.
   * @param entity The target entity.
   * @param key The target key.
   * @return An integer.
   */
  public int getEntityProjectileSum(LivingEntity entity, NamespacedKey key) {
    //Variables
    int sum = 0;
    ItemStack primary = null;
    ItemStack secondary = null;
    EquipmentSlot slot;
    ItemMeta meta;
    
    //Entity Data
    sum += get(entity.getPersistentDataContainer(), key, PersistentDataType.INTEGER, 0);
    
    //Armor
    for(ItemStack i : entity.getEquipment().getArmorContents()) {
      if (i == null) { continue; }
      meta = i.getItemMeta();
      if (meta == null) { continue; }
      sum += get(meta.getPersistentDataContainer(), key, PersistentDataType.INTEGER, 0);
    }
    
    //Determine what is the weapon and what is the projectile
    primary = entity.getEquipment().getItemInMainHand();
    slot = EquipmentSlot.HAND;
    
    // If the primary item isn't shootable and isn't throwable
    // then we must be using the off hand.
    if (!ItemUtil.isCategory(primary, ItemUtil.SHOOTABLE) && !ItemUtil.isCategory(primary, ItemUtil.THROWABLE)) {
      primary = entity.getEquipment().getItemInOffHand();
      slot = EquipmentSlot.OFF_HAND;
    }
    
    // If the primary item is a shootable
    // then fetch the arrow
    if (ItemUtil.isCategory(primary, ItemUtil.SHOOTABLE)) {

      // Get the crossbow's loaded arrow (or just the first valid one since multiple
      // can be added)
      if (primary.getType() == Material.CROSSBOW) {

        meta = primary.getItemMeta();
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
      else if (ItemUtil.isCategory(entity.getEquipment().getItemInOffHand(), ItemUtil.ARROWS)) {
        secondary = entity.getEquipment().getItemInOffHand();
      }

      // Check the inventory for arrows
      else if (entity instanceof InventoryHolder) {
        InventoryHolder holder = (InventoryHolder) entity;
        for (ItemStack item : holder.getInventory().getContents()) {
          if (!ItemUtil.isCategory(item, ItemUtil.ARROWS)) { continue; }
          secondary = item;
          break;
        }
      }
    }

    // Otherwise just use the other hand
    // this accounts for throwable items
    else {
      secondary = (slot == EquipmentSlot.HAND ? entity.getEquipment().getItemInOffHand()
          : entity.getEquipment().getItemInMainHand());
    }
    
    //Primary Value
    if (primary != null && !isIgnoredItem(primary)) {
      meta = primary.getItemMeta();
      if (meta != null) {
        sum += get(meta.getPersistentDataContainer(), key, PersistentDataType.INTEGER, 0);
      }
    }
    
    //Secondary Value
    if (secondary != null && !isIgnoredItem(secondary)) {
      meta = secondary.getItemMeta();
      if (meta != null) {
        sum += get(meta.getPersistentDataContainer(), key, PersistentDataType.INTEGER, 0);
      }
    }
    
    //Throw an event to allow for other plugins to hook into.
    RPGDataFetchEvent event = new RPGDataFetchEvent(entity, this, sum);
    Bukkit.getPluginManager().callEvent(event);
    
    //Return result
    return event.getValue();
  }
  
  /**
   * Returns the sum of all integer values across an entity's equipment.
   * @param entity The target entity.
   * @param key The target key.
   * @return An integer.
   */
  public int getEntitySum(LivingEntity entity, NamespacedKey key) { 
    
    //Variables
    int sum = 0;
    ItemStack item;
    ItemMeta meta;
    
    //Entity Data
    sum += get(entity.getPersistentDataContainer(), key, PersistentDataType.INTEGER, 0);
    
    //Armor
    for(ItemStack i : entity.getEquipment().getArmorContents()) {
      if (i == null) { continue; }
      meta = i.getItemMeta();
      if (meta == null) { continue; }
      sum += get(meta.getPersistentDataContainer(), key, PersistentDataType.INTEGER, 0);
    }
    
    //Main Hand
    item = entity.getEquipment().getItemInMainHand();
    if (item != null && !isIgnoredItem(item)) {
      meta = item.getItemMeta();
      if (meta != null) {
        sum += get(meta.getPersistentDataContainer(), key, PersistentDataType.INTEGER, 0);
      }
    }
    
    //Off Hand
    item = entity.getEquipment().getItemInMainHand();
    if (item != null && !isIgnoredItem(item)) {
      meta = item.getItemMeta();
      if (meta != null) {
        sum += get(meta.getPersistentDataContainer(), key, PersistentDataType.INTEGER, 0);
      }
    }
    
    //Throw an event to allow for other plugins to hook into.
    RPGDataFetchEvent event = new RPGDataFetchEvent(entity, this, sum);
    Bukkit.getPluginManager().callEvent(event);
    
    //Return result
    return event.getValue();
  }
  
  /**
   * Sets this value to the persistent data container.
   * @param <T> The complex data type.
   * @param <Z> The primitive data type.
   * @param container The persistent data container.
   * @param key The namespaced key we're setting.
   * @param type The persistent data type.
   * @return True if present
   */
  public <T, Z> boolean has(PersistentDataContainer container, NamespacedKey key, PersistentDataType<T, Z> type) {
    return container.has(key, type);
  }
  
  /**
   * Sets this value to the persistent data holder.
   * @param <T> The complex data type.
   * @param <Z> The primitive data type.
   * @param holder The persistent data holder.
   * @param key The namespaced key we're setting.
   * @param type The persistent data type.
   * @return True if present
   */
  public <T, Z> boolean has(PersistentDataHolder holder, NamespacedKey key, PersistentDataType<T, Z> type) {
    return has(holder.getPersistentDataContainer(), key, type);
  }
  
  /**
   * Returns if the item is ignored.
   * @param item The target itemstack.
   * @return True or false.
   */
  private boolean isIgnoredItem(ItemStack item) {
    return (ItemUtil.isCategory(item, ItemUtil.HELMETS) || ItemUtil.isCategory(item, ItemUtil.CHESTPLATES)
        || ItemUtil.isCategory(item, ItemUtil.LEGGINGS) || ItemUtil.isCategory(item, ItemUtil.BOOTS)
        || ItemUtil.isCategory(item, ItemUtil.ARROWS));
  }
  
  /**
   * Removes this value from persistent data container.
   * @param <T> The complex data type.
   * @param <Z> The primitive data type.
   * @param container The persistent data container.
   * @param key The namespaced key we're setting.
   * @param type The persistent data type.
   * @param value The value to return if not found.
   * @return The previous value held.
   */
  public <T, Z> Z remove(PersistentDataContainer container, NamespacedKey key, PersistentDataType<T, Z> type, Z value) {
    Z old = container.getOrDefault(key, type, value);
    container.remove(key);
    return old;
  }
  
  /**
   * Removes this value from persistent data container.
   * @param <T> The complex data type.
   * @param <Z> The primitive data type.
   * @param container The persistent data holder.
   * @param key The namespaced key we're setting.
   * @param type The persistent data type.
   * @param value The value to return if not found.
   * @return The previous value held.
   */
  public <T, Z> Z remove(PersistentDataHolder holder, NamespacedKey key, PersistentDataType<T, Z> type, Z value) {
    return remove(holder.getPersistentDataContainer(), key, type, value);
  }
  
  /**
   * Sets this value to the persistent data container.
   * @param <T> The complex data type.
   * @param <Z> The primitive data type.
   * @param container The persistent data container.
   * @param key The namespaced key we're setting.
   * @param type The persistent data type.
   * @param value The new value to be set
   * @return The previous value held.
   */
  public <T, Z> Z set(PersistentDataContainer container, NamespacedKey key, PersistentDataType<T, Z> type, Z value) {
    Z old = container.get(key, type);
    container.set(key, type, value);
    return old;
  }
  
  /**
   * Sets this value to the persistent data holder.
   * @param <T> The complex data type.
   * @param <Z> The primitive data type.
   * @param holder The persistent data holder.
   * @param key The namespaced key we're setting.
   * @param type The persistent data type.
   * @param value The new value to be set
   * @return The previous value held.
   */
  public <T, Z> Z set(PersistentDataHolder holder, NamespacedKey key, PersistentDataType<T, Z> type, Z value) {
    return set(holder.getPersistentDataContainer(), key, type, value);
  }
  
  /**
   * Generates the lore string generated by this data type.
   * @param values The values attached to this.
   * @return A string representing the data type.
   */
  public abstract String toLoreString(Object... values);
  
  /**
   * Returns if this data type allows for negative values.
   * @return A boolean that represents if negative values are allowed.
   */
  public abstract boolean isNegativeAllowed();
  
  /**
   * Returns the data key tied to this rpg data.
   * @return A string key.
   */
  public String getDataKey() { return key; }
}
