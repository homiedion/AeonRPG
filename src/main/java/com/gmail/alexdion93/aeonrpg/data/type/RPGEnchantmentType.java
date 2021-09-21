package com.gmail.alexdion93.aeonrpg.data.type;

import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import com.gmail.alexdion93.aeonrpg.data.interfaces.RPGDataOneValued;
import com.gmail.alexdion93.aeonrpg.util.RomanNumeral;

/**
 * Represents an enchantment.
 * @author Alex Dion
 */
public abstract class RPGEnchantmentType extends RPGDataType implements RPGDataOneValued {

  private NamespacedKey primary;
  
  /**
   * Constructor
   *
   * @param plugin      The targe tplugin.
   * @param key         The key of this type.
   * @param displayName The display name of this type.
   * @param description This type's description
   */
  public RPGEnchantmentType(JavaPlugin plugin, String key, String displayName, String description) {
    super(plugin, "enchantment." + key.toLowerCase(), displayName, description);
    primary = new NamespacedKey(plugin, "enchantment." + key.toLowerCase());
  }
  
  @Override
  public RPGDataAlignment getAlignment() { return RPGDataAlignment.POSITIVE; }
  
  /**
   * Returns the level of the enchantment.
   * @param container The persistent data container.
   * @return The level of the enchantment.
   */
  public int getLevel(PersistentDataContainer container) {
    return get(container, getPrimaryKey(), PersistentDataType.INTEGER, 0);
  }
  
  /**
   * Returns the level of the enchantment.
   * @param container The persistent data container.
   * @return The level of the enchantment.
   */
  public int getLevel(PersistentDataHolder holder) {
    return getLevel(holder.getPersistentDataContainer());
  }
  
  @Override
  public int getPrimaryDefaultValue() { return 0; }
  
  @Override
  public NamespacedKey getPrimaryKey() { return primary; }
  
  /**
   * Returns if this enchantment should display level.
   * This is primarily used to emulate enchantments
   * that have a max level of 1 such as "Silk Touch"
   * @return True if the level is displayed.
   */
  public boolean isLevelDisplayed() { return true; }
  
  @Override
  public boolean isNegativeAllowed() { return false; }
  
  /**
   * Sets the enchantment's level on the container.
   * @param container The persistent data container.
   * @param value The value we're setting this to.
   */
  public void setLevel(PersistentDataContainer container, int value) {
    if (value <= 0 || (value < 0 && !isNegativeAllowed())) {
      remove(container, getPrimaryKey(), PersistentDataType.INTEGER, 0);
      return;
    }
    set(container, getPrimaryKey(), PersistentDataType.INTEGER, value);
  }
  
  /**
   * Sets the enchantment's level on the holder.
   * @param holder The persistent data holder.
   * @param value The value we're setting this to.
   */
  public void setLevel(PersistentDataHolder holder, int value) {
    setLevel(holder.getPersistentDataContainer(), value);
  }

  /**
   * Generates the string displayed on an item's lore
   *
   * @param value The target value.
   * @return A string representing the type.
   */
  @Override
  public String toLoreString(Object... values) {
    
    //Exit if no values.
    if (values.length == 0) { return null; }
    
    //Variables
    int value = (values.length >= 1 && values[0] != null ? (int) values[0] : 0);
    ChatColor color = (getAlignment() == RPGDataAlignment.NEGATIVE ? ChatColor.RED : ChatColor.GRAY); 
    
    //Return
    return color + getDisplayName() + (isLevelDisplayed() ? "" : " " + RomanNumeral.parse(value));
  }
  
}
