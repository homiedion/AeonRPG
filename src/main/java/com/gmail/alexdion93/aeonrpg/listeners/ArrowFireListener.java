package com.gmail.alexdion93.aeonrpg.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.projectiles.ProjectileSource;

import com.gmail.alexdion93.aeonrpg.data.interfaces.RPGDataString;
import com.gmail.alexdion93.aeonrpg.data.interfaces.RPGDataTwoValued;
import com.gmail.alexdion93.aeonrpg.data.interfaces.RPGDataValued;
import com.gmail.alexdion93.aeonrpg.data.type.RPGAttributeType;
import com.gmail.alexdion93.aeonrpg.data.type.RPGDataType;
import com.gmail.alexdion93.aeonrpg.data.type.RPGEnchantmentType;
import com.gmail.alexdion93.aeonrpg.data.type.RPGPotionEffectType;
import com.gmail.alexdion93.aeonrpg.managers.GenericRPGTypeManager;
import com.gmail.alexdion93.aeonrpg.managers.RPGTypeManager;
import com.gmail.alexdion93.aeonrpg.util.RPGDataUtil;
import com.gmail.alexdion93.inventoryequipevent.util.ItemUtil;

/**
 * Ensures that arrows fired retain their RPG Data
 *
 * @author Alex Dion
 */
public class ArrowFireListener implements Listener {

  private GenericRPGTypeManager<RPGAttributeType> am;
  private GenericRPGTypeManager<RPGEnchantmentType> em;
  private GenericRPGTypeManager<RPGDataType> gm;
  private GenericRPGTypeManager<RPGPotionEffectType> pm;

  /**
   * Constructor
   *
   * @param manager The potion effect manager
   */
  public ArrowFireListener(RPGTypeManager manager) {
    am = manager.getAttributeManager();
    em = manager.getEnchantmentManager();
    gm = manager.getGenericManager();
    pm = manager.getPotionEffectManager();
  }

  /**
   * Triggers when an arrow is picked up
   *
   * @param event The event being triggered
   */
  @SuppressWarnings("deprecation")
  @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
  public void onArrowPickup(PlayerPickupArrowEvent event) {

    // Fetch the item stack
    ItemStack item = event.getItem().getItemStack();
    ItemMeta meta = item.getItemMeta();
    if (meta == null) {
      return;
    }

    // Copy the data over to the new item.
    PersistentDataContainer source = event.getArrow().getPersistentDataContainer();
    PersistentDataContainer target = meta.getPersistentDataContainer();

    // Copy Attributes
    for (RPGAttributeType t : am.getTypes()) {
      PersistentDataType<Integer, Integer> type = PersistentDataType.INTEGER;
      NamespacedKey key = t.getNamespacedKey();

      if (source.has(key, type)) {
        target.set(key, type, source.get(key, type));
        t.modifyItem(meta, item.getType(), source.get(key, type));
      }
    }

    // Copy Enchantments
    for (RPGEnchantmentType t : em.getTypes()) {
      PersistentDataType<Integer, Integer> type = PersistentDataType.INTEGER;
      NamespacedKey key = t.getNamespacedKey();

      if (source.has(key, type)) {
        target.set(key, type, source.get(key, type));
        t.modifyItem(meta, item.getType(), source.get(key, type));
      }
    }

    // Copy Generics
    for (RPGDataType t : gm.getTypes()) {

      NamespacedKey key = t.getNamespacedKey();

      if (t instanceof RPGDataTwoValued) {
        PersistentDataType<Integer, Integer> type = PersistentDataType.INTEGER;
        NamespacedKey alt = ((RPGDataTwoValued) t).getAltNamespacedKey();

        if (source.has(key, type) || source.has(alt, type)) {
          int val = source.getOrDefault(key, type, 0);
          int oth = source.getOrDefault(alt, type, 0);
          target.set(key, type, val);
          target.set(alt, type, oth);
          ((RPGDataTwoValued) t).modifyItem(meta, item.getType(), val, oth);
        }
      } else if (t instanceof RPGDataValued) {
        PersistentDataType<Integer, Integer> type = PersistentDataType.INTEGER;
        if (source.has(key, type)) {
          int val = source.getOrDefault(key, type, 0);
          target.set(key, type, val);
          ((RPGDataValued) t).modifyItem(meta, item.getType(), val);
        }
      } else if (t instanceof RPGDataString) {
        PersistentDataType<String, String> type = PersistentDataType.STRING;
        if (source.has(key, type)) {
          String val = source.getOrDefault(key, type, null);
          target.set(key, type, val);
          ((RPGDataString) t).modifyItem(meta, item.getType(), val);
        }
      }
    }

    // Copy Potion Effects
    for (RPGPotionEffectType t : pm.getTypes()) {
      PersistentDataType<Integer, Integer> type = PersistentDataType.INTEGER;
      NamespacedKey key = t.getNamespacedKey();
      NamespacedKey alt = t.getAltNamespacedKey();

      if (source.has(key, type) || source.has(alt, type)) {
        int val = source.getOrDefault(key, type, 0);
        int oth = source.getOrDefault(alt, type, 0);
        target.set(key, type, val);
        target.set(alt, type, oth);
        t.modifyItem(meta, item.getType(), val, oth);
      }
    }

    // Generate the lore
    meta.setLore(RPGDataUtil.generateLore(source));

    // Update the item's meta
    item.setItemMeta(meta);

    // Update the item
    event.getItem().setItemStack(item);
  }

  /**
   * Triggers when a dispenser is fired Replaces the normal arrow firing to apply
   * custom data to arrows.
   *
   * @param event The event being fired
   */
  @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
  public void onDispenserFire(BlockDispenseEvent event) {

    // Fetch the item being fired
    ItemStack item = event.getItem();

    // Exit if not arrows being fired
    if (!ItemUtil.isCategory(item, ItemUtil.ARROWS)) {
      return;
    }

    Bukkit.broadcastMessage("Arrowed fire with " + event.getVelocity());
    event.setCancelled(true);

    // Immitate the normal dispenser firing
    Block block = event.getBlock();

    if (!(block.getBlockData() instanceof Directional)) {
      return;
    }
    Directional directional = (Directional) block.getBlockData();

    BlockFace face = directional.getFacing();
    Bukkit.broadcastMessage(" Face:" + face);

    Location loc = block.getLocation();
    Bukkit.broadcastMessage("" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());

  }

  /**
   * Triggers when an entity launches a projectile Stores the data of the item
   * fired
   *
   * @param event The event being fired.
   */
  @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
  public void onProjectileLaunch(ProjectileLaunchEvent event) {

    // Fetch the projectile and make sure its an arrow
    Projectile projectile = event.getEntity();
    if (projectile.getType() == EntityType.TRIDENT) {
      return;
    }
    if (!(projectile instanceof AbstractArrow)) {
      return;
    }
    ProjectileSource shooter = projectile.getShooter();

    // Copy the data over to the new item.
    PersistentDataContainer target = projectile.getPersistentDataContainer();
    PersistentDataContainer source = null;

    // Player Projectile Source
    if (shooter instanceof Player) {
      Player player = (Player) shooter;
      PlayerInventory inventory = player.getInventory();
      ItemStack item = null;

      // Check the main hand
      if (ItemUtil.isCategory(inventory.getItemInMainHand(), ItemUtil.ARROWS)) {
        item = inventory.getItemInMainHand();
      }
      // Check the off hand
      else if (ItemUtil.isCategory(inventory.getItemInOffHand(), ItemUtil.ARROWS)) {
        item = inventory.getItemInOffHand();
      }

      // Check the rest of the inventory
      else {
        for (ItemStack i : inventory.getContents()) {
          if (ItemUtil.isCategory(i, ItemUtil.ARROWS)) {
            item = i;
            break;
          }
        }
      }

      // Failed to find
      if ((item == null) || (item.getItemMeta() == null)) {
        return;
      }

      // Set the source
      source = item.getItemMeta().getPersistentDataContainer();
    }

    // No source found
    if (source == null) {
      return;
    }

    // Copy Attributes
    for (RPGAttributeType t : am.getTypes()) {
      PersistentDataType<Integer, Integer> type = PersistentDataType.INTEGER;
      NamespacedKey key = t.getNamespacedKey();

      if (!source.has(key, type)) {
        continue;
      }
      target.set(key, type, source.get(key, type));
    }

    // Copy Enchantments
    for (RPGEnchantmentType t : em.getTypes()) {
      PersistentDataType<Integer, Integer> type = PersistentDataType.INTEGER;
      NamespacedKey key = t.getNamespacedKey();

      if (source.has(key, type)) {
        target.set(key, type, source.get(key, type));
      }
    }

    // Copy Generics
    for (RPGDataType t : gm.getTypes()) {

      NamespacedKey key = t.getNamespacedKey();

      if (t instanceof RPGDataTwoValued) {
        PersistentDataType<Integer, Integer> type = PersistentDataType.INTEGER;
        NamespacedKey alt = ((RPGDataTwoValued) t).getAltNamespacedKey();

        if (source.has(key, type)) {
          target.set(key, type, source.get(key, type));
        }
        if (source.has(alt, type)) {
          target.set(alt, type, source.get(alt, type));
        }
      } else if (t instanceof RPGDataValued) {
        PersistentDataType<Integer, Integer> type = PersistentDataType.INTEGER;
        if (source.has(key, type)) {
          target.set(key, type, source.get(key, type));
        }
      } else if (t instanceof RPGDataString) {
        PersistentDataType<String, String> type = PersistentDataType.STRING;
        if (source.has(key, type)) {
          target.set(key, type, source.get(key, type));
        }
      }
    }

    // Copy Potion Effects
    for (RPGPotionEffectType t : pm.getTypes()) {
      PersistentDataType<Integer, Integer> type = PersistentDataType.INTEGER;
      NamespacedKey key = t.getNamespacedKey();
      NamespacedKey alt = t.getAltNamespacedKey();

      if (source.has(key, type)) {
        target.set(key, type, source.get(key, type));
      }
      if (source.has(alt, type)) {
        target.set(alt, type, source.get(alt, type));
      }
    }
  }
}
