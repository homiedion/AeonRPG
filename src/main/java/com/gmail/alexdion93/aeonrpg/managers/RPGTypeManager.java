package com.gmail.alexdion93.aeonrpg.managers;

import java.util.ArrayList;
import java.util.logging.Logger;

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
    init();
  }

  /*
   * Fetches an data type if possible
   *
   * @param The key we're searching for
   *
   * @return A data type or null
   */
  public RPGDataType get(String key) {
    if (key == null) {
      return null;
    }
    for (GenericRPGTypeManager<?> manager : managers) {
      if (!manager.has(key)) {
        continue;
      }
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
   * Initialize Variables
   */
  public void init() {
    managers = new ArrayList<>();
    // The order these are added in is the order they are displayed in.
    managers.add(attributes = new GenericRPGTypeManager<>(plugin));
    managers.add(enchantments = new GenericRPGTypeManager<>(plugin));
    managers.add(potioneffects = new GenericRPGTypeManager<>(plugin));
    managers.add(generic = new GenericRPGTypeManager<>(plugin));
  }

  /**
   * Triggers when the server startup or reload has completed
   *
   * @param event The event being fired
   */
  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void onServerLoad(ServerLoadEvent event) {
    Logger log = plugin.getLogger();

    log.info("Loaded " + attributes.getKeys().size() + " attributes.");
    log.info("Loaded " + enchantments.getKeys().size() + " enchantments.");
    log.info("Loaded " + generic.getKeys().size() + " generic data types");
    log.info("Loaded " + potioneffects.getKeys().size() + " potion effects.");
  }
}
