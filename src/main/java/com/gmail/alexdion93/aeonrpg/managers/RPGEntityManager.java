package com.gmail.alexdion93.aeonrpg.managers;

import java.io.File;
import java.util.HashMap;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;

import com.gmail.alexdion93.aeonrpg.AeonRPG;
import com.gmail.alexdion93.aeonrpg.data.holder.RPGEntity;

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
   * Fetches an rpg entity from the manager.
   * @param key The key we're looking for.
   * @return An rpg entity or null if not present.
   */
  public RPGEntity get(String key) {
    return entities.get(key);
  }
  
  /**
   * Returns if the key is registered.
   * @param key The key we're searching for.
   * @return True if the key is registered.
   */
  public boolean has(String key) {
    return entities.containsKey(key);
  }
  
  /**
   * Triggers when the server startup or reload has completed.
   * Loads all stored rpg entities from the plugin's folder.
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
      }
    }
    
    //Inform the server how many entities were loaded
    plugin.getLogger().info("Loaded " + entities.size() + " rpg entities.");
  }
}
