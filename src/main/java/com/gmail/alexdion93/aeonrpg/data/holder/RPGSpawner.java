package com.gmail.alexdion93.aeonrpg.data.holder;

import java.util.Random;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import com.gmail.alexdion93.aeonrpg.AeonRPG;

/**
 * TODO: Spawn function
 * @author Alex Dion
 */
public class RPGSpawner {

  private Location location;
  private String entity;
  private int radius = 5;
  private long cooldown;
  private int minCooldown = 30000; // 30 seconds
  private int maxCooldown = 60000; // 1 Minute
  private boolean dirty = false;
  private int maxEntityCount = 10;
  
  /**
   * Constructor
   */
  public RPGSpawner(String entity, Location location) {
    if (entity == null) { throw new IllegalArgumentException("No rpg entity provided."); }
    if (location == null) { throw new IllegalArgumentException("No location provided."); }
    setEntity(entity);
    setLocation(location);
    resetCooldown();
  }
  
  /**
   * Returns the key of the entity tied to this spawner.
   * @return The key of the entity tied to this spawner.
   */
  public String getEntity() {
    return entity;
  }
  
  /**
   * Returns the location of spawner.
   * @return The location of spawner.
   */
  public Location getLocation() {
    return location;
  }
  
  /**
   * Returns the maximum cooldown of this spawner.
   * @return The maximum cooldown of this spawner.
   */
  public int getMaximumCooldown() {
    return maxCooldown;
  }
  
  /**
   * Returns the minimum cooldown of this spawner.
   * @return The minimum cooldown of this spawner.
   */
  public int getMinimumCooldown() {
    return minCooldown;
  }
  
  /**
   * Returns the radius of spawner.
   * @return The radius of spawner.
   */
  public int getRadius() {
    return radius;
  }
  
  /**
   * Returns if changes were made to the spawner. 
   * @return True if the spawner needs to be changed.
   */
  public boolean isDirty() { return dirty; }
  
  /**
   * Returns if this is on cooldown.
   * @return True if the spawner is on cooldown.
   */
  public boolean isOnCooldown() {
    return (System.currentTimeMillis() > cooldown);
  }
  
  /**
   * Resets the cooldown of the spawner. 
   */
  public void resetCooldown() {
    Random rand = new Random();
    cooldown = rand.nextInt(maxCooldown - minCooldown) + minCooldown;
  }
  
  /**
   * Sets the key of the entity tied to this spawner.
   * @param entity The key of the entity being spawned.
   * @return The object instance.
   */
  public void setEntity(String entity) {
    this.entity = entity;
    dirty = true;
  }
  
  /**
   * Sets the location of this spawner.
   * @param location The new location.
   * @return The object instance.
   */
  public RPGSpawner setLocation(Location location) {
    this.location = location;
    dirty = true;
    return this;
  }
  
  /**
   * Sets the maximum cooldown this spawner can have.
   * @param maximumCooldown The new maximum cooldown.
   * @return The object instance.
   */
  public RPGSpawner setMaximumCooldown(int maximumCooldown) {
    dirty = true;
    return this;
  }
  
  /**
   * Sets the minimum cooldown this spawner can have.
   * @param minimumCooldown The new minimum cooldown.
   * @return The object instance.
   */
  public RPGSpawner setMinimumCooldown(int minimumCooldown) {
    dirty = true;
    return this;
  }
  
  /**
   * Sets the radius of this spawner.
   * @param radius The new spawn radius.
   * @return The object instance.
   */
  public RPGSpawner setRadius(int radius) {
    this.radius = radius;
    dirty = true;
    return this;
  }
  
  /**
   * Loads a configuration section
   * @param section The configuration section
   */
  public void load(AeonRPG plugin, ConfigurationSection section) {
    dirty = false;
  }
  
  /**
   * Saves to configuration section
   * @param section The configuration section
   */
  public void save(AeonRPG plugin, ConfigurationSection section) {
    dirty = false;
    
    // Save Location Data
    section.set("Location.X", location.getBlockX());
    section.set("Location.Y", location.getBlockY());
    section.set("Location.Z", location.getBlockZ());
    section.set("Location.World", location.getWorld().getName());
    
    // Radius
    section.set("Radius", radius);
    
    // Cooldown
    section.set("Cooldown.Min", minCooldown);
    section.set("Cooldown.Max", maxCooldown);
    
    // Entity
    section.set("Entity.Max Count", maxEntityCount);
    section.set("Entity.Key", entity);
  }
  
  /**
   * Returns the maximum number of entities that exist in the area around the spawner.
   * @return The maximum number of entities that exist in the area around the spawner.
   */
  public int getMaxEntityCount() {
    return maxEntityCount;
  }
  
  /**
   * Sets the maximum number of entities that exist in the area around the spawner.
   * @param maxEntityCount The new maximum.
   * @return The object instance.
   */
  public RPGSpawner setMaxEntityCount(int maxEntityCount) {
    this.maxEntityCount = maxEntityCount;
    dirty = true;
    return this;
  }
}
