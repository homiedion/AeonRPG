package com.gmail.alexdion93.aeonrpg.managers;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;

import com.gmail.alexdion93.aeonrpg.AeonRPG;
import com.gmail.alexdion93.aeonrpg.data.type.RPGAttributeType;
import com.gmail.alexdion93.aeonrpg.data.type.RPGDataType;
import com.gmail.alexdion93.aeonrpg.data.type.RPGEnchantmentType;
import com.gmail.alexdion93.aeonrpg.data.type.RPGPotionEffectType;

/**
 * A container for multiple defined rpg data types
 * TODO: Remove init methods from places that don't need it. We can do in the constructor
 * @author Alex Dion
 *
 */
public class RPGTypeManager implements Listener {

  private GenericRPGTypeManager<RPGAttributeType> attributes;
  private GenericRPGTypeManager<RPGEnchantmentType> enchantments;
  private ArrayList<GenericRPGTypeManager<?>> managers;
  private final AeonRPG plugin;
  private GenericRPGTypeManager<RPGPotionEffectType> potioneffects;
  private GenericRPGTypeManager<RPGDataType> generic;

  /**
   * Constructor
   *
   * @param plugin the plugin this is tied to
   */
  public RPGTypeManager(final AeonRPG plugin) {
    this.plugin = plugin;
    
    managers = new ArrayList<>();
    managers.add(attributes = new GenericRPGTypeManager<>(plugin));
    managers.add(enchantments = new GenericRPGTypeManager<>(plugin));
    managers.add(potioneffects = new GenericRPGTypeManager<>(plugin));
    managers.add(generic = new GenericRPGTypeManager<>(plugin));
  }

  /*
   * Fetches an data type if possible
   *
   * @param The key we're searching for
   * @return A data type or null
   */
  public RPGDataType get(String key) {
    if (key == null) { return null; }
    
    for (GenericRPGTypeManager<?> manager : managers) {
      if (!manager.has(key)) { continue; }
      return manager.get(key);
    }
    
    return null;
  }

  /**
   * Returns the attribute manager of the plugin.
   *
   * @return The attribute manager of the plugin.
   */
  public GenericRPGTypeManager<RPGAttributeType> getAttributeManager() {
    return attributes;
  }

  /**
   * Returns the enchantment manager of the plugin.
   *
   * @return The enchantment manager of the plugin.
   */
  public GenericRPGTypeManager<RPGEnchantmentType> getEnchantmentManager() {
    return enchantments;
  }

  /**
   * Returns the generic manager of the plugin.
   *
   * @return The generic manager of the plugin.
   */
  public GenericRPGTypeManager<RPGDataType> getGenericManager() {
    return generic;
  }

  /**
   * Returns a collection of managers present.
   *
   * @return The managers registered.
   */
  public ArrayList<GenericRPGTypeManager<?>> getManagers() {
    return managers;
  }

  /**
   * Returns the potion effect manager of the plugin.
   *
   * @return The potion effect manager of the plugin.
   */
  public GenericRPGTypeManager<RPGPotionEffectType> getPotionEffectManager() {
    return potioneffects;
  }

  /**
   * Triggers when the server startup or reload has completed
   *
   * @param event The event being fired
   */
  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void onServerLoad(ServerLoadEvent event) {
    Logger log = plugin.getLogger();

    log.info("Type Manager");
    log.info("  Attributes: " + attributes.getKeys().size());
    log.info("  Enchantments: " + enchantments.getKeys().size());
    log.info("  Potion Effects: " + potioneffects.getKeys().size());
    log.info("  Generics: " + generic.getKeys().size());
  }
  
  /**
   * Saves the data on all rpg types
   */
  public void save() {
    
    YamlConfiguration config = plugin.openPluginFile("config.yml");
    Logger log = plugin.getLogger();
    
    if (config == null) {
      log.severe("Failed to save. Could not open config.yml");
      return;
    }
    
    for(GenericRPGTypeManager<?> manager : managers) {
      manager.save(config);
    }
    
    plugin.savePluginFile(config, "config.yml");
  }
}
