package com.gmail.alexdion93.aeonrpg.data.holder;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;

import com.gmail.alexdion93.aeonrpg.AeonRPG;
import com.gmail.alexdion93.aeonrpg.data.interfaces.*;
import com.gmail.alexdion93.aeonrpg.data.type.*;
import com.gmail.alexdion93.aeonrpg.managers.RPGTypeManager;

public class RPGEntity {
  
  private AeonRPG plugin;
  private EntityType type;
  private HashMap<String, RPGAttributeData> attributes;
  private HashMap<String, RPGEnchantmentData> enchantments;
  private HashMap<String, RPGGenericData> generics;
  private HashMap<String, RPGPotionEffectData> potions;
  private HashMap<String, RPGSkillData> skills;
  private boolean dirty;
  
  /**
   * Load Constructor
   * @param section The configuration section we're loading from.
   */
  public RPGEntity(AeonRPG plugin, ConfigurationSection section) {
    this.plugin = plugin;
    attributes = new HashMap<>();
    enchantments = new HashMap<>();
    generics = new HashMap<>();
    potions = new HashMap<>();
    skills = new HashMap<>();
    dirty = false;
    load(plugin, section);
  }
  
  /**
   * Constructor
   * @param type The entity type.
   */
  public RPGEntity(AeonRPG plugin, EntityType type) {
    this.plugin = plugin;
    this.type = type;
    dirty = true;
  }
  
  /**
   * Spawns the entity at the location provided.
   * @param plugin The aeonrpg plugin
   * @param holder The persistent data holder we're applying this to.
   */
  public void apply(PersistentDataHolder holder) {
    //Variables
    RPGTypeManager manager = plugin.getRPGDataTypeManager();
    
    //Attributes
    if (attributes != null) for(String key: attributes.keySet()) {
      //Type Check
      RPGAttributeType type = manager.getAttributeManager().get(key);
      if (type == null) { continue; }
      
      //Apply Data
      RPGAttributeData data = attributes.get(key);
      type.setFlat(holder, data.getFlat());
      type.setScaling(holder, data.getScaling());
    }
    
    //Enchantments
    if (enchantments != null) for(String key: enchantments.keySet()) {
      //Type Check
      RPGEnchantmentType type = manager.getEnchantmentManager().get(key);
      if (type == null) { continue; }
      
      //Apply Data
      RPGEnchantmentData data = enchantments.get(key);
      type.setLevel(holder, data.getLevel());
    }
    
    //Generics
    if (generics != null) for(String key: generics.keySet()) {
      //Type Check
      RPGDataType type = manager.getGenericManager().get(key);
      if (type == null) { continue; }
      
      //Apply data
      RPGGenericData data = generics.get(key);
      if (type instanceof RPGDataOneValued) {
        RPGDataOneValued alt = (RPGDataOneValued) type;
        NamespacedKey altKey = alt.getPrimaryKey();
        int value = data.getPrimary();
        
        if (value == 0) { type.remove(holder, altKey, PersistentDataType.INTEGER, 0);}
        else { type.set(holder, altKey, PersistentDataType.INTEGER, value); }
      }
      
      if (type instanceof RPGDataTwoValued) {
        RPGDataTwoValued alt = (RPGDataTwoValued) type;
        NamespacedKey altKey = alt.getSecondaryKey();
        int value = data.getSecondary();
        
        if (value == 0) { type.remove(holder, altKey, PersistentDataType.INTEGER, 0);}
        else { type.set(holder, altKey, PersistentDataType.INTEGER, value); }
      }
      
      if (type instanceof RPGDataString) {
        RPGDataString alt = (RPGDataString) type;
        NamespacedKey altKey = alt.getStringKey();
        String value = data.getString();
        
        if (value == null || value.isEmpty()) { type.remove(holder, altKey, PersistentDataType.STRING, null);}
        else { type.set(holder, altKey, PersistentDataType.STRING, value); }
      }
      
    }
    
    //Potions
    if (potions != null) for(String key: potions.keySet()) {
      //Type Check
      RPGPotionEffectType type = manager.getPotionEffectManager().get(key);
      if (type == null) { continue; }
      
      //Apply Data
      RPGPotionEffectData data = potions.get(key);
      type.apply(holder, data.getLevel(), data.getDuration());
    }
    
    //Skills
    if (skills != null) for(String key: skills.keySet()) {
    //Type Check
      RPGSkillType type = manager.getSkillManager().get(key);
      if (type == null) { continue; }
      
      //Apply Data
      RPGSkillData data = skills.get(key);
      type.setLevel(holder, data.getLevel());
      type.setExperience(holder, data.getExperience());
    }
  }
  
  /**
   * Returns the data tied to provided type
   * @param type The type functioning as a key.
   * @return A data container or null.
   */
  public RPGAttributeData get(RPGAttributeType type) {
    return attributes.getOrDefault(type.getDataKey(), new RPGAttributeData(0,0));
  }
  
  /**
   * Returns the data tied to provided type
   * @param type The type functioning as a key.
   * @return A data container or null.
   */
  public RPGGenericData get(RPGDataType type) {
    return generics.getOrDefault(type.getDataKey(), new RPGGenericData(0,0, null));
  }
  
  /**
   * Returns the data tied to provided type
   * @param type The type functioning as a key.
   * @return A data container or null.
   */
  public RPGEnchantmentData get(RPGEnchantmentType type) {
    return enchantments.getOrDefault(type.getDataKey(), new RPGEnchantmentData(0));
  }
  
  /**
   * Returns the data tied to provided type
   * @param type The type functioning as a key.
   * @return A data container or null.
   */
  public RPGPotionEffectData get(RPGPotionEffectType type) {
    return potions.getOrDefault(type.getDataKey(), new RPGPotionEffectData(0, 0));
  }
  
  /**
   * Returns the data tied to provided type
   * @param type The type functioning as a key.
   * @return A data container or null.
   */
  public RPGSkillData get(RPGSkillType type) {
    return skills.getOrDefault(type.getDataKey(), new RPGSkillData(0, 0));
  }
  
  /**
   * Returns if this has been modified and thus needs to be saved.
   * @return True if this entity has been changed.
   */
  public boolean isDirty() { return dirty; }
  
  /**
   * Loads a configuration section
   * @param section The configuration section
   */
  public void load(AeonRPG plugin, ConfigurationSection section) {
    //Load Entity Data
    type = EntityType.UNKNOWN;
    try { type = EntityType.valueOf(section.getString("type", "UNKNOWN").toUpperCase()); }
    catch(Exception e) { /* Ignore */ }
    
    //Variables
    RPGTypeManager manager = plugin.getRPGDataTypeManager();
    
    //Attributes
    for(RPGAttributeType type : manager.getAttributeManager().getTypes()) {
      String key = type.getDataKey();
      int flat = section.getInt(key + ".flat", 0);
      int scaling = section.getInt(key + ".scaling", 0);
      
      if (flat == 0 && scaling == 0) { continue; }
      
      set(type, flat, scaling);
    }
    
    //Enchantments
    for(RPGEnchantmentType type : manager.getEnchantmentManager().getTypes()) {
      String key = type.getDataKey();
      
      int level = section.getInt(key + ".level", 0);
      if (level <= 0) { continue; }
      
      set(type, level);
    }
    
    //Generics
    for(RPGDataType type : manager.getGenericManager().getTypes()) {
      String key = type.getDataKey();
      int primary = section.getInt(key + ".primary", 0);
      int secondary = section.getInt(key + ".secondary", 0);
      String string = section.getString(key + ".string", null);
      
      set(type, primary, secondary, string);
    }
    
    //Potions
    for(RPGPotionEffectType type : manager.getPotionEffectManager().getTypes()) {
      String key = type.getDataKey();
      
      int level = section.getInt(key + ".level", 0);
      int duration = section.getInt(key + ".duration", 0);
      
      if (level <= 0) { continue; }
      if (!type.isInstant() && duration <= 0) { continue; }
      
      set(type, level, duration);
    }
    
    //Skills
    for(RPGSkillType type : manager.getSkillManager().getTypes()) {
      String key = type.getDataKey();
      
      int level = section.getInt(key + ".level", 0);
      int experience = section.getInt(key + ".experience", 0);
      
      if (level <= 0 && experience <= 0) { continue; }
      
      set(type, level, experience);
    }
  }
  
  /**
   * Saves to configuration section
   * @param section The configuration section
   */
  public void save(AeonRPG plugin, ConfigurationSection section) {
    
    dirty = false;
    
    //Entity Data
    section.set("type", type.name());
    
    //Attributes
    if (attributes != null) for(String key : attributes.keySet()) {
      RPGAttributeData data = attributes.get(key);
      
      if (data.getFlat() == 0) { section.set(key + ".flat", null); }
      else { section.set(key + ".flat", data.getFlat()); }
      
      if (data.getScaling() == 0) { section.set(key + ".scaling", null); }
      else { section.set(key + ".scaling", data.getScaling()); }
    }
    
    //Enchantments
    if (enchantments != null) for(String key : enchantments.keySet()) {
      RPGEnchantmentData data = enchantments.get(key);
      
      if (data.getLevel() <= 0) { section.set(key + ".level", null); }
      else { section.set(key + ".level", data.getLevel()); }
    }
    
    //Generics
    if (generics != null) for(String key : generics.keySet()) {
      RPGGenericData data = generics.get(key);
      
      int primary = data.getPrimary();
      int secondary = data.getSecondary();
      String string = data.getString();
      
      section.set(key + ".primary", primary == 0 ? null : primary );
      section.set(key + ".secondary", secondary == 0 ? null : secondary );
      section.set(key + ".string", string);
    }
    
    //Potions
    if (potions != null) for(String key : potions.keySet()) {
      RPGPotionEffectData data = potions.get(key);
      
      if (data.getLevel() <= 0) { section.set(key + ".level", null); }
      else { section.set(key + ".level", data.getLevel()); }
      
      if (data.getDuration() <= 0) { section.set(key + ".duration", null); }
      else { section.set(key + ".duration", data.getDuration()); }
    }
    
    //Skills
    if (skills != null) for(String key : skills.keySet()) {
      RPGSkillData data = skills.get(key);
      
      if (data.getLevel() <= 0) { section.set(key + ".level", null); }
      else { section.set(key + ".level", data.getLevel()); }
      
      if (data.getExperience() <= 0) { section.set(key + ".experience", null); }
      else { section.set(key + ".experience", data.getExperience()); }
    }
    
  }
  
  /**
   * Sets the rpg attribute and its associated data.
   * @param type The data type.
   * @param flat The flat value.
   * @param scaling The scaling value.
   */
  public void set(RPGAttributeType type, int flat, int scaling) {
    //Flag as dirty
    dirty = true;
    
    //Set the data
    String key = type.getDataKey();
    if (flat == 0 && scaling == 0) { attributes.remove(key); }
    else { attributes.put(key, new RPGAttributeData(flat, scaling)); }
  }
  
  /**
   * Sets the rpg generic and its associated data.
   * @param type The rpg data type
   * @param primary The primary data value
   * @param secondary The secondary data value
   * @param string The string data value.
   */
  public void set(RPGDataType type, int primary, int secondary, String string) {
    
    //Flag as dirty
    dirty = true;
    
    //Set the data
    String key = type.getDataKey();
    if (primary == 0 && secondary == 0 && string == null) { generics.remove(key); }
    else { generics.put(key, new RPGGenericData(primary, secondary, string)); }
  }
  
  /**
   * Sets the rpg enchantment and its associated data.
   * @param type The data type.
   * @param level The level value.
   */
  public void set(RPGEnchantmentType type, int level) {
    
    //Flag as dirty
    dirty = true;
    
    //Set the data
    String key = type.getDataKey();
    if (level <= 0) { enchantments.remove(key); }
    else { enchantments.put(key, new RPGEnchantmentData(level)); }
  }
  
  /**
   * Sets the rpg potion effect and its associated data.
   * @param type The data type.
   * @param level The level value.
   * @param duration The duration value.
   */
  public void set(RPGPotionEffectType type, int level, int duration) {
    
    //Flag as dirty
    dirty = true;
    
    //Set the data
    String key = type.getDataKey();
    if (level <= 0 && duration <= 0) { potions.remove(key); }
    else { potions.put(key, new RPGPotionEffectData(level, duration)); }
  }
  
  /**
   * Sets the rpg skill and its associated data.
   * @param type The data type.
   * @param level The level value.
   * @param experience The experience value.
   */
  public void set(RPGSkillType type, int level, int experience) {
    
    //Flag as dirty
    dirty = true;
    
    //Set the data
    String key = type.getDataKey();
    if (level <= 0 && experience <= 0) { skills.remove(key); }
    else { skills.put(key, new RPGSkillData(level, experience)); }
  }
  
  /**
   * Spawns the entity at the location provided.
   * @param plugin The aeonrpg plugin
   * @param location The location of the entity.
   */
  public void spawn(Location location) {
    //Spawn the entity
    Entity entity = location.getWorld().spawnEntity(location, type);
    
    //Apply the data
    apply(entity);
  }
  
  @Override
  public String toString() {
    String str = "";
    
    //Variables
    RPGTypeManager manager = plugin.getRPGDataTypeManager();
    
    //Attributes
    for(String key: attributes.keySet()) {
      //Type Check
      RPGAttributeType type = manager.getAttributeManager().get(key);
      if (type == null) { continue; }
    }
    
    //Enchantments
    for(String key: attributes.keySet()) {
      //Type Check
      RPGEnchantmentType type = manager.getEnchantmentManager().get(key);
      if (type == null) { continue; }
    }
    
    //Potions
    for(String key: attributes.keySet()) {
      //Type Check
      RPGPotionEffectType type = manager.getPotionEffectManager().get(key);
      if (type == null) { continue; }
    }
    
    //Skills
    for(String key: attributes.keySet()) {
    //Type Check
      RPGSkillType type = manager.getSkillManager().get(key);
      if (type == null) { continue; }
    }
    
    return str;
  }
  
  /**
   * Sends internal data to the command sender.
   * @param sender The command sender.
   */
  public void inspect(CommandSender sender) {
    // Attributes
    sender.sendMessage(ChatColor.GOLD + "  Attributes:");
    if (!attributes.isEmpty()) for(String key : attributes.keySet()) {
      RPGAttributeData data = attributes.get(key);
      sender.sendMessage("    " + key + ": " + data.getFlat() + " & " + data.getScaling() + "%");
    }
    
    // Enchantments
    sender.sendMessage(ChatColor.GOLD + "  Enchantments");
    if (!enchantments.isEmpty()) for(String key : enchantments.keySet()) {
      RPGEnchantmentData data = enchantments.get(key);
      sender.sendMessage("    " + key + ": Lv." + data.getLevel());
    }
    
    //Generics
    sender.sendMessage(ChatColor.GOLD + "  Generics:");
    if (!generics.isEmpty()) for(String key : generics.keySet()) {
      RPGGenericData data = generics.get(key);
      sender.sendMessage("    " + key + ": " + data.getPrimary() + " & " + data.getSecondary() + " & " + data.getString());
    }
    
    // Potions
    sender.sendMessage(ChatColor.GOLD + "  Potions:");
    if (!potions.isEmpty()) for(String key : potions.keySet()) {
      RPGPotionEffectData data = potions.get(key);
      sender.sendMessage("    " + key + ": Lv." + data.getLevel() + " & " + data.getDuration() + "s ");
    }
    
    // Skills
    sender.sendMessage(ChatColor.GOLD + "  Skills:");
    if (!skills.isEmpty()) for(String key : skills.keySet()) {
      RPGSkillData data = skills.get(key);
      sender.sendMessage("    " + key + ": Lv." + data.getLevel() + " & " + data.getExperience() + " exp");
    }
  }
}
