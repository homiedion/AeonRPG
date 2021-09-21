package com.gmail.alexdion93.aeonrpg.data.type;

import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.LingeringPotionSplashEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import com.gmail.alexdion93.aeonrpg.data.interfaces.RPGDataTwoValued;
import com.gmail.alexdion93.aeonrpg.util.RomanNumeral;

/**
 * Represents a potion effect.
 * @author Alex Dion
 */
public abstract class RPGPotionEffectType extends RPGDataType implements RPGDataTwoValued, Listener {

  private NamespacedKey primary;
  private NamespacedKey secondary;
  private HashSet<UUID> entities;
  
  /**
   * Constructor
   *
   * @param plugin      The targe tplugin.
   * @param key         The key of this type.
   * @param displayName The display name of this type.
   * @param description This type's description
   */
  public RPGPotionEffectType(JavaPlugin plugin, String key, String displayName, String description) {
    super(plugin, "potioneffect." + key.toLowerCase(), displayName, description);
    primary = new NamespacedKey(plugin, "potioneffect." + key.toLowerCase() + ".level");
    secondary = new NamespacedKey(plugin, "potioneffect." + key.toLowerCase() + ".duration");
    entities = new HashSet<>();
  }

  /**
   * Applies the potion effect to the holder.
   * If the potion already exists the effects are
   * combined using the following logic:
   *   * If the levels are equal
   *        * Level remains the same
   *        * Durations combine
   *   * If the levels aren't equal
   *        * The higher level takes priority
   *        * A fraction of the duration is added
   * @param container The persistent data holder.
   * @param level The level of the effect.
   * @param duration The duration of the effect.
   */
  public void apply(PersistentDataContainer container, int level, int duration) {
    int currentLevel = getLevel(container);
    int currentDuration = getDuration(container);
    int newDuration = 0;
    
    if (level == currentLevel) { newDuration = duration + currentDuration; }
    else {
      int divisor = 1 + Math.abs(currentLevel - level);
      
      if (level > currentLevel) { newDuration = duration + (currentDuration / divisor); }
      else { newDuration = currentDuration + (duration / divisor); }
    }
    
    setLevel(container, Math.max(currentLevel, level));
    setDuration(container, newDuration);
  }
  
  /**
   * Applies the potion effect to the holder.
   * If the potion already exists the effects are
   * combined using the following logic:
   *   * If the levels are equal
   *        * Level remains the same
   *        * Durations combine
   *   * If the levels aren't equal
   *        * The higher level takes priority
   *        * A fraction of the duration is added
   * @param holder The persistent data holder.
   * @param level The level of the effect.
   * @param duration The duration of the effect.
   */
  public void apply(PersistentDataHolder holder, int level, int duration) {
    int currentLevel = getLevel(holder);
    int currentDuration = getDuration(holder);
    int newDuration = 0;
    
    if (level == currentLevel) { newDuration = duration + currentDuration; }
    else {
      int divisor = 1 + Math.abs(currentLevel - level);
      
      if (level > currentLevel) { newDuration = duration + (currentDuration / divisor); }
      else { newDuration = currentDuration + (duration / divisor); }
    }
    
    setLevel(holder, Math.max(currentLevel, level));
    setDuration(holder, newDuration);
    
    //Trigger the apply effect
    if (holder instanceof LivingEntity) {
      LivingEntity entity = (LivingEntity) holder;
      onPotionApply(entity, level, duration);
      
      //IF this isn't a palyer add them to the map
      if (entity.getType() == EntityType.PLAYER) { return; }
      entities.add(entity.getUniqueId());
    }
  }

  /**
   * Returns the affected entities.
   * @return A set of affected entities.
   */
  public HashSet<UUID> getAffectedEntities() {
    return entities;
  }

  @Override
  public RPGDataAlignment getAlignment() { return RPGDataAlignment.POSITIVE; }
  
  /**
   * Returns the duration of the potion effect.
   * @param container The persistent data container.
   * @return The duration of the potion effect.
   */
  public int getDuration(PersistentDataContainer container) {
    return get(container, getSecondaryKey(), PersistentDataType.INTEGER, 0);
  }

  /**
   * Returns the duration of the potion effect.
   * @param container The persistent data container.
   * @return The duration of the potion effect.
   */
  public int getDuration(PersistentDataHolder holder) {
    return getDuration(holder.getPersistentDataContainer());
  }

  /**
   * Returns the level of the potion effect.
   * @param container The persistent data container.
   * @return The level of the potion effect.
   */
  public int getLevel(PersistentDataContainer container) {
    return get(container, getPrimaryKey(), PersistentDataType.INTEGER, 0);
  }

  /**
   * Returns the level of the potion effect.
   * @param container The persistent data container.
   * @return The level of the potion effect.
   */
  public int getLevel(PersistentDataHolder holder) {
    return getLevel(holder.getPersistentDataContainer());
  }
  
  @Override
  public int getPrimaryDefaultValue() { return 0; };
  
  @Override
  public NamespacedKey getPrimaryKey() { return primary; }
  
  @Override
  public int getSecondaryDefaultValue() { return 0; }
  
  @Override
  public NamespacedKey getSecondaryKey() { return secondary; }
  
  /**
   * Return if the entity has this potion effect.
   * @param entity The target entity.
   * @return True if present.
   */
  public boolean has(Entity entity) {
    return has(entity, getPrimaryKey(), PersistentDataType.INTEGER);
  }
  
  /**
   * Returns if this potion effect applies instantly.
   * @return A boolean representing is this is an instant.
   */
  public boolean isInstant() { return false; }

  public boolean isLevelIndependent() { return false; }
  
  @Override
  public boolean isNegativeAllowed() { return false; }
  
  /**
   * Applies custom potion effects to creatures within the area effect cloud
   *
   * @param event The event being fired
   */
  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void onAreaEffectCloudApply(AreaEffectCloudApplyEvent event) {
    // Fetch the cloud
    AreaEffectCloud cloud = event.getEntity();

    // Fetch the data
    PersistentDataContainer data = cloud.getPersistentDataContainer();

    // Fetch the level
    int level = getLevel(data);
    if (level <= 0) { return; }
    
    // Fetch the duration
    int duration = getDuration(data) / 4;
    if (duration <= 0 && !isInstant()) { return; }
    
    //Apply to all entities
    for (LivingEntity entity : event.getAffectedEntities()) {
      apply(entity, level, duration);
    }
  }
  
  /**
   * Applies custom potion effects when an item is consumed
   *
   * @param event The event being fired
   */
  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
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

    // Fetch the level
    int level = getLevel(data);
    if (level <= 0) { return; }
    
    // Fetch the duration
    int duration = getDuration(data);
    if (duration <= 0 && !isInstant()) { return; }
    
    //Apply the potion effect
    apply(player, level, duration);
  }
  
  /**
   * Adds newly spawned creatures to the set of affected creatures
   * @param event The event being triggered.
   */
  public void onCreatureSpawn(CreatureSpawnEvent event) {
    LivingEntity entity = event.getEntity();
    if (!has(entity)) { return; }
    
    entities.add(entity.getUniqueId());
  }
  
  /**
   * Removes the entity from the set if it dies
   * @param event The event being fired
   */
  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void onEntityDeath(EntityDeathEvent event) {
    entities.remove(event.getEntity().getUniqueId());
  }
  
  /**
   * Applies custom potion effects to area effect clouds
   * @param event The event being fired
   */
  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void onLingeringSplashPotion(LingeringPotionSplashEvent event) {

    // Fetch the potion
    ThrownPotion potion = event.getEntity();
    if (potion == null) { return; }
    
    //Fetch the item
    ItemStack item = potion.getItem();
    if (item == null) { return; }

    // Fetch the meta
    ItemMeta meta = item.getItemMeta();
    if (meta == null) { return; }

    // Fetch the data
    PersistentDataContainer data = meta.getPersistentDataContainer();
    
    // Fetch the level
    int level = getLevel(data);
    if (level <= 0) { return; }
    
    // Fetch the duration
    int duration = getDuration(data);
    if (duration <= 0 && !isInstant()) { return; }
    
    // Fetch the effect cloud
    AreaEffectCloud cloud = event.getAreaEffectCloud();
    PersistentDataContainer cloudData = cloud.getPersistentDataContainer();
    PotionType potionType = cloud.getBasePotionData().getType();

    // Ensure the cloud effect always triggers the event
    if (potionType == PotionType.AWKWARD || 
        potionType == PotionType.MUNDANE || 
        potionType == PotionType.THICK || 
        potionType == PotionType.UNCRAFTABLE) {
      cloud.addCustomEffect(new PotionEffect(PotionEffectType.LUCK, -1, 0, false, false, false), false);
    }

    //Apply to the cloud
    apply(cloudData, level, duration);
  }

  /**
   * Removes all potion effects from a player who died
   * @param event The event being fired
   */
  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void onPlayerDeath(PlayerDeathEvent event) {
    LivingEntity entity = event.getEntity();
    remove(entity, getPrimaryKey(), PersistentDataType.INTEGER, 0);
    remove(entity, getSecondaryKey(), PersistentDataType.INTEGER, 0);
  }
  
  /**
   * Triggers when the potion effect is applied.
   * @param entity   The entity being targetted.
   * @param level    The level of the effect.
   * @param duration The remaining duration of the effect.
   */
  public abstract void onPotionApply(LivingEntity entity, int level, int duration);
  
  /**
   * Applies custom potion effects when splash potions are used
   * @param event The event being fired
   */
  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void onPotionSplash(PotionSplashEvent event) {

    // Fetch the potion
    ThrownPotion potion = event.getPotion();
    if (potion == null) { return; }
    
    // Fetch the item
    ItemStack item = potion.getItem();
    if (item == null) { return; }

    // Fetch the meta
    ItemMeta meta = item.getItemMeta();
    if (meta == null) { return; }

    // Fetch the data
    PersistentDataContainer data = meta.getPersistentDataContainer();

    // Fetch the level
    int level = getLevel(data);
    if (level <= 0) { return; }
    
    // Fetch the duration
    int duration = getDuration(data);
    if (duration <= 0 && !isInstant()) { return; }

    // Loop affected entities
    for (LivingEntity entity : event.getAffectedEntities()) {
      double intensity = event.getIntensity(entity);
      apply(entity, level, (int) (duration * intensity));
    }
  }
  
  /**
   * Triggers when the potion effect ticks.
   * @param entity   The entity being targetted.
   * @param level    The level of the effect.
   * @param duration The remaining duration of the effect.
   */
  public abstract void onPotionTick(LivingEntity entity, int level, int duration);
  
  /**
   * Build the uuid set when the server finishes loading
   * @param event The event being fired
   */
  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void onServerLoad(ServerLoadEvent event) {
    for(World world : Bukkit.getWorlds()) {
      for(Entity entity : world.getEntities()) {
        if (!(entity instanceof LivingEntity)) { continue; }
        if (!has(entity)) { continue; }
        entities.add(entity.getUniqueId());
      }
    }
  }

  /**
   * Sets the potion's duration on the container.
   * @param container The persistent data container.
   * @param value The value we're setting this to.
   */
  public void setDuration(PersistentDataContainer container, int value) {
    if (value == 0 || (value < 0 && !isNegativeAllowed())) {
      remove(container, getSecondaryKey(), PersistentDataType.INTEGER, value);
      return;
    }
    set(container, getSecondaryKey(), PersistentDataType.INTEGER, value);
  }

  /**
   * Sets the potion's duration on the holder.
   * @param holder The persistent data holder.
   * @param value The value we're setting this to.
   */
  public void setDuration(PersistentDataHolder holder, int value) {
    setDuration(holder.getPersistentDataContainer(), value);
  }

  /**
   * Sets the potion's level on the container.
   * @param container The persistent data container.
   * @param value The value we're setting this to.
   */
  public void setLevel(PersistentDataContainer container, int value) {
    if (value == 0 || (value < 0 && !isNegativeAllowed())) {
      remove(container, getPrimaryKey(), PersistentDataType.INTEGER, 0);
      return;
    }
    set(container, getPrimaryKey(), PersistentDataType.INTEGER, value);
  }

  /**
   * Sets the potion's level on the holder.
   * @param holder The persistent data holder.
   * @param value The value we're setting this to.
   */
  public void setLevel(PersistentDataHolder holder, int value) {
    setLevel(holder.getPersistentDataContainer(), value);
  }
  
  @Override
  public String toLoreString(Object... values) {
    
    //Empty Check
    if (values.length == 0) { return null; }
    
    //Variables
    int level = (values.length >= 1 && values[0] != null ? (int) values[0] : 0);
    int duration = (values.length >= 2 && values[1] != null ? (int) values[1] : 0);
    
    //Invalid level check
    if (level <= 0) { return null; }
    
    // Variables
    int minutes = (duration / 60);
    int seconds = (duration % 60);
    ChatColor color = (getAlignment() == RPGDataAlignment.NEGATIVE ? ChatColor.RED : ChatColor.GRAY);
    String romanNumeral = (isLevelIndependent() && (level > 0) ? "" : " " + RomanNumeral.parse(level));
    String display = color + getDisplayName() + romanNumeral + " (" + minutes + ":" + (seconds < 10 ? "0" : "")
        + seconds + ")";

    // Return
    return display;
  }
}
