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
import com.gmail.alexdion93.aeonrpg.data.type.RPGSkillType;

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
  private GenericRPGTypeManager<RPGSkillType> skills;
  private GenericRPGTypeManager<RPGDataType> generics;

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
    managers.add(skills = new GenericRPGTypeManager<>(plugin));
    managers.add(generics = new GenericRPGTypeManager<>(plugin));
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
    return generics;
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
   * Returns the skill manager of the plugin.
   *
   * @return The skill manager of the plugin.
   */
  public GenericRPGTypeManager<RPGSkillType> getSkillManager() {
    return skills;
  }

  /**
   * Triggers when the server startup or reload has completed
   *
   * @param event The event being fired
   */
  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void onServerLoad(ServerLoadEvent event) {
    Logger log = plugin.getLogger();
    log.info("Loaded " + attributes.getKeys().size() + " rpg attributes.");
    log.info("Loaded " + enchantments.getKeys().size() + " rpg enchantments.");
    log.info("Loaded " + potioneffects.getKeys().size() + " rpg potion effects.");
    log.info("Loaded " + skills.getKeys().size() + " rpg skills.");
    log.info("Loaded " + generics.getKeys().size() + " rpg generics.");
  }
}
