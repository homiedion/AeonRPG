package com.gmail.alexdion93.aeonrpg.listeners;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent.Action;
import org.bukkit.event.entity.LingeringPotionSplashEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import com.gmail.alexdion93.aeonrpg.data.type.RPGPotionEffectType;
import com.gmail.alexdion93.aeonrpg.managers.GenericRPGTypeManager;
import com.gmail.alexdion93.aeonrpg.util.Pair;
import com.gmail.alexdion93.aeonrpg.util.RPGPotionUtil;

//import javafx.util.Pair;

public class PotionApplyListener implements Listener {

  private GenericRPGTypeManager<RPGPotionEffectType> manager;

  /**
   * Constructor
   *
   * @param manager The potion effect manager
   */
  public PotionApplyListener(GenericRPGTypeManager<RPGPotionEffectType> manager) {
    this.manager = manager;
  }

  /**
   * Applies custom potion effects to creatures within the area effect cloud
   *
   * @param event The event being fired
   */
  @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
  public void onAreaEffectCloudApply(AreaEffectCloudApplyEvent event) {
    // Fetch the cloud
    AreaEffectCloud cloud = event.getEntity();

    // Fetch the data
    PersistentDataContainer data = cloud.getPersistentDataContainer();

    // Fetch the effects
    HashMap<RPGPotionEffectType, Pair<Integer, Integer>> effects = new HashMap<>();
    for (RPGPotionEffectType type : manager.getTypes()) {
      NamespacedKey key = type.getNamespacedKey();
      NamespacedKey alt = type.getAltNamespacedKey();

      // Fetch the level
      int level = data.getOrDefault(key, PersistentDataType.INTEGER, -1);
      int duration = data.getOrDefault(alt, PersistentDataType.INTEGER, -1);

      // Add to the map
      effects.put(type, new Pair<Integer, Integer>(level, duration));
    }

    // Loop affected entities
    for (LivingEntity entity : event.getAffectedEntities()) {

      // Loop all effects
      for (RPGPotionEffectType type : effects.keySet()) {
        // Fetch the level and duration
        Pair<Integer, Integer> pair = effects.get(type);

        // Apply to the entity
        boolean isInstant = type.isInstant();
        RPGPotionUtil.applyPotionEffect(entity, type, (int) Math.ceil(pair.getValue() / (isInstant ? 4.0 : 1.0)),
            (int) Math.ceil(pair.getKey() / (isInstant ? 1.0 : 2.0)));
      }
    }
  }

  /**
   * Applies custom potion effects when an item is consumed
   *
   * @param event The event being fired
   */
  @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
  public void onConsume(PlayerItemConsumeEvent event) {
    // Variables
    Player player = event.getPlayer();
    ItemStack item = event.getItem();

    // Fetch the meta
    ItemMeta meta = item.getItemMeta();
    if (meta == null) {
      return;
    }

    // Fetch the data container
    PersistentDataContainer data = meta.getPersistentDataContainer();

    // Apply any potion effects
    for (RPGPotionEffectType type : manager.getTypes()) {
      if (type == null) {
        continue;
      }

      // Fetch duration and level
      int duration = data.getOrDefault(type.getAltNamespacedKey(), PersistentDataType.INTEGER, 0);
      int level = data.getOrDefault(type.getNamespacedKey(), PersistentDataType.INTEGER, 0);

      // Apply to the target
      RPGPotionUtil.applyPotionEffect(player, type, duration, level);
    }
  }

  /**
   * Removes all potion effects from entity who died
   *
   * @param event The event being fired
   */
  @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
  public void onEntityDeath(EntityDeathEvent event) {

    LivingEntity entity = event.getEntity();

    // Player
    if (entity.getType() == EntityType.PLAYER) {
      for (RPGPotionEffectType type : manager.getTypes()) {
        type.removeFrom(entity);
      }
    }

    // Mob
    else {
      UUID uuid = entity.getUniqueId();
      for (RPGPotionEffectType type : manager.getTypes()) {
        type.getAffectedMobs().remove(uuid);
      }
    }
  }

  /**
   * Applies custom potion effects to area effect clouds
   *
   * @param event The event being fired
   */
  @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
  public void onLingeringSplashPotion(LingeringPotionSplashEvent event) {

    // Fetch the item
    ThrownPotion potion = event.getEntity();
    if (potion == null) {
      return;
    }
    ItemStack item = potion.getItem();
    if (item == null) {
      return;
    }

    // Fetch the meta
    ItemMeta meta = item.getItemMeta();
    if (meta == null) {
      return;
    }

    // Fetch the data
    PersistentDataContainer data = meta.getPersistentDataContainer();

    // Fetch the effect cloud
    AreaEffectCloud cloud = event.getAreaEffectCloud();
    PersistentDataContainer cloudData = cloud.getPersistentDataContainer();
    PotionType potionType = cloud.getBasePotionData().getType();

    // Ensure the cloud effect always triggers the event
    if ((potionType == PotionType.AWKWARD) || (potionType == PotionType.MUNDANE) || (potionType == PotionType.THICK)
        || (potionType == PotionType.UNCRAFTABLE)) {
      cloud.addCustomEffect(new PotionEffect(PotionEffectType.LUCK, -1, 0, false, false, false), false);
    }

    // Fetch the effects and apply to the cloud
    for (RPGPotionEffectType type : manager.getTypes()) {
      NamespacedKey key = type.getNamespacedKey();
      NamespacedKey alt = type.getAltNamespacedKey();

      // Fetch the level
      int level = data.getOrDefault(key, PersistentDataType.INTEGER, -1);
      int duration = data.getOrDefault(alt, PersistentDataType.INTEGER, -1);

      // Level Check
      if (level <= 0) {
        continue;
      }

      // Add to the entity
      cloudData.set(key, PersistentDataType.INTEGER, level);
      cloudData.set(alt, PersistentDataType.INTEGER, duration);
    }
  }

  /**
   * Applies custom potion calculation to vanilla effects
   *
   * @param event The event being fired
   */
  @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
  public void onPotionApply(EntityPotionEffectEvent event) {

    if (event.getAction() != Action.CHANGED) {
      return;
    }
    if (!(event.getEntity() instanceof LivingEntity)) {
      return;
    }

    // Skip certain causes to prevent massive stacks from recurring sources
    switch (event.getCause()) {
    case AREA_EFFECT_CLOUD:
      break;
    case ARROW:
      break;
    case ATTACK:
      break;
    case BEACON:
      return;
    case COMMAND:
      break;
    case CONDUIT:
      return;
    case CONVERSION:
      return;
    case DEATH:
      return;
    case DOLPHIN:
      return;
    case EXPIRATION:
      return;
    case FOOD:
      break;
    case ILLUSION:
      break;
    case MILK:
      return;
    case PATROL_CAPTAIN:
      break;
    case PLUGIN:
      return;
    case POTION_DRINK:
      break;
    case POTION_SPLASH:
      break;
    case SPIDER_SPAWN:
      return;
    case TOTEM:
      break;
    case TURTLE_HELMET:
      return;
    case UNKNOWN:
      break;
    case VILLAGER_TRADE:
      break;
    case WITHER_ROSE:
      break;
    default:
      break;
    }

    // Apply the custom potion calculations
    RPGPotionUtil.applyPotionEffect((LivingEntity) event.getEntity(), event.getNewEffect());
    event.setCancelled(true);
  }

  /**
   * Applies custom potion effects when splash potions are used
   *
   * @param event The event being fired
   */
  @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
  public void onPotionSplash(PotionSplashEvent event) {

    // Fetch the item
    ThrownPotion potion = event.getPotion();
    if (potion == null) {
      return;
    }
    ItemStack item = potion.getItem();
    if (item == null) {
      return;
    }

    // Fetch the meta
    ItemMeta meta = item.getItemMeta();
    if (meta == null) {
      return;
    }

    // Fetch the data
    PersistentDataContainer data = meta.getPersistentDataContainer();

    // Fetch the effects
    HashMap<RPGPotionEffectType, Pair<Integer, Integer>> effects = new HashMap<>();
    for (RPGPotionEffectType type : manager.getTypes()) {
      NamespacedKey key = type.getNamespacedKey();
      NamespacedKey alt = type.getAltNamespacedKey();

      // Fetch the level
      int level = data.getOrDefault(key, PersistentDataType.INTEGER, -1);
      int duration = data.getOrDefault(alt, PersistentDataType.INTEGER, -1);

      // Add to the map
      effects.put(type, new Pair<Integer, Integer>(level, duration));
    }

    // Loop affected entities
    for (LivingEntity entity : event.getAffectedEntities()) {

      double intensity = event.getIntensity(entity);

      // Loop all effects
      for (RPGPotionEffectType type : effects.keySet()) {

        // Fetch the level and duration
        Pair<Integer, Integer> pair = effects.get(type);

        // Apply to the entity while factoring in the intensity
        RPGPotionUtil.applyPotionEffect(entity, type, (int) (pair.getValue() * intensity), pair.getKey());
      }
    }
  }
}
