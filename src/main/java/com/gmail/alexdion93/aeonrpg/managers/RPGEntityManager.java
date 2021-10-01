package com.gmail.alexdion93.aeonrpg.managers;

import java.io.File;
import java.util.HashMap;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;

import com.gmail.alexdion93.aeonrpg.AeonRPG;
import com.gmail.alexdion93.aeonrpg.data.holder.RPGEntity;
import com.gmail.alexdion93.aeonrpg.data.type.RPGAttributeType;
import com.gmail.alexdion93.aeonrpg.data.type.RPGEnchantmentType;
import com.gmail.alexdion93.aeonrpg.data.type.RPGPotionEffectType;
import com.gmail.alexdion93.aeonrpg.data.type.RPGSkillType;
import com.gmail.alexdion93.aeonrpg.data.type.RPGDataType.RPGDataAlignment;
import com.gmail.alexdion93.aeonrpg.util.RPGDataUtil;

/**
 * Manages collections of rpg entity data.
 * @author Alex Dion

 */
public class RPGEntityManager implements Listener {

  private HashMap<String, RPGEntity> entities;
  private AeonRPG plugin;
  
  /**
   * Constructor
   * @param plugin The plugin we're loading from
   */
  public RPGEntityManager(AeonRPG plugin) {
    this.plugin = plugin;
    entities = new HashMap<>();
  }
  
  /**
   * Adds an entity to the manager's map.
   * @param key The key the entity is being added under
   * @param entity The entity being added.
   */
  public void add(String key, RPGEntity entity) {
    entities.put(key.toLowerCase(), entity);
  }
  
  /**
   * Removes an entity to the manager's map.
   * @param key The key the entity is being added under
   * @return The entity that was removed from the map
   */
  public RPGEntity remove(String key) {
    return entities.remove(key);
  }
  
  /**
   * Fetches an rpg entity from the manager.
   * @param key The key we're looking for.
   * @return An rpg entity or null if not present.
   */
  public RPGEntity get(String key) {
    return entities.get(key.toLowerCase());
  }
  
  /**
   * Returns if the key is registered.
   * @param key The key we're searching for.
   * @return True if the key is registered.
   */
  public boolean has(String key) {
    return entities.containsKey(key.toLowerCase());
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
    inspect(player, data);
  }
  
  /**
   * Allows for creative move users to inspect an entity by right clicking it.
   * @param event The event being fired.
   */
  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void onRightClickInspect(PlayerInteractAtEntityEvent event) {
    
    // Ignore if player isn't in creative mode
    Player player = event.getPlayer();
    if (player.getGameMode() != GameMode.CREATIVE) { return; }
    
    //Ignore clicks from the off hand
    if (event.getHand() == EquipmentSlot.OFF_HAND) { return; }
    
    // Ensure the player's hand is empty
    ItemStack item = player.getInventory().getItemInMainHand();
    if (item != null && item.getType() != Material.AIR) { return; }
    
    // Fetch the entity and data
    Entity entity = event.getRightClicked();
    PersistentDataContainer data = entity.getPersistentDataContainer();
    
    //Inspect
    inspect(player, data);
  }
  
  /**
   * Inspects the data container and sends it to the player.
   * @param player The target player.
   * @param data The data being inspected.
   */
  public void inspect(Player player, PersistentDataContainer data) {
  //Send the information
    player.sendMessage(ChatColor.GOLD + "" + ChatColor.UNDERLINE + "        Inspection        ");
    
    //Attributes
    player.sendMessage(ChatColor.GOLD + "  Attributes:");
    for(RPGAttributeType type : plugin.getRPGDataTypeManager().getAttributeManager().getTypes()) {
      
      int flat = type.getFlat(data);
      int scaling = type.getScaling(data);
      if (flat == 0 && scaling == 0) { continue; }
      
      boolean isNegative = (type.getAlignment() == RPGDataAlignment.NEGATIVE);
      String positive = (isNegative ? ChatColor.RED : ChatColor.GREEN) + "+";
      String negative = (isNegative ? ChatColor.GREEN : ChatColor.RED) + "";
      
      player.sendMessage("    " + ChatColor.YELLOW + type.getDisplayName() + ": " + ChatColor.WHITE +
          (flat > 0 ? positive : negative) + flat + ChatColor.WHITE
          + " & " + (scaling > 0 ? positive : negative) + scaling + "%");
    }
    
    //Enchantments
    player.sendMessage(ChatColor.GOLD + "  Enchantments:");
    for(RPGEnchantmentType type : plugin.getRPGDataTypeManager().getEnchantmentManager().getTypes()) {
      
      int level = type.getLevel(data);
      
      if (level == 0) { continue; }
      
      boolean isNegative = (type.getAlignment() == RPGDataAlignment.NEGATIVE);
      String positive = (isNegative ? ChatColor.RED : ChatColor.GREEN) + "+";
      String negative = (isNegative ? ChatColor.GREEN : ChatColor.RED) + "";
      
      player.sendMessage("    " + ChatColor.YELLOW + type.getDisplayName() + ": " + ChatColor.WHITE +
          (level > 0 ? positive : negative) + "Lv." + level + ChatColor.WHITE);
    }
    
    //Potion Effects
    player.sendMessage(ChatColor.GOLD + "  Potion Effects:");
    for(RPGPotionEffectType type : plugin.getRPGDataTypeManager().getPotionEffectManager().getTypes()) {
      
      int level = type.getLevel(data);
      int duration = type.getDuration(data);
      
      if (level == 0 && duration == 0) { continue; }
      
      boolean isNegative = (type.getAlignment() == RPGDataAlignment.NEGATIVE);
      String positive = (isNegative ? ChatColor.RED : ChatColor.GREEN) + "+";
      String negative = (isNegative ? ChatColor.GREEN : ChatColor.RED) + "";
      
      player.sendMessage("    " + ChatColor.YELLOW + type.getDisplayName() + ": " + ChatColor.WHITE +
          (level > 0 ? positive : negative) + "Lv." + level + ChatColor.WHITE
          + " & " + (duration > 0 ? positive : negative) + duration + "s");
    }
    
    //Skills
    player.sendMessage(ChatColor.GOLD + "  Skills:");
    for(RPGSkillType type : plugin.getRPGDataTypeManager().getSkillManager().getTypes()) {
      
      int level = type.getLevel(data);
      int experience = type.getExperience(data);
      
      if (level == 0 && experience == 0) { continue; }
      
      boolean isNegative = (type.getAlignment() == RPGDataAlignment.NEGATIVE);
      String positive = (isNegative ? ChatColor.RED : ChatColor.GREEN) + "+";
      String negative = (isNegative ? ChatColor.GREEN : ChatColor.RED) + "";
      
      player.sendMessage("    " + ChatColor.YELLOW + type.getDisplayName() + ": " + ChatColor.WHITE +
          (level > 0 ? positive : negative) + "Lv." + level + ChatColor.WHITE
          + " & " + (experience > 0 ? positive : negative) + experience + "% Exp");
    }
  }
  
  /**
   * Triggers when the server startup or reload has completed.
   * Loads all stored rpg entities from the plugin's folder.
   * 
   * The reason data is loaded when the server finishes
   * loading is that rpg data types will not be registered prior
   * to the ServerLoadEvent.
   * 
   * @param event The event being fired
   */
  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void onServerLoad(ServerLoadEvent event) {
    
    //Clear the map
    entities.clear();
    
    //Create the directory if it doesn't exist.
    File directory = new File(plugin.getDataFolder().getAbsoluteFile() + "/Entities");
    if (!directory.exists()) {
      plugin.getLogger().warning("Failed to find the Entities directory!");
      return;
    }
    
    //Loop all files in the directory
    for(File file : directory.listFiles()) {
      
      //Skip non YAML files
      if (!file.getName().endsWith(".yml")) { continue; }
      String name = file.getName().replace(".yml", "");
      
      //Attempt to load the configuration as an RPGEntity
      YamlConfiguration config = new YamlConfiguration();
      try {
        config.load(file);
        add(name, new RPGEntity(plugin, config));
      }
      catch (Exception e) {
        plugin.getLogger().severe("Failed to load " + file.getName() + " due to " + e.getClass().getSimpleName());
        e.printStackTrace();
      }
    }
    
    //Inform the server how many entities were loaded
    plugin.getLogger().info("Loaded " + entities.size() + " rpg entities.");
  }
  
  /**
   * Apply transfer the rpg data from a spawn egg to the target.
   * @param event The event being fired.
   */
  @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
  public void onSpawnEggPlace(PlayerInteractEvent event) {
    
    //Exit if not a right click action
    if (event.getAction() != Action.RIGHT_CLICK_BLOCK) { return; }
    
    //Exit if not a spawn egg
    ItemStack item = event.getItem();
    if (item == null) { return; }
    if (!item.getType().name().contains("SPAWN_EGG")) { return; }
    
    //Cancel the normal spawn event
    event.setCancelled(true);
    
    //Find the target location
    Block block = event.getClickedBlock();
    Location location = block.getLocation();
    World world = block.getWorld();
    
    //Data Sources
    PersistentDataContainer source = item.getItemMeta().getPersistentDataContainer();
    PersistentDataContainer target = null;
    
    //Adjust location
    if (!block.isPassable()) { location = location.add(0.0, 1.0, 0.0); }
    location = location.add(0.5, 0.0, 0.5);
    
    //Fetch entity type
    EntityType type = EntityType.UNKNOWN;
    try {
      if (item.getType() == Material.MOOSHROOM_SPAWN_EGG) { type = EntityType.MUSHROOM_COW; }
      else { type = EntityType.valueOf(item.getType().name().replace("_SPAWN_EGG", "")); }
    }
    catch(Exception e) { /* Ignore */ }
    if (type == EntityType.UNKNOWN) { return; }
    
    //Spawner Logic
    if (block.getType() == Material.SPAWNER) {
      
      CreatureSpawner spawner = (CreatureSpawner) block.getState();
      target = spawner.getPersistentDataContainer();
      spawner.setSpawnedType(type);
      
      // Success
      RPGDataUtil.copyData(source, target);
      world.playEffect(location, Effect.MOBSPAWNER_FLAMES, 0);
      spawner.update();
    }
    
    //Spawn Creature Logic
    else {
      Entity entity = world.spawnEntity(location, type);
      target = entity.getPersistentDataContainer();
      
      //Exit if no source or target from
      if (source == null || target == null) { return; }
      
      // Success
      world.playEffect(location, Effect.MOBSPAWNER_FLAMES, 0);
      RPGDataUtil.copyData(source, target);
    }
  }
  
  /**
   * Transfers rpg data to entities spawned by a spawner.
   * @param event The event being fired.
   */
  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void onSpawnerActivate(SpawnerSpawnEvent event) {
    CreatureSpawner spawner = event.getSpawner();
    Entity entity = event.getEntity();
    
    PersistentDataContainer source = spawner.getPersistentDataContainer();
    PersistentDataContainer target = entity.getPersistentDataContainer();
    
    RPGDataUtil.copyData(source, target);
  }

  /**
   * Returns all the keys stored within.
   * @return The string keys with registered values.
   */
  public Set<String> getKeys() {
    return entities.keySet();
  }
  
  /**
   * Saves all modified rpg entities.
   */
  public void save() {
    
    //Loop all keys
    for(String key : entities.keySet()) {
      //Fetch the rpg entity
      RPGEntity entity = entities.get(key);
      if (entity == null) { continue; }
      
      //Ignore if clean
      if (!entity.isDirty()) { continue; }
      
      //Save
      try {
        File file = new File(plugin.getDataFolder().getAbsoluteFile() + "/Entities/" + key + ".yml");
        YamlConfiguration config = new YamlConfiguration();
        entity.save(plugin, config);
        config.save(file);
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}
