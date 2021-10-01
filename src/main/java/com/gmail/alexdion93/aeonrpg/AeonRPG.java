package com.gmail.alexdion93.aeonrpg;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.gmail.alexdion93.aeonrpg.commands.*;
import com.gmail.alexdion93.aeonrpg.data.type.RPGAttributeType;
import com.gmail.alexdion93.aeonrpg.data.type.RPGEnchantmentType;
import com.gmail.alexdion93.aeonrpg.data.type.RPGPotionEffectType;
import com.gmail.alexdion93.aeonrpg.data.type.RPGSkillType;
import com.gmail.alexdion93.aeonrpg.listeners.*;
import com.gmail.alexdion93.aeonrpg.managers.*;
import com.gmail.alexdion93.aeonrpg.tasks.*;
import com.gmail.alexdion93.aeonrpg.util.RPGDataUtil;

/**
 * Plugin Main
 * @author Alex Dion
 */
public class AeonRPG extends JavaPlugin {

  private static final boolean DEBUG = true;
  private static final String[] FOLDERS = {"Items", "Entities"};
  
  private ArrowFireListener arrowListener;
  private RPGTypeManager typeManager;
  private RPGEntityManager entityManager;

  /**
   * Returns the manager of all data types.
   * @return The manager of all data types.
   */
  public RPGTypeManager getRPGDataTypeManager() {
    return typeManager;
  }
  
  /**
   * Returns the manager of all rpg entities.
   * @return The manager of all rpg entities.
   */
  public RPGEntityManager getRPGEntityManager() {
    return entityManager;
  }

  /*
   * Initializes variables
   */
  private void init() {
    RPGDataUtil.init(this);
    typeManager = new RPGTypeManager(this);
    entityManager = new RPGEntityManager(this);
    arrowListener = new ArrowFireListener(this);
    if (DEBUG) { getLogger().warning("DEBUG Mode Enabled"); }
    
    createData();
  }
  
  /**
   * Updates all files and folders associated with this plugin.
   */
  private void createData() {
    
    //Create all folders associated with the plugin
    for(String folder : FOLDERS) {
      File directory = new File(getDataFolder().getAbsoluteFile() + "/" + folder);
      if (directory.exists()) { continue; }
      directory.mkdirs();
    }
    
    //Create configuration file if it doesn't exist
    File file = new File(getDataFolder().getAbsoluteFile() + "/config.yml");
    if (!file.exists()) {
      saveResource("config.yml", false);
    }
  }

  /**
   * Loads the plugin's configuration
   */
  private void load() {
  }

  /*
   * Plugin Disable
   */
  @Override
  public void onDisable() {
    unregister();
    unschedule();
    save();
  }

  /*
   * Plugin Enable
   */
  @Override
  public void onEnable() {
    init();
    load();
    register();
    schedule();
  }

  /*
   * Registers events
   */
  private void register() {
    // Variables
    PluginManager manager = Bukkit.getPluginManager();

    // Register Commands
    new Command_RPGEntity(this);
    new Command_RPGItem(this);
    new Command_RPGPlayer(this);
    //new Command_RPGInspect(this);
    //new Command_RPGItem(this);
    //new Command_RPGPlayer(this);
    

    // Register Listeners
    manager.registerEvents(typeManager, this);
    manager.registerEvents(entityManager, this);
    manager.registerEvents(arrowListener, this);
    
    
    //Debug Type Registrations
    if (DEBUG) {
      getLogger().warning("Registering DEBUG types...");
      
      //Debug Attribute
      RPGAttributeType attribute = new RPGAttributeType(this, "Test_Attribute", "Test Attribute", "For testing purposes.") {};
      typeManager.getAttributeManager().register(attribute);
      
      //Debug Enchantment
      RPGEnchantmentType enchantment = new RPGEnchantmentType(this, "Test_Enchantment", "Test Enchantment", "For testing purposes.") {};
      typeManager.getEnchantmentManager().register(enchantment);
      
      //Debug Potion Effect
      RPGPotionEffectType potion = new RPGPotionEffectType(this, "Test_Potion", "Test Potion", "For testing purposes.") {

        @Override
        public void onPotionApply(LivingEntity entity, int level, int duration) {
          entity.sendMessage("Lv." + level + " " + getDisplayName() +
              " has been applied to you for " + duration + " seconds.");
        }

        @Override
        public void onPotionTick(LivingEntity entity, int level, int duration) {
          entity.sendMessage("Lv." + level + " " + getDisplayName() +
              " has ticked. " + duration + " seconds remain.");
          entity.getLocation().getWorld().playEffect(entity.getEyeLocation(), Effect.MOBSPAWNER_FLAMES, 0);
        }};
      typeManager.getPotionEffectManager().register(potion);
      
      //Debug Skill
      RPGSkillType skill = new RPGSkillType(this, "Test_Skill", "Test Skill", "For testing purposes.") {};
      typeManager.getSkillManager().register(skill);
    }
  }

  /*
   * Saves the plugin's data
   */
  private void save() {
    entityManager.save();
  }

  /**
   * Saves a plugin file.
   * @param config The yaml configuration we're saving.
   * @param filename The name of the file we're saving to.
   */
  public void savePluginFile(YamlConfiguration config, String filename) {
    
    try {
      config.save(getDataFolder().getAbsolutePath() + "/" + filename);
    }
    catch (Exception e) { e.printStackTrace(); }
  }

  /*
   * Schedules tasks to be run
   */
  private void schedule() {
    new CustomPotionTick(this, typeManager.getPotionEffectManager()).schedule();
  }
  
  /*
   * Unregisters all listeners tied to this plugin
   */
  private void unregister() {
    HandlerList.unregisterAll(this);
  }
  
  /*
   * Cancels all tasks tied to this plugin.
   */
  private void unschedule() {
    Bukkit.getScheduler().cancelTasks(this);
  }
}
