package com.gmail.alexdion93.aeonrpg.util;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.gmail.alexdion93.aeonrpg.data.type.RPGPotionEffectType;

/**
 * A utility for handing the application of potions.
 * @author Alex Dion
 *
 */
public class RPGPotionUtil {

  /**
   * Applies a potion effect to the target, increasing the duration based on old
   * effect.
   *
   * @param target the target of the effect
   * @param effect the effect type.
   */
  public static void applyPotionEffect(LivingEntity target, PotionEffect effect) {
    applyPotionEffect(target, effect.getType(), effect.getDuration(), effect.getAmplifier());
  }

  /**
   * Applies a potion effect to the target, increasing the duration based on old
   * effect.
   *
   * @param target    the target of the effect
   * @param type      the effect type.
   * @param ticks     the new effect's duration in ticks.
   * @param amplifier the new effect's amplifier.
   */
  public static void applyPotionEffect(LivingEntity target, PotionEffectType type, int ticks, int amplifier) {

    // Fetch the old potion effect
    PotionEffect potion = target.getPotionEffect(type);
    if (potion == null) {
      target.addPotionEffect(new PotionEffect(type, ticks, amplifier));
      return;
    }

    // Remove the old potion effect
    target.removePotionEffect(type);

    // Calculate the divisor
    int divisor = 1 + Math.abs(amplifier - potion.getAmplifier());
    int duration = (amplifier >= potion.getAmplifier() ? ticks + (potion.getDuration() / divisor)
        : potion.getDuration() + (ticks / divisor));

    // Apply the potion effect
    target.addPotionEffect(new PotionEffect(type, duration, Math.max(amplifier, potion.getAmplifier())));
  }

  /**
   * Applies a potion effect to the target, increasing the duration based on old
   * effect.
   *
   * @param target the target of the effect
   * @param type   the effect type.
   * @param ticks  the new effect's duration in ticks.
   * @param level  the new effect's level.
   */
  public static void applyPotionEffect(LivingEntity target, RPGPotionEffectType type, int ticks, int level) {

    // Level Validation
    if (level <= 0) {
      return;
    }

    // Duration potion effect
    if (!type.isInstant()) {

      // Time Validation
      if (ticks <= 0) {
        return;
      }

      // Fetch Data and Keys
      PersistentDataContainer data = target.getPersistentDataContainer();
      NamespacedKey key = type.getNamespacedKey();
      NamespacedKey alt = type.getAltNamespacedKey();

      // Fetch the old potion effect
      int oldLevel = data.getOrDefault(key, PersistentDataType.INTEGER, 0);
      int oldDuration = data.getOrDefault(alt, PersistentDataType.INTEGER, 0);

      // Calculate the divisor
      int divisor = (oldLevel == 0 ? 1 : 1 + Math.abs(level - oldLevel));
      int duration = (level >= oldLevel ? ticks + (oldDuration / divisor) : oldDuration + (ticks / divisor));

      // Modify the custom potion effect on the entity
      data.set(key, PersistentDataType.INTEGER, Math.max(oldLevel, level));
      data.set(alt, PersistentDataType.INTEGER, duration);

      // Trigger the apply effect
      type.onPotionApply(target, level, duration);

      // Add affected mobs to the map
      if (target.getType() != EntityType.PLAYER) {
        type.getAffectedMobs().add(target.getUniqueId());
      }
    }

    // Instant potion effect
    else {
      type.onPotionApply(target, level, -1);
    }
  }
}
