package com.gmail.alexdion93.aeonrpg.data.type;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import com.gmail.alexdion93.aeonrpg.data.interfaces.RPGDataTwoValued;
import com.gmail.alexdion93.aeonrpg.events.RPGSkillExperienceGainEvent;
import com.gmail.alexdion93.aeonrpg.events.RPGSkillLevelUpEvent;

import net.md_5.bungee.api.ChatColor;

/**
 * Represents a skill.
 * @author Alex Dion
 */
public abstract class RPGSkillType extends RPGDataType implements RPGDataTwoValued, Listener {

  private NamespacedKey primary;
  private NamespacedKey secondary;
  
  /**
   * Constructor
   *
   * @param plugin      The target plugin.
   * @param key         The key of this type.
   * @param displayName The display name of this type.
   * @param description The type's description
   */
  public RPGSkillType(JavaPlugin plugin, String key, String displayName, String description) {
    super(plugin, "skill." + key.toLowerCase(), displayName, description);
    primary = new NamespacedKey(plugin, "skill." + key.toLowerCase() + ".level");
    secondary = new NamespacedKey(plugin, "skill." + key.toLowerCase() + ".experience");
  }

  @Override public RPGDataAlignment getAlignment() { return RPGDataAlignment.POSITIVE; }

  /**
   * Returns the amount of experience to level up.
   * @param level The current level
   * @return The amount of experience required to level up.
   */
  public int getExperienceRequired(int level) { return Math.max(level, 1) * 100; }

  /**
   * Returns the greatest level this skill can level.
   * @return The greatest level this skill can level.
   */
  public int getMaxLevel() { return 100; }
  
  @Override
  public int getPrimaryDefaultValue() { return 1; }
  
  @Override
  public NamespacedKey getPrimaryKey() { return primary; }
  
  @Override
  public int getSecondaryDefaultValue() { return 0; }
  
  @Override
  public NamespacedKey getSecondaryKey() { return secondary; }

  /**
   * Gives or takes a set amount of experience from a player.
   * @param player The target player.
   * @param experience The amount of experience being.
   */
  public void giveExperience(Player player, int experience) {
    
    if (player == null) { throw new IllegalArgumentException("Player cannot be null"); }
    
    //Ignore if the player is already at the maximum level
    int level = player.getPersistentDataContainer().getOrDefault(getPrimaryKey(), PersistentDataType.INTEGER, getPrimaryDefaultValue()); 
    if (level >= getMaxLevel()) { return; }
    
    //Call an event with the experience being given.
    RPGSkillExperienceGainEvent xpEvent = new RPGSkillExperienceGainEvent(player, this, experience);
    Bukkit.getPluginManager().callEvent(xpEvent);
    
    //If the event was cancelled we don't need to progress further
    if (xpEvent.isCancelled()) { return; }
    
    //Fetch the player's values
    NamespacedKey key = getPrimaryKey();
    NamespacedKey alt = getSecondaryKey();
    PersistentDataType<Integer, Integer> type = PersistentDataType.INTEGER;
    PersistentDataContainer data = player.getPersistentDataContainer();
    int playerLevel = data.getOrDefault(key, type, 1);
    int playerExperience = data.getOrDefault(alt, type, 0);
    
    //Fetch experience multiplier
    double multiplier = 1 + (getSum(player, getSecondaryKey()) / 100.0);
    
    //Add the experience to the player
    int levelsGained = 0;
    int experienceRequired = Math.max(getExperienceRequired(playerLevel), 1);
    playerExperience += (xpEvent.getExperience() * multiplier);
    
    //Level the player up as much as necessary
    while(playerExperience >= experienceRequired) {
      
      //Increase the level
      levelsGained++;
      
      //Decrease the player's experience
      playerExperience -= experienceRequired;
      
      //Update the experience required
      experienceRequired = Math.max(getExperienceRequired(playerLevel), 1);
      
      //Failsafe
      if (playerLevel + levelsGained >= getMaxLevel()) { break; }
    }
    
    //Call the level up event if needed
    if (levelsGained > 0) {
      RPGSkillLevelUpEvent levelEvent = new RPGSkillLevelUpEvent(player, this, playerLevel, playerLevel + levelsGained);
      Bukkit.getPluginManager().callEvent(levelEvent);
    }
    
    //Update the player's values
    data.set(key, type, Math.min(Math.max(playerLevel + levelsGained, 1), getMaxLevel()));
    data.set(alt, type, Math.max(playerExperience, 0));
  }

  @Override
  public boolean isNegativeAllowed() { return true; }

  /**
   * Triggers when a player levels up.
   * @param event The event being fired.
   */
  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void onLevelUp(RPGSkillLevelUpEvent event) {
    
    //Variables
    Player player = event.getPlayer();
    
    //Make sure this is the right skill
    if (event.getSkill() != this) { return; }
    
    //Send the message
    player.sendMessage(ChatColor.GOLD + "[" + ChatColor.RED + "Level Up" + ChatColor.GOLD + "] "
        + ChatColor.GRAY + "Your " + ChatColor.YELLOW + getDisplayName() + ChatColor.GRAY + " is now level "
        + ChatColor.YELLOW + event.getNewLevel() + ChatColor.GRAY + "!"
        );
    
    //Play level up sound
    Sound sound = (event.getNewLevel() % 5 == 0 ? Sound.UI_TOAST_CHALLENGE_COMPLETE : Sound.ENTITY_PLAYER_LEVELUP);
    player.playSound(player.getLocation(), sound, SoundCategory.MASTER, 1.0f, 1.0f);
  }

  /**
   * Allows the player to have an innate skill value
   *
   * @param event The event being fired.
   */
  @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
  public void onPlayerJoin(PlayerJoinEvent event) {
    Player player = event.getPlayer();
    
    //Set Level
    set(player, getPrimaryKey(), PersistentDataType.INTEGER, getPrimaryDefaultValue());
    
    //Set Experience
    set(player, getSecondaryKey(), PersistentDataType.INTEGER, getSecondaryDefaultValue());
  }

  @Override
  public String toLoreString(Object... values) {
    
    String result = "";
    int level = (values.length >= 1 && values[0] != null ? (int) values[0] : 0);
    int experience = (values.length >= 2 && values[1] != null ? (int) values[1] : 0);
    
    //Level String
    if (level != 0) {
      String color = (level > 0 ? ChatColor.GREEN + "" : ChatColor.RED + "-");
      result += color + level + " " + getDisplayName();
    }
    
    //Experience String
    if (experience != 0) {
      String color = (experience > 0 ? ChatColor.GREEN + "" : ChatColor.RED + "-");
      result += "\n" + color + experience + "% " + getDisplayName() + " experience";
    }
    
    //Return
    return result.trim();
  }
  
  /**
   * Sets the skill's level on the container.
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
   * Sets the skill's level on the holder.
   * @param holder The persistent data holder.
   * @param value The value we're setting this to.
   */
  public void setLevel(PersistentDataHolder holder, int value) {
    setLevel(holder.getPersistentDataContainer(), value);
  }
  
  /**
   * Sets the skill's experience on the holder.
   * @param container The persistent data container.
   * @param value The value we're setting this to.
   */
  public void setExperience(PersistentDataContainer container, int value) {
    if (value == 0 || (value < 0 && !isNegativeAllowed())) {
      remove(container, getSecondaryKey(), PersistentDataType.INTEGER, value);
      return;
    }
    set(container, getSecondaryKey(), PersistentDataType.INTEGER, value);
  }
  
  /**
   * Sets the skill's experience on the holder.
   * @param holder The persistent data holder.
   * @param value The value we're setting this to.
   */
  public void setExperience(PersistentDataHolder holder, int value) {
    setExperience(holder.getPersistentDataContainer(), value);
  }
  
  /**
   * Returns the experience of the skill.
   * @param container The persistent data container.
   * @return The experience of the skill.
   */
  public int getExperience(PersistentDataContainer container) {
    return get(container, getSecondaryKey(), PersistentDataType.INTEGER, 0);
  }

  /**
   * Returns the experience of the skill.
   * @param container The persistent data container.
   * @return The experience of the skill.
   */
  public int getExperience(PersistentDataHolder holder) {
    return getExperience(holder.getPersistentDataContainer());
  }
  
  /**
   * Returns the level of the skill.
   * @param container The persistent data container.
   * @return The level of the skill.
   */
  public int getLevel(PersistentDataContainer container) {
    return get(container, getPrimaryKey(), PersistentDataType.INTEGER, 0);
  }

  /**
   * Returns the level of the skill.
   * @param container The persistent data container.
   * @return The level of the skill.
   */
  public int getLevel(PersistentDataHolder holder) {
    return getLevel(holder.getPersistentDataContainer());
  }
}
