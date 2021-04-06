package com.gmail.alexdion93.aeonrpg;

import org.bukkit.Bukkit;
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
  public void init() {
    RPGDataUtil.init(this);
    typeManager = new RPGTypeManager(this);
    recipeManager = new RPGRecipeManager();
    arrowListener = new ArrowFireListener(typeManager);
    potionListener = new PotionApplyListener(typeManager.getPotionEffectManager());
  }

  /**
   * Loads the plugin's configuration
   */
  public void load() {
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
  public void register() {
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
   * Saves the plugins
   */
  public void save() {
  }

  /*
   * Schedules tasks to be run
   */
  public void schedule() {
    new CustomPotionTick(this, typeManager.getPotionEffectManager()).schedule();
  }

  /*
   * Unregisters all listeners and tasks
   */
  public void unregister() {
    HandlerList.unregisterAll(this);
  }

  /*
   * Cancels all tasks
   */
  public void unschedule() {
    Bukkit.getScheduler().cancelTasks(this);
  }
}
