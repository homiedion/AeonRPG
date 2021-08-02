package com.gmail.alexdion93.aeonrpg;

import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.gmail.alexdion93.aeonrpg.data.interfaces.RPGRecipeModifier;
import com.gmail.alexdion93.aeonrpg.data.type.RPGAttributeType;
import com.gmail.alexdion93.aeonrpg.data.type.RPGDataType;
import com.gmail.alexdion93.aeonrpg.data.type.RPGEnchantmentType;
import com.gmail.alexdion93.aeonrpg.data.type.RPGPotionEffectType;
import com.gmail.alexdion93.aeonrpg.managers.GenericRPGTypeManager;

/**
 * An abstract class created to hasten the creation of new plugins
 * tied to AeonRPG. These plugins will be referred to as "Modules" 
 * for the sake of having terminology that specifically points to
 * them.
 * 
 * @author Alex Dion
 */
public abstract class RPGModule extends JavaPlugin {

  /**
   * Class Variables
   */
  protected AeonRPG aeonrpg;
  
  /*
   * Module Disable
   */
  @Override
  public void onDisable() {
    unregister();
    unschedule();
    save();
  }

  /*
   * Module Enable
   */
  @Override
  public void onEnable() {
    init();
    load();
    register();
    schedule();
  }
  
  /**
   * Initializes the module and grabs an
   * instance of AeonRPG for later use.
   */
  protected void init() {
    
    PluginManager pm = Bukkit.getPluginManager();
    Logger log = getLogger();
    
    aeonrpg = (AeonRPG) pm.getPlugin("AeonRPG");
    
    //Trigger if we can't get an instance of AeonRPG
    if (aeonrpg == null) {
      log.severe("Failed to fetch an instance of AeonRPG.");
      log.severe("Please ensure that:");
      log.severe(" • The plugin is installed");
      log.severe(" • This module has AeonRPG as a dependancy");
      log.severe(" • This module is loading after AeonRPG");
      
      pm.disablePlugin(this);
      return;
    }
  }
  
  /**
   * Loads data into the module
   */
  protected void load() {}
  
  /**
   * Registers data with AeonRPG
   */
  protected void register() {}
  
  /**
   * Attempts to register one or more RPGAttributes to the appropriate manager
   * and performs certain operations based on the interfaces included:
   * • If the type is a listener it will automatically register it to this module
   * • If the type modifies a vanilla recipe it will be applied to them
   * @param types One or more rpg attributes
   */
  protected void registerAttributes(RPGAttributeType... types) {
    GenericRPGTypeManager<RPGAttributeType> manager = aeonrpg.getRPGDataTypeManager().getAttributeManager();
    for(RPGAttributeType type : types) { registerData(manager, type); }
  }
  
  /**
   * Attempts to register one or more RPGAttributes to the appropriate manager
   * and performs certain operations based on the interfaces included:
   * • If the type is a listener it will automatically register it to this module
   * • If the type modifies a vanilla recipe it will be applied to them
   * @param types A collection of attributes
   */
  protected void registerAttributes(List<RPGAttributeType> types) {
    GenericRPGTypeManager<RPGAttributeType> manager = aeonrpg.getRPGDataTypeManager().getAttributeManager();
    for(RPGAttributeType type : types) { registerData(manager, type); }
  }
  
  /**
   * Registers a single type of data to the appropriate manager type
   * @param manager The manager we're registering to.
   * @param type The being registered.
   */
  private <T extends RPGDataType> void registerData(GenericRPGTypeManager<T> manager, T type) {
    try {
      manager.put(type);
      if (type instanceof Listener) { Bukkit.getPluginManager().registerEvents((Listener) type, this); }
      if (type instanceof RPGRecipeModifier) { ((RPGRecipeModifier) type).modifyRecipes(); }
    }
    catch (Exception e) {
      getLogger().warning(String.format("Failed to register %s. %s", type.getDisplayName(), e.toString()));
    }
    
  }
  
  /**
   * Attempts to register one or more RPGEnchantmentTypes to the appropriate manager
   * and performs certain operations based on the interfaces included:
   * • If the type is a listener it will automatically register it to this module
   * • If the type modifies a vanilla recipe it will be applied to them
   * @param types One or more rpg data types
   */
  protected void registerEnchantments(RPGEnchantmentType... types) {
    GenericRPGTypeManager<RPGEnchantmentType> manager = aeonrpg.getRPGDataTypeManager().getEnchantmentManager();
    for(RPGEnchantmentType type : types) {registerData(manager, type);}
  }
  
  /**
   * Attempts to register one or more RPGEnchantmentTypes to the appropriate manager
   * and performs certain operations based on the interfaces included:
   * • If the type is a listener it will automatically register it to this module
   * • If the type modifies a vanilla recipe it will be applied to them
   * @param types A collection of enchantments
   */
  protected void registerEnchantments(List<RPGEnchantmentType> types) {
    GenericRPGTypeManager<RPGEnchantmentType> manager = aeonrpg.getRPGDataTypeManager().getEnchantmentManager();
    for(RPGEnchantmentType type : types) {registerData(manager, type);}
  }
 
  /**
   * Attempts to register one or more RPGDataTypes to the appropriate manager
   * and performs certain operations based on the interfaces included:
   * • If the type is a listener it will automatically register it to this module
   * • If the type modifies a vanilla recipe it will be applied to them
   * @param types One or more rpg data types
   */
  protected void registerGenerics(RPGDataType... types) {
    GenericRPGTypeManager<RPGDataType> manager = aeonrpg.getRPGDataTypeManager().getGenericManager();
    for(RPGDataType type : types) {registerData(manager, type);}
  }
  
  /**
   * Attempts to register one or more RPGDataTypes to the appropriate manager
   * and performs certain operations based on the interfaces included:
   * • If the type is a listener it will automatically register it to this module
   * • If the type modifies a vanilla recipe it will be applied to them
   * @param types A collection of generics
   */
  protected void registerGenerics(List<RPGDataType> types) {
    GenericRPGTypeManager<RPGDataType> manager = aeonrpg.getRPGDataTypeManager().getGenericManager();
    for(RPGDataType type : types) {registerData(manager, type);}
  }
  
  /**
   * Attempts to register one or more RPGPotionEffectTypes to the appropriate manager
   * and performs certain operations based on the interfaces included:
   * • If the type is a listener it will automatically register it to this module
   * • If the type modifies a vanilla recipe it will be applied to them
   * @param types One or more rpg data types
   */
  protected void registerPotionEffects(RPGPotionEffectType... types) {
    GenericRPGTypeManager<RPGPotionEffectType> manager = aeonrpg.getRPGDataTypeManager().getPotionEffectManager();
    for(RPGPotionEffectType type : types) {registerData(manager, type);}
  }
  
  /**
   * Attempts to register one or more RPGPotionEffectTypes to the appropriate manager
   * and performs certain operations based on the interfaces included:
   * • If the type is a listener it will automatically register it to this module
   * • If the type modifies a vanilla recipe it will be applied to them
   * @param types A collection of attributes
   */
  protected void registerPotionEffects(List<RPGPotionEffectType> types) {
    GenericRPGTypeManager<RPGPotionEffectType> manager = aeonrpg.getRPGDataTypeManager().getPotionEffectManager();
    for(RPGPotionEffectType type : types) {registerData(manager, type);}
  }
  
  /**
   * Schedules tasks related to this module.
   */
  protected void schedule() {}
  
  /**
   * Saves data tied to this module
   */
  protected void save() {}
  
  /*
   * Unregisters all listeners tied to this module
   */
  protected void unregister() {
    HandlerList.unregisterAll(this);
  }
  
  /*
   * Cancels all tasks tied to this module
   */
  protected void unschedule() {
    Bukkit.getScheduler().cancelTasks(this);
  }
  
  
}
