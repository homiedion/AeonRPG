package com.gmail.alexdion93.aeonrpg.managers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;

import com.gmail.alexdion93.aeonrpg.AeonRPG;
import com.gmail.alexdion93.aeonrpg.data.type.RPGDataType;

/**
 * A container and manager of custom rpg data types
 * @author Alex Dion
 *
 */
public class GenericRPGTypeManager<T extends RPGDataType> {

  private List<String> sortedKeys;
  private final HashMap<String, T> types;
  private AeonRPG plugin;

  /**
   * Constructor
   * @param plugin The AeonRPG plugin
   */
  @SuppressWarnings("serial")
  public GenericRPGTypeManager(AeonRPG plugin) {
    this.plugin = plugin;
    types = new HashMap<>();
    
    //TODO: Consider changing this, it might be inefficient
    sortedKeys = new ArrayList<String>() {
      @Override
      public boolean add(String mt) {
        super.add(mt);
        Collections.sort(sortedKeys);
        return true;
      }
    };
  }


  /**
   * Fetches an type from the map.
   *
   * @param key The target key the type is stored under.
   * @return A registered type or null.
   */
  public T get(final String key) {
    return types.get(key.toUpperCase());
  }

  /**
   * Returns the sorted keys within the type map.
   *
   * @return The sorted keys within the type map.
   */
  public List<String> getKeys() {
    return sortedKeys;
  }

  /**
   * Returns all types associated with this manager.
   *
   * @return all types associated with this manager.
   */
  public Collection<T> getTypes() {
    return types.values();
  }

  /**
   * Fetches an type from the map.
   *
   * @param key The target key the type is stored under.
   * @return True if the key is present
   */
  public boolean has(final String key) {
    return types.containsKey(key.toUpperCase());
  }

  /**
   * Adds an type to the map.
   *
   * @param type The type being added to the map
   * @return Whether or not the addition was successful
   */
  public boolean put(final T type) {
    Logger log = plugin.getLogger();
    
    // Null Check
    if (type == null) {
      log.severe("Failed to register due to type being null");
      return false;
    }
    
    //Variables
    NamespacedKey nsk = type.getNamespacedKey();
    String key = type.getNamespacedKey().getKey().toUpperCase();
    
    //Check if the type is disabled in the configuration
    YamlConfiguration config = plugin.openPluginFile("config.yml");
    if (config != null) {
      if (!config.getBoolean("data." + nsk.getNamespace() + "." + key, true)) {
        log.info("Failed to register " + key + " due to being disabled in config.yml");
        return false;
      }
    }

    // Duplicate Entry Check
    if (types.containsKey(key)) {
      log.warning("Failed to register " + key + " as its namespace is already in use");
      return false;
    }
    
    // Add to the appropriate players
    types.put(key, type);
    sortedKeys.add(key);
    log.info("Successfully registered " + key + "");
    return true;
  }
  
  /**
   * Saves all data to the config file
   * @param config The configuration file
   */
  public void save(YamlConfiguration config) {
    for(String key : sortedKeys) {
      
      //Variables
      T type = types.get(key);
      NamespacedKey nsk = type.getNamespacedKey();
      //String key = type.getNamespacedKey().getKey().toUpperCase();
      String path = "data." + nsk.getNamespace() + "." + key;
      
      if (config.contains(path)) { continue; }
      config.set(path, true);
    }
  }
}
