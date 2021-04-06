package com.gmail.alexdion93.aeonrpg.managers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.bukkit.NamespacedKey;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.gmail.alexdion93.aeonrpg.data.type.RPGDataType;

/**
 * A container and manager of custom rpg data types
 * @author Alex Dion
 *
 */
public class GenericRPGTypeManager<T extends RPGDataType> {

  private List<String> sortedKeys;
  private final HashMap<String, T> types;
  private File file;
  private FileConfiguration config;

  /**
   * Constructor
   */
  @SuppressWarnings("serial")
  public GenericRPGTypeManager(JavaPlugin plugin) {
    types = new HashMap<>();
    sortedKeys = new ArrayList<String>() {
      @Override
      public boolean add(String mt) {
        super.add(mt);
        Collections.sort(sortedKeys);
        return true;
      }
    };

    // Create the config file
    String path = "config.yml";
    file = new File(plugin.getDataFolder(), path);
    if (!file.exists()) {
      file.getParentFile().mkdirs();
      try {
        file.createNewFile();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    config = new YamlConfiguration();
    try {
      config.load(file);
    } catch (IOException | InvalidConfigurationException e) {
      e.printStackTrace();
    }
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
   * @param key The target key the type is stored under.
   * @throws IllegalArgumentException If the type is already registered.
   * @throws NullPointerException     If the type provided is null.
   */
  public void put(final T type) {
    // Null Check
    if (type == null) {
      throw new NullPointerException("Null cannot be used for types");
    }

    // Duplicate Entry Check
    NamespacedKey nsk = type.getNamespacedKey();
    String key = nsk.getKey().toUpperCase();
    if (types.containsKey(key)) {
      throw new IllegalArgumentException("Type \"" + key + "\" already registered to this map!");
    }

    // Disabled Check
    if (config != null) {
      String path = nsk.getNamespace() + "." + key;

      // Create Default Value
      if (!config.contains(path)) {
        config.set(path, true);
        try {
          config.save(file);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }

      // Ignore if disabled via config
      if (!config.getBoolean(path, true)) {
        return;
      }
    }

    // Register the type
    types.put(key, type);

    // Add to the sorted keys
    sortedKeys.add(key);
  }
}
