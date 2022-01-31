package com.gmail.alexdion93.aeonrpg.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;

import com.gmail.alexdion93.aeonrpg.util.RPGDataUtil;

/**
 * Ensures that arrows fired retain their RPG Data
 *
 * @author Alex Dion
 */
public class RPGBlockListener implements Listener {
  
  /**
   * Constructor
   *
   * @param plugin The source plugin
   */
  public RPGBlockListener() {
  }

  /**
   * Triggers when blocks are placed. Any rpg data on the item is copied
   * over to the block. 
   * @param event The event being triggered
   */
  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void onBlockPlace(BlockPlaceEvent event) {
    
    // Variables
    ItemStack item = event.getItemInHand();
    Block block = event.getBlock();
    
    // Ignore if no meta
    ItemMeta meta = item.getItemMeta();
    if (meta == null) { return; }
    PersistentDataContainer source = meta.getPersistentDataContainer();
    
    // Check if block is a persistent data holder
    BlockState state = block.getState();
    if (!(state instanceof PersistentDataHolder)) { return; }
    PersistentDataContainer target = ((PersistentDataHolder) state).getPersistentDataContainer();
    
    // Copy the data over
    RPGDataUtil.copyData(source, target);
  }
  
  /**
   * Triggers when blocks drops an item. Any rpg data on the item is copied
   * over from the block. 
   * @param event The event being triggered
   */
  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void onBlockDropItem(BlockDropItemEvent event) {
    
    // Skip if not persistent data holder
    Block block = event.getBlock();
    if (!(block.getState() instanceof PersistentDataHolder)) { return; }
    
    // Get the persistent data holder
    PersistentDataContainer source = ((PersistentDataHolder) block.getState()).getPersistentDataContainer();
    
    // Ignore if no items
    if (event.getItems().isEmpty()) { return; }
    
    // Loop the items dropped
    for(Item drop : event.getItems()) {
      // Fetch the itemstack
      ItemStack item = drop.getItemStack();
      
      //Skip if no meta
      ItemMeta meta = item.getItemMeta();
      if (meta == null) { continue; }
      PersistentDataContainer target = meta.getPersistentDataContainer();
      
      // Skip if mismatched types
      if (item.getType() != event.getBlock().getType()) { continue; }
      
      //Copy the data
      RPGDataUtil.copyData(source, target);
      meta.setLore(RPGDataUtil.generateLore(target));
      item.setItemMeta(meta);
    }
  }
  
  /**
   * Allows for creative move users to inspect a block by right clicking it.
   * @param event The event being fired.
   */
  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void onRightClickInspect(PlayerInteractEvent event) {
    
    // Ignore if not right clicking a block
    if (event.getAction() != Action.RIGHT_CLICK_BLOCK) { return; } 
    
    // Ignore off hand
    if (event.getHand() == EquipmentSlot.OFF_HAND) { return; }
    
    // Ignore if item isn't empty
    Player player = event.getPlayer();
    ItemStack item = player.getInventory().getItemInMainHand();
    if (item != null && item.getType() != Material.AIR) { return; }
    
    // Ignore if no block
    Block block = event.getClickedBlock();
    if (block == null) { return; }
    
    //Fetch the data
    if (!(block.getState() instanceof PersistentDataHolder)) { return; }
    PersistentDataHolder holder = (PersistentDataHolder) block.getState();
    PersistentDataContainer data = holder.getPersistentDataContainer();
    
    //Inspect
    RPGDataUtil.inspect(player, data);
  }
}
