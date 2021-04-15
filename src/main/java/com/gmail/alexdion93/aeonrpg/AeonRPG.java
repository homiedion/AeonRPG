package com.gmail.alexdion93.aeonrpg;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.gmail.alexdion93.aeonrpg.commands.CMD_AeonRPG;
import com.gmail.alexdion93.aeonrpg.listeners.ArrowFireListener;
import com.gmail.alexdion93.aeonrpg.listeners.PotionApplyListener;
import com.gmail.alexdion93.aeonrpg.managers.RPGRecipeManager;
import com.gmail.alexdion93.aeonrpg.managers.RPGTypeManager;
import com.gmail.alexdion93.aeonrpg.tasks.CustomPotionTick;
import com.gmail.alexdion93.aeonrpg.util.RPGDataUtil;

/**
 * Plugin Main
 *
 * @author Alex Dion
 */
public class AeonRPG extends JavaPlugin {

  /*
   * Controllers
   */
  private ArrowFireListener arrowListener;
  private PotionApplyListener potionListener;
  private RPGRecipeManager recipeManager;
  private RPGTypeManager typeManager;

  /**
   * Returns the manager of all data types.
   *
   * @return The manager of all data types.
   */
  public RPGTypeManager getRPGDataTypeManager() {
    return typeManager;
  }

  /**
   * Returns the recipe manager
   *
   * @return the recipe manager
   */
  public RPGRecipeManager getRPGRecipeManager() {
    return recipeManager;
  }

  /*
   * Initializes variables
   */
  private void init() {
    RPGDataUtil.init(this);
    typeManager = new RPGTypeManager(this);
    recipeManager = new RPGRecipeManager();
    arrowListener = new ArrowFireListener(this);
    potionListener = new PotionApplyListener(typeManager.getPotionEffectManager());
  }

  /**
   * Loads the plugin's configuration
   */
  private void load() {
    if (openPluginFile("config.yml") == null) {
      saveResource("config.yml", false);
    }
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

  /**
   * Fetches a plugin file and returns it.
   * @param filename The target file name
   * @return The configuration file.
   */
  public YamlConfiguration openPluginFile(String filename) {
    YamlConfiguration config = new YamlConfiguration();
    
    try { config.load(getDataFolder().getAbsolutePath() + "/" + filename); }
    catch (Exception e) { return null; }
    
    return config;
  }

  /*
   * Registers events
   */
  private void register() {
    // Variables
    PluginManager manager = Bukkit.getPluginManager();

    // Register Commands
    new CMD_AeonRPG(this);

    // Register Listeners
    manager.registerEvents(typeManager, this);
    manager.registerEvents(recipeManager, this);
    manager.registerEvents(arrowListener, this);
    manager.registerEvents(potionListener, this);
  }

  /*
   * Saves the plugin's data
   */
  private void save() {
    typeManager.save();
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
