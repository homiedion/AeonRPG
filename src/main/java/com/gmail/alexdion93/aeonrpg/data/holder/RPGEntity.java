package com.gmail.alexdion93.aeonrpg.data.holder;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import com.gmail.alexdion93.aeonrpg.AeonRPG;
import com.gmail.alexdion93.aeonrpg.data.type.RPGPotionEffectType;
import com.gmail.alexdion93.aeonrpg.util.RPGDataUtil;

public class RPGEntity {
  
  private ItemStack item;
  private EntityType type;
  private HashMap<RPGPotionEffectType, RPGPotionEffectData> potionEffects;
  
  /**
   * Constructor
   * @param type The entity type.
   */
  public RPGEntity(EntityType type) {
    this.type = type;
    potionEffects = new HashMap<>();
    initializeItemStack();
  }
  
  /**
   * Load Constructor
   * @param section The configuration section we're loading from.
   */
  public RPGEntity(AeonRPG plugin, ConfigurationSection section) {
    load(plugin, section);
  }
  
  /**
   * Loads a configuration section
   * @param section The configuration section
   */
  public void load(AeonRPG plugin, ConfigurationSection section) {
    //Load Entity Data
    type = EntityType.UNKNOWN;
    try { type = EntityType.valueOf(section.getString("type", "UNKNOWN").toUpperCase()); }
    catch(Exception e) { /* Ignore */ }
    
    //Load rpg data
    initializeItemStack();
    if (section.isConfigurationSection("data")) {
      ItemMeta meta = item.getItemMeta();
      RPGDataUtil.loadRPGData(section.getConfigurationSection("data"), meta.getPersistentDataContainer());
      item.setItemMeta(meta);
    }
  }
  
  /**
   * Saves to configuration section
   * @param section The configuration section
   */
  public void save(AeonRPG plugin, ConfigurationSection section) {
    //Save Entity Type
    section.set("Type", type.name());
    
    //Save rpg data
    PersistentDataContainer data = item.getItemMeta().getPersistentDataContainer();
    if (!section.isConfigurationSection("data")) { section.createSection("data"); }
    RPGDataUtil.saveRPGData(section.getConfigurationSection("data"), data);
  }
  
  /**
   * Adds an rpg potion effect to this container.
   * @param type The type being added.
   * @param level The level being added.
   * @param duration The duration being added.
   */
  public void addPotionEffect(RPGPotionEffectType type, int level, int duration) {
    potionEffects.put(type, new RPGPotionEffectData(level, duration));
  }
  
  /**
   * Removes an rpg potion effect from this container.
   * @param type The type being removed.
   */
  public void removePotionEffect(RPGPotionEffectType type) {
    potionEffects.remove(type);
  }
  
  /**
   * Initializers the item stack.
   */
  private void initializeItemStack() {
    
    //Material Check
    Material material = Material.matchMaterial(type + "SPAWN_EGG");
    if (material == null) { material = Material.EGG; }
    
    //Initialize ItemStack
    item = new ItemStack(material);
  }
  
  /**
   * Returns the itemstack that represents this entity.
   * @return The itemstack that represents this entity.
   */
  public ItemStack getItemStack() {
    return item;
  }
  
  /**
   * Spawns the entity with the corresponding rpg data.
   * @param location The location the entity is being spawned at.
   * @return The entity being spawned.
   */
  public Entity spawn(Location location) {
    //Variables
    Entity entity = location.getWorld().spawnEntity(location, type, false);
    PersistentDataContainer target = entity.getPersistentDataContainer();
    
    //Copy Data
    if (item != null && item.getItemMeta() != null) {
      PersistentDataContainer source = item.getItemMeta().getPersistentDataContainer();
      RPGDataUtil.copyData(source, target);
    }
    
    //Apply potion effects
    for(RPGPotionEffectType type : potionEffects.keySet()) {
      
      int level = potionEffects.get(type).getLevel();
      int duration = potionEffects.get(type).getLevel();
      type.apply(entity, level, duration);
    }
    
    //Return
    return entity;
  }
}
