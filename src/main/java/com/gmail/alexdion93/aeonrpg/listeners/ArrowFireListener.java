package com.gmail.alexdion93.aeonrpg.listeners;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dispenser;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.AbstractArrow.PickupStatus;
import org.bukkit.entity.Arrow;
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
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;

import com.gmail.alexdion93.aeonrpg.AeonRPG;
import com.gmail.alexdion93.aeonrpg.util.RPGDataUtil;
import com.gmail.alexdion93.inventoryequipevent.util.ItemUtil;

/**
 * Ensures that arrows fired retain their RPG Data
 *
 * @author Alex Dion
 */
public class ArrowFireListener implements Listener {
  private AeonRPG plugin;
  
  /**
   * Constructor
   *
   * @param plugin The source plugin
   */
  public ArrowFireListener(AeonRPG plugin) {
    this.plugin = plugin;
  }

  /**
   * Triggers when an arrow is picked up
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
    RPGDataUtil.copyData(source, target);
    
    // Generate the lore
    meta.setLore(RPGDataUtil.generateLore(target));

    // Update the item's meta
    item.setItemMeta(meta);

    // Update the item
    event.getItem().setItemStack(item);
  }

  /**
   * Triggers when a dispenser is fired Replaces the normal arrow firing to apply
   * custom data to arrows.
   * TODO: Incomplete method
   *
   * @param event The event being fired
   */
  @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
  public void onDispenserFire(BlockDispenseEvent event) {

    // Exit if not arrows being fired
    ItemStack item = event.getItem();
    if (!ItemUtil.isCategory(item, ItemUtil.ARROWS)) { return; }
    
    // Directional Validation
    Block block = event.getBlock();
    if (!(block.getBlockData() instanceof Directional)) { return; }
    Directional directional = (Directional) block.getBlockData();
    
    //Cancel the normal event
    event.setCancelled(true);
    
    //Find the spawn location
    BlockFace face = directional.getFacing();
    Location loc = block.getRelative(face).getLocation().add(0.5, 0.5, 0.5);
    
    //Spawn the Arrow
    Arrow arrow = (Arrow) block.getWorld().spawnEntity(loc, EntityType.ARROW);
    arrow.setVelocity(event.getVelocity());
    arrow.setPickupStatus(PickupStatus.ALLOWED);
    
    //Remove the itemstack from the dispenser
    //This requires a 1 tick delay as the block state already
    //has been modified to remove the original item.
    new BukkitRunnable() {

      @Override
      public void run() {
        Dispenser disp = (Dispenser) block.getState();
        ItemStack[] contents = disp.getInventory().getStorageContents();
        for(int i = 0; i < contents.length; i++) {
          
          if (contents[i] == null) { continue; }
          if (!contents[i].isSimilar(item)) { continue; }
          
          contents[i].setAmount(contents[i].getAmount() - 1);
          
          if (contents[i].getAmount() <= 0) { contents[i] = null; }      
          break;
        }
        
        disp.getInventory().setContents(contents);
        block.getState().update();
      }
    }.runTask(plugin);
    
    //Copy data over to the arrow
    ItemMeta meta = item.getItemMeta();
    if (meta == null) { return; }
    
    
    RPGDataUtil.copyData(meta.getPersistentDataContainer(), arrow.getPersistentDataContainer());
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
    
    //Copy the data
    RPGDataUtil.copyData(source, target);
  }
}
