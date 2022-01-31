package com.gmail.alexdion93.aeonrpg.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dispenser;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
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
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import com.gmail.alexdion93.aeonrpg.AeonRPG;
import com.gmail.alexdion93.aeonrpg.util.RPGDataUtil;

/**
 * Ensures that arrows fired retain their RPG Data
 *
 * @author Alex Dion
 */
public class ArrowLaunchListener implements Listener {
  
  private AeonRPG plugin;
  
  /**
   * Constructor
   *
   * @param plugin The source plugin
   */
  public ArrowLaunchListener(AeonRPG plugin) {
    this.plugin = plugin;
  }

  /**
   * Returns if the item a viable target for the launch
   * @param item The target item.
   * @return True if its an arrow.
   */
  private boolean isViable(ItemStack item) {
    if (item == null) { return false; }
    switch(item.getType()) {
      case ARROW:
      case TIPPED_ARROW:
      case SPECTRAL_ARROW:
        return true;
      default:
        return false;
    }
  }

  /**
   * Triggers when an entity launches an arrow
   * Stores the data of the item launched
   *
   * @param event The event being fired.
   */
  @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
  public void onArrowLaunch(ProjectileLaunchEvent event) {

    // Variables
    Projectile projectile = event.getEntity();
    ProjectileSource shooter = projectile.getShooter();
    PersistentDataContainer target = projectile.getPersistentDataContainer();
    PersistentDataContainer source = null;
    
    // Ignore if not an abstract arrow or trident.
    if (!(projectile instanceof AbstractArrow)) { return; }
    if (projectile.getType() == EntityType.TRIDENT) { return; }
    
    // If its a player add custom damage.
    if (shooter instanceof Player) {
      
      // Variables
      Player player = (Player) shooter;
      PlayerInventory inventory = player.getInventory();
      ItemStack item = null;
      
      // If the main hand is a throwable or projectile use that.
      if (isViable(inventory.getItemInMainHand())) {
        item = inventory.getItemInMainHand();
      }
      
      // Otherwise check the off hand
      else if (isViable(inventory.getItemInOffHand())) {
        item = inventory.getItemInOffHand();
      }
      
      // Otherwise loop the inventory for the first viable item.
      // This only trigger for arrows
      else {
        for (ItemStack i : inventory.getContents()) {
          if (isViable(i)) {
            item = i;
            break;
          }
        }
      }

      // Failed to find item.
      if (item == null || !item.hasItemMeta()) { return; }

      // Set the source
      source = item.getItemMeta().getPersistentDataContainer();
    }
    
    // Mob Interaction
    else if (shooter instanceof Entity) {
      Entity entity = (Entity) shooter;
      source = entity.getPersistentDataContainer();
    }

    // Exit if no source found
    if (source == null) { return; }
    
    // Copy the data
    RPGDataUtil.copyData(source, target);
  }
  
  /**
   * Triggers when an arrow is picked up
   * @param event The event being triggered
   */
  @SuppressWarnings("deprecation")
  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
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
   * Triggers when a dispenser is fired
   * Negates the original arrow being fired.
   * Fires a replacement with data stored.
   *
   * @param event The event being fired
   */
  @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
  public void onDispenserFire(BlockDispenseEvent event) {

    // Exit if the item isn't viable
    ItemStack item = event.getItem();
    if (!isViable(item)) { return; }
    
    // Directional Validation
    Block block = event.getBlock();
    if (!(block.getBlockData() instanceof Directional)) { return; }
    Directional directional = (Directional) block.getBlockData();
    
    // Cancel the normal event
    event.setCancelled(true);
    
    // Find the spawn location
    BlockFace face = directional.getFacing();
    Location loc = block.getRelative(face).getLocation().add(0.5, 0.5, 0.5);
    
    // Spawn a specific projectile
    Projectile projectile = null;
    switch(item.getType()) {
      case ARROW:
        projectile = (Projectile) block.getWorld().spawnEntity(loc, EntityType.ARROW);
        break;
      case TIPPED_ARROW:
        projectile = (Projectile) block.getWorld().spawnEntity(loc, EntityType.ARROW);
        Arrow arrow = (Arrow) projectile;
        arrow.setBasePotionData(((PotionMeta) item.getItemMeta()).getBasePotionData());
        break;
      case SPECTRAL_ARROW:
        projectile = (Projectile) block.getWorld().spawnEntity(loc, EntityType.SPECTRAL_ARROW);
        break;
      default:
        return;
    }
    
    // Copy the velocity over to the projectile
    projectile.setVelocity(event.getVelocity());
    
    // Remove the itemstack from the dispenser
    // This requires a 1 tick delay as the block state already
    // has been modified to remove the original item.
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
    RPGDataUtil.copyData(meta.getPersistentDataContainer(), projectile.getPersistentDataContainer());
    
    // Trigger a custom projectile launch event
    ProjectileLaunchEvent e = new ProjectileLaunchEvent(projectile);
    Bukkit.getPluginManager().callEvent(e);
  }
}
