package com.gmail.alexdion93.aeonrpg.tasks;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.gmail.alexdion93.aeonrpg.AeonRPG;
import com.gmail.alexdion93.aeonrpg.data.type.RPGPotionEffectType;
import com.gmail.alexdion93.aeonrpg.managers.GenericRPGTypeManager;

/**
 * A repeating task used to trigger custom potion effects 
 * @author Alex Dion
 *
 */
public class CustomPotionTick extends BukkitRunnable {

  private long tickRate = -1;

  private GenericRPGTypeManager<RPGPotionEffectType> manager;
  private AeonRPG plugin;

  public CustomPotionTick(AeonRPG plugin, GenericRPGTypeManager<RPGPotionEffectType> manager) {
    this.manager = manager;
    this.plugin = plugin;
    
    File file = new File(plugin.getDataFolder().getAbsoluteFile() + "/config.yml");
    YamlConfiguration config = new YamlConfiguration();
    
    try {
      config.load(file);
      tickRate = config.getLong("potion.rate", 20L);
    }
    catch (IOException | InvalidConfigurationException e) { e.printStackTrace(); }
  }

  @Override
  public void run() {

    // Loop all potion effects
    for (RPGPotionEffectType type : manager.getTypes()) {
      
      // Tick all players
      for (Player player : Bukkit.getOnlinePlayers()) {
        if (!type.has(player)) { continue; }
        tick(player, type);
      }

      // Tick all entities
      for (UUID uuid : type.getAffectedEntities()) {
        Entity entity = Bukkit.getEntity(uuid);
        if (!(entity instanceof LivingEntity)) { continue; }
        if (!type.has(entity)) { continue; }
        tick((LivingEntity) entity, type);
      }
    }
  }

  /**
   * Schedules this task to run
   */
  public void schedule() {
    
    if (tickRate > 0) {
      runTaskTimer(plugin, tickRate, tickRate);
      plugin.getLogger().info("Custom Potion Timer scheduled for " + tickRate + " ticks");
    }
    
    else {
      plugin.getLogger().info("Custom Potion Timer disabled.");
    }
  }

  /**
   * Ticks a specific entity
   *
   * @param entity The entity being ticked.
   * @param type   The type we're looking for
   */
  public void tick(LivingEntity entity, RPGPotionEffectType type) {

    // Fetch the level
    int level = type.getLevel(entity);
    if (level < 0) { return; }

    // Fetch the duration
    int duration = type.getDuration(entity);
    if (duration <= 0) { return; }

    // Update the duration
    duration -= Math.ceil(tickRate / 20.0);
    
    // Apply the tick
    type.onPotionTick(entity, level, duration);
    
    //Remove if no duration
    if (duration <= 0) {
      type.setLevel(entity, 0);
      type.setDuration(entity, 0);
    }
    
    //Update the duration
    else {
      type.setDuration(entity, duration);
    }

  }
}
