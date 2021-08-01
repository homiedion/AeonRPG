package com.gmail.alexdion93.aeonrpg.data.type;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import com.gmail.alexdion93.aeonrpg.data.enums.RPGDataAlignment;
import com.gmail.alexdion93.aeonrpg.data.interfaces.RPGDataTwoValued;
import com.gmail.alexdion93.aeonrpg.data.interfaces.RPGDataValued;

public abstract class RPGAttributeType extends RPGDataType implements RPGDataValued, Listener {

  /**
   * Constructor
   *
   * @param plugin      The target plugin.
   * @param key         The key of this type.
   * @param displayName The display name of this type.
   * @param description The type's description
   */
  public RPGAttributeType(final JavaPlugin plugin, final String key, final String displayName,
      final String description) {
    super(plugin, "attribute." + key, displayName, description);
  }

  @Override
  public RPGDataAlignment getAlignment() {
    return RPGDataAlignment.POSITIVE;
  }

  @Override
  public int getDefaultValue() {
    return 0;
  }

  /**
   * Allows negative values
   */
  @Override
  public boolean isNegativeAllowed() {
    return true;
  }

  /**
   * Modifies the item this is placed on
   */
  @Override
  public void modifyItem(ItemMeta meta, Material material, int value) {
    meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
  }

  /**
   * Allows the player to have an innate attribute value
   *
   * @param event The event being fired.
   */
  @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
  public void onPlayerJoin(PlayerJoinEvent event) {
    Player player = event.getPlayer();

    // Two data values
    if (this instanceof RPGDataTwoValued) {
      RPGDataTwoValued alt = ((RPGDataTwoValued) this);
      alt.modifyEntityDefault(player, getDefaultValue(), alt.getDefaultAltValue());
    }

    // Single data values
    else {
      modifyTargetDefault(player, getDefaultValue());
    }
  }

  /**
   * Generates the string displayed on an item's lore
   *
   * @param value The target value.
   * @return A string representing the type.
   */
  @Override
  public String toLoreString(final int value) {
    // Negative Attribute
    if (getAlignment() == RPGDataAlignment.NEGATIVE) {
      return (value > 0 ? ChatColor.RED + "+" : ChatColor.GREEN + "") + value + ChatColor.GRAY + " " + getDisplayName();
    }

    // Positive Attribute
    else {
      return (value > 0 ? ChatColor.GREEN + "+" : ChatColor.RED + "") + value + ChatColor.GRAY + " " + getDisplayName();
    }
  }
}
