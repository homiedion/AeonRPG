package com.gmail.alexdion93.aeonrpg.managers;

import java.io.File;
import java.util.HashMap;
import java.util.Set;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import com.gmail.alexdion93.aeonrpg.AeonRPG;
import com.gmail.alexdion93.aeonrpg.data.holder.RPGSpawner;

/**
 * Manages collections of rpg spawner data.
 * @author Alex Dion
 */
public class RPGSpawnerManager implements Listener {

  private HashMap<String, RPGSpawner> spawners;
  private AeonRPG plugin;
  
  /**
   * Constructor
   * @param plugin The plugin we're loading from
   */
  public RPGSpawnerManager(AeonRPG plugin) {
    this.plugin = plugin;
    spawners = new HashMap<>();
  }
  
  /**
   * Adds an spawner to the manager's map.
   * @param key The key the spawner is being added under
   * @param spawner The spawner being added.
   */
  public void add(String key, RPGSpawner spawner) {
    spawners.put(key.toLowerCase(), spawner);
  }
  
  /**
   * Removes an spawner to the manager's map.
   * @param key The key the spawner is being added under
   * @return The spawner that was removed from the map
   */
  public RPGSpawner remove(String key) {
    return spawners.remove(key);
  }
  
  /**
   * Fetches an rpg spawner from the manager.
   * @param key The key we're looking for.
   * @return An rpg spawner or null if not present.
   */
  public RPGSpawner get(String key) {
    return spawners.get(key.toLowerCase());
  }
  
  /**
   * Returns if the key is registered.
   * @param key The key we're searching for.
   * @return True if the key is registered.
   */
  public boolean has(String key) {
    return spawners.containsKey(key.toLowerCase());
  }
  
  /**
   * Returns all the keys stored within.
   * @return The string keys with registered values.
   */
  public Set<String> getKeys() {
    return spawners.keySet();
  }
  
  /**
   * Saves all modified rpg entities.
   */
  public void save() {
    
    //Loop all keys
    for(String key : spawners.keySet()) {
      //Fetch the rpg spawner
      RPGSpawner spawner = spawners.get(key);
      if (spawner == null) { continue; }
      
      //Ignore if clean
      if (!spawner.isDirty()) { continue; }
      
      //Save
      try {
        File file = new File(plugin.getDataFolder().getAbsoluteFile() + "/Spawners/" + key + ".yml");
        YamlConfiguration config = new YamlConfiguration();
        spawner.save(plugin, config);
        config.save(file);
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}
