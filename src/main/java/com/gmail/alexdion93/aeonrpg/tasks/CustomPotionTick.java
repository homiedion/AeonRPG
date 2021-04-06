package com.gmail.alexdion93.aeonrpg.tasks;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
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

  public static final long SECONDS = 1;

  private GenericRPGTypeManager<RPGPotionEffectType> manager;
  private AeonRPG plugin;

  public CustomPotionTick(AeonRPG plugin, GenericRPGTypeManager<RPGPotionEffectType> manager) {
    this.manager = manager;
    this.plugin = plugin;
  }

  @Override
  public void run() {

    // Loop all potion effects
    for (RPGPotionEffectType type : manager.getTypes()) {
      // Tick all players
      for (Player player : Bukkit.getOnlinePlayers()) {
        tick(player, type);
      }

      // Tick all entities
      for (UUID uuid : type.getAffectedMobs()) {

        // Fetch the entity
        Entity entity = Bukkit.getEntity(uuid);
        if ((entity == null) || entity.isDead() || !(entity instanceof LivingEntity)) {
          type.getAffectedMobs().remove(uuid);
          continue;
        }

        // Tick the entity
        tick((LivingEntity) entity, type);
      }
    }

  }

  /**
   * Schedules this task to run
   */
  public void schedule() {
    runTaskTimer(plugin, SECONDS * 20L, SECONDS * 20L);
  }

  /**
   * Ticks a specific entity
   *
   * @param entity The entity being ticked.
   * @param type   The type we're looking for
   */
  public void tick(LivingEntity entity, RPGPotionEffectType type) {

    PersistentDataContainer data = entity.getPersistentDataContainer();
    NamespacedKey key = type.getNamespacedKey();
    NamespacedKey alt = type.getAltNamespacedKey();

    int level = -1;
    int duration = -1;

    // Fetch the level
    if (data.has(key, PersistentDataType.INTEGER)) {
      level = data.get(key, PersistentDataType.INTEGER);
    }
    if (level < 0) {
      return;
    }

    // Fetch the duration
    if (data.has(alt, PersistentDataType.INTEGER)) {
      duration = data.get(alt, PersistentDataType.INTEGER);
    }
    if (duration <= 0) {
      return;
    }

    // Apply the tick
    type.onPotionTick(entity, level, duration);

    // Update the duration
    duration -= SECONDS;
    if (duration <= 0) {
      type.removeFrom(entity);
    } else {
      data.set(alt, PersistentDataType.INTEGER, duration);
    }

  }
}
