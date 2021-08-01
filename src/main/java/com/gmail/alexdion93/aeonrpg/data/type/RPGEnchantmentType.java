package com.gmail.alexdion93.aeonrpg.data.type;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import com.gmail.alexdion93.aeonrpg.data.enums.RPGDataAlignment;
import com.gmail.alexdion93.aeonrpg.data.interfaces.RPGDataLevelIndependent;
import com.gmail.alexdion93.aeonrpg.data.interfaces.RPGDataValued;
import com.gmail.alexdion93.aeonrpg.util.RomanNumeral;

public abstract class RPGEnchantmentType extends RPGDataType implements RPGDataLevelIndependent, RPGDataValued {

  /**
   * Constructor
   *
   * @param plugin      The targe tplugin.
   * @param key         The key of this type.
   * @param displayName The display name of this type.
   * @param description This type's description
   */
  public RPGEnchantmentType(final JavaPlugin plugin, final String key, final String displayName,
      final String description) {
    super(plugin, "enchantment." + key, displayName, description);
  }

  /**
   * Returns the default value of this type.
   *
   * @return The default value of this type.
   */
  @Override
  public int getDefaultValue() {
    return 0;
  }

  @Override
  public boolean isNegativeAllowed() {
    return false;
  }

  /**
   * Modifies the item this is placed on
   */
  @Override
  public void modifyItem(ItemMeta meta, Material material, int value) {
    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
  }

  /**
   * Generates the string displayed on an item's lore
   *
   * @param value The target value.
   * @return A string representing the type.
   */
  @Override
  public String toLoreString(final int value) {
    return (getAlignment() == RPGDataAlignment.NEGATIVE ? ChatColor.RED : ChatColor.GRAY) + getDisplayName()
        + (isLevelIndependent() ? "" : " " + RomanNumeral.parse(value));
  }
}
