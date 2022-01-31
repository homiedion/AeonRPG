package com.gmail.alexdion93.aeonrpg.data.type;

import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import com.gmail.alexdion93.aeonrpg.data.interfaces.*;

/**
 * Represents an attribute.
 * @author Alex Dion
 */
public abstract class RPGAttributeType extends RPGDataType implements RPGDataTwoValued, Listener {

  private NamespacedKey primary;
  private NamespacedKey secondary;
  
  /**
   * Constructor
   * @param plugin      The target plugin.
   * @param key         The key of this type.
   * @param displayName The display name of this type.
   * @param description The type's description
   */
  public RPGAttributeType(JavaPlugin plugin, String key, String displayName, String description) {
    super(plugin, "attribute." + key.toLowerCase(), displayName, description);
    primary = new NamespacedKey(plugin, "attribute." + key.toLowerCase() + ".flat");
    secondary = new NamespacedKey(plugin, "attribute." + key.toLowerCase() + ".scaling");
  }

  @Override
  public RPGDataAlignment getAlignment() { return RPGDataAlignment.POSITIVE; }
  
  /**
   * Returns the flat value present on an entity and if possible their equipment.
   * @param entity The target entity.
   * @return The total flat value found on the entity and their equipment.
   */
  public int getFlat(Entity entity) {
    return getSum(entity, getPrimaryKey());
  }

  /**
   * Returns the flat value of the attribute.
   * @param container The persistent data container.
   * @return The flat value of the attribute.
   */
  public int getFlat(PersistentDataContainer container) {
    return get(container, getPrimaryKey(), PersistentDataType.INTEGER, 0);
  }
  
  /**
   * Returns the flat value of the attribute.
   * @param container The persistent data container.
   * @return The flat value of the attribute.
   */
  public int getFlat(PersistentDataHolder holder) {
    return getFlat(holder.getPersistentDataContainer());
  }

  @Override
  public int getPrimaryDefaultValue() { return 0; }

  @Override
  public NamespacedKey getPrimaryKey() { return primary; }

  /**
   * Returns the scaling value present on an entity and if possible their equipment.
   * @param entity The target entity.
   * @return The total scaling value found on the entity and their equipment.
   */
  public int getScaling(Entity entity) {
    return getSum(entity, getSecondaryKey());
  }

  /**
   * Returns the scaling value of the attribute.
   * @param container The persistent data container.
   * @return The scaling value of the attribute.
   */
  public int getScaling(PersistentDataContainer container) {
    return get(container, getSecondaryKey(), PersistentDataType.INTEGER, 0);
  }
  
  /**
   * Returns the scaling value of the attribute.
   * @param container The persistent data container.
   * @return The scaling value of the attribute.
   */
  public int getScaling(PersistentDataHolder holder) {
    return getScaling(holder.getPersistentDataContainer());
  }
  
  @Override
  public int getSecondaryDefaultValue() { return 0; }
  
  @Override
  public NamespacedKey getSecondaryKey() { return secondary; }
  
  @Override
  public boolean isNegativeAllowed() { return false; }
  
  /**
   * Allows the player to have an innate attribute value
   *
   * @param event The event being fired.
   */
  @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
  public void onPlayerJoin(PlayerJoinEvent event) {
    Player player = event.getPlayer();
    
    //Primary Data Value
    set(player, getPrimaryKey(), PersistentDataType.INTEGER, getPrimaryDefaultValue());
    
    //Secondary Data Value
    if (this instanceof RPGDataTwoValued) {
      RPGDataTwoValued alt = (RPGDataTwoValued) this;
      NamespacedKey key = alt.getSecondaryKey();
      
      if (alt.getSecondaryDefaultValue() != 0 && !has(player, key, PersistentDataType.INTEGER)) {
        set(player, key, PersistentDataType.INTEGER, alt.getSecondaryDefaultValue());
      }
    }
    
    //String Type
    if (this instanceof RPGDataString) {
      RPGDataString alt = (RPGDataString) this;
      NamespacedKey key = alt.getStringKey();
      
      if (alt.getStringDefaultValue() != null && !has(player, key, PersistentDataType.STRING)) {
        set(player, key, PersistentDataType.STRING, alt.getStringDefaultValue());
      }
    }
  }
  
  /**
   * Sets the attribute's flat value on the container.
   * @param container The persistent data container.
   * @param value The value we're setting this to.
   */
  public void setFlat(PersistentDataContainer container, int value) {
    if (value == 0 || (value < 0 && !isNegativeAllowed())) {
      remove(container, getPrimaryKey(), PersistentDataType.INTEGER, 0);
      return;
    }
    set(container, getPrimaryKey(), PersistentDataType.INTEGER, value);
  }
  
  /**
   * Sets the attribute's flat value on the holder.
   * @param holder The persistent data holder.
   * @param value The value we're setting this to.
   */
  public void setFlat(PersistentDataHolder holder, int value) {
    setFlat(holder.getPersistentDataContainer(), value);
  }
  
  /**
   * Sets the attribute's scaling value on the container.
   * @param container The persistent data container.
   * @param value The value we're setting this to.
   */
  public void setScaling(PersistentDataContainer container, int value) {
    if (value == 0 || (value < 0 && !isNegativeAllowed())) {
      remove(container, getSecondaryKey(), PersistentDataType.INTEGER, value);
      return;
    }
    set(container, getSecondaryKey(), PersistentDataType.INTEGER, value);
  }
  
  /**
   * Sets the attribute's scaling value on the holder.
   * @param holder The persistent data holder.
   * @param value The value we're setting this to.
   */
  public void setScaling(PersistentDataHolder holder, int value) {
    setScaling(holder.getPersistentDataContainer(), value);
  }

  @Override
  public String toLoreString(Object... values) {
    
    //Exit if no value
    if (values.length == 0) { return null; }
    
    //Variables
    String result = "";
    boolean isNegative = (getAlignment() == RPGDataAlignment.NEGATIVE); 
    String positiveColor = (isNegative ? ChatColor.RED : ChatColor.GREEN) + "+";
    String negativeColor = (isNegative ? ChatColor.GREEN : ChatColor.RED) + "";
    int flat = (values.length >= 1 && values[0] != null ? (int) values[0] : 0);
    int scale = (values.length >= 2 && values[1] != null ? (int) values[1] : 0);
    
    //Level String
    if (flat != 0) {
      result += (flat > 0 ? positiveColor : negativeColor) + flat + ChatColor.GRAY + " " + getDisplayName();
    }
    
    //Sccaling String
    if (scale != 0) {
      result += "\n" + (scale > 0 ? positiveColor: negativeColor) + scale + "% "+ ChatColor.GRAY + getDisplayName();
    }
    
    //Return
    return result.trim();
  }
  

  /**
   * Returns the total value of the attribute.
   * @param entity The target entity.
   * @return The total value of the attribute.
   */
  public int getTotal(Entity entity) {
    
    int flat = getFlat(entity);
    int scaling = getScaling(entity);
    double multiplier = 1 + (scaling / 100.0);
    
    return (int) (flat * multiplier);
  }
  
  /**
   * Returns the total value of the attribute.
   * @param container The persistent data container.
   * @return The total value of the attribute.
   */
  public int getTotal(PersistentDataContainer container) {
    
    int flat = getFlat(container);
    int scaling = getScaling(container);
    double multiplier = 1 + (scaling / 100.0);
    
    return (int) (flat * multiplier);
  }
  
  /**
   * Returns the total value of the attribute.
   * @param container The persistent data container.
   * @return The total value of the attribute.
   */
  public int getTotal(PersistentDataHolder holder) {
    return getTotal(holder.getPersistentDataContainer());
  }
}
