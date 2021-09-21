package com.gmail.alexdion93.aeonrpg.managers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

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
    return types.get(key.toLowerCase());
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
    return types.containsKey(key.toLowerCase());
  }

  /**
   * Adds an type to the map.
   *
   * @param type The type being added to the map
   * @return Whether or not the addition was successful
   */
  public boolean register(final T type) {
    Logger log = plugin.getLogger();
    
    // Null Check
    if (type == null) {
      log.severe("Failed to register due to type being null");
      return false;
    }
    
    //Variables
    
    
    // Success
    String key = type.getDataKey().toLowerCase();
    if (!has(key)) {
      types.put(key, type);
      sortedKeys.add(key);
      log.info("Registered " + key + " from " + type.getDisplayName() + ".");
    }
    else {
      log.warning("Key " + type.getDataKey() + " is already registered.");
    }
    
    return true;
  }
}
