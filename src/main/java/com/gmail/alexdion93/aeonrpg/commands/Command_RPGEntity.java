package com.gmail.alexdion93.aeonrpg.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import com.gmail.alexdion93.aeonrpg.AeonRPG;
import com.gmail.alexdion93.aeonrpg.data.holder.*;
import com.gmail.alexdion93.aeonrpg.data.interfaces.*;
import com.gmail.alexdion93.aeonrpg.data.type.*;
import com.gmail.alexdion93.aeonrpg.managers.GenericRPGTypeManager;
import com.gmail.alexdion93.aeonrpg.util.RPGDataUtil;

/**
 * A command that allows the player to create, remove and modify stored data for
 * entities as well as spawn entities with that data.
 * @author Alex Dion
 *
 */
public class Command_RPGEntity implements CommandExecutor, TabCompleter {
  private AeonRPG plugin;
  private ArrayList<String> rpgKeys;
  private ArrayList<String> entityKeys;

  /**
   * Constructor
   *
   * @param plugin the plugin tied to this
   */
  public Command_RPGEntity(AeonRPG plugin) {
    this.plugin = plugin;
    rpgKeys = new ArrayList<>();
    entityKeys = new ArrayList<>();
    plugin.getCommand("rpgentity").setExecutor(this);
    plugin.getCommand("rpgentity").setTabCompleter(this);
  }
  
  /**
   * Returns any keys that contain the provided value.
   * @param value The value we're searching for
   * @param keys The keys that we're searching through.
   * @return A list containing all keys that matched the criteria
   */
  private List<String> getMatchingKeys(String value, List<String> keys) {
    ArrayList<String> result = new ArrayList<>();
    
    //If no keys were provided return something
    if (keys == null || keys.isEmpty()) { return Arrays.asList("<None>"); }
    
    //If the value is empty return all keys
    if (value == null || value.isEmpty()) { return keys; }
    
    //Filter the keys based on which ones contain the value
    for(String key : keys) {
      if (!key.toLowerCase().contains(value.toLowerCase())) { continue; }
      result.add(key);
    }
    return result;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    
    // rpgentity
    if (args.length == 0) {
      sender.sendMessage(
        ChatColor.RED + "Error!" + ChatColor.GRAY + " Incomplete Command",
        ChatColor.GRAY + "Please provide one of the following arguments",
        ChatColor.GRAY + "/rpgentity " + ChatColor.YELLOW + "create <name>",
        ChatColor.GRAY + "/rpgentity " + ChatColor.YELLOW + "give <name>",
        ChatColor.GRAY + "/rpgentity " + ChatColor.YELLOW + "inspect <name>",
        ChatColor.GRAY + "/rpgentity " + ChatColor.YELLOW + "modify <name> <type>...",
        ChatColor.GRAY + "/rpgentity " + ChatColor.YELLOW + "remove <name>",
        ChatColor.GRAY + "/rpgentity " + ChatColor.YELLOW + "spawn <name>"
        );
      return true;
    }
    
    // rpgentity <operation>
    if (args.length == 1) {
      sender.sendMessage(
        ChatColor.RED + "Error!" + ChatColor.GRAY + " Incomplete Command",
        ChatColor.GRAY + "Please provide an appropriate entity key",
        ChatColor.GRAY + "/rpgentity " + args[0] + ChatColor.YELLOW + "<name>"
        );
      return true;
    }
    
    // Fetch the entity key.
    String key = args[1].toLowerCase();
    
    // rpgentity create <name> <type>
    if (args[0].equalsIgnoreCase("create")) {
      subCmdCreate(sender, args, key);
      return true;
    }
    
    // Fetch the entity
    RPGEntity entity = plugin.getRPGEntityManager().get(key);
    if (entity == null) {
      sender.sendMessage(
        ChatColor.RED + "Error!" + ChatColor.GRAY + " Invalid Entity",
        ChatColor.GRAY + "Failed to find entity with the name \"" + key + "\""
        );
      return true;
    }
    
    // rpgentity modify <name>
    if (args[0].equalsIgnoreCase("modify")) {
      subCmdModify(sender, args, entity, key);
      return true;
    }
    
    // rpgentity give <name>
    if (args[0].equalsIgnoreCase("give")) {
      subCmdGive(sender, entity, key);
      return true;
    }
    
    // rpgentity inspect <name>
    if (args[0].equalsIgnoreCase("inspect")) {
      subCmdInspect(sender, entity, key);
      return true;
    }
    
    // rpgentity remove <name>
    if (args[0].equalsIgnoreCase("remove")) {
      subCmdRemove(sender, key);
      return true;
    }
    
    // rpgentity spawn <name>
    if (args[0].equalsIgnoreCase("spawn")) {
      subCmdSpawn(sender, entity, key);
      return true;
    }
    
    // Exit
    return true;
  }
  
  /**
   * TODO: Inspect subcommand logic
   * @param sender The command sender
   * @param entity The entity being inspected.
   * @param key The rpg entity's map key assigned to the entity
   */
  private void subCmdInspect(CommandSender sender, RPGEntity entity, String key) {
    
    sender.sendMessage(ChatColor.GOLD + "Inspecting " + key);
    entity.inspect(sender);
  }
  
  /**
   * Remove subcommand logic
   * @param sender The command sender
   * @param entity The rpg entity.
   */
  private void subCmdRemove(CommandSender sender, String key) {
    
    //Remove from the map
    plugin.getRPGEntityManager().remove(key);
    
    //Success
    sender.sendMessage(
      ChatColor.GREEN + "Success!" + ChatColor.GRAY + " Removed entity \"" + key + "\""
      );
  }
  
  /**
   * @param sender The command sender
   * @param args The command arguments
   * @param entity The entity being modified.
   * @param key The rpg entity's map key
   */
  private void subCmdModify(CommandSender sender, String[] args, RPGEntity entity, String key) {
    
    //Incomplete command
    if (args.length == 2) {
      sender.sendMessage(
        ChatColor.RED + "Error!" + ChatColor.GRAY + " Incomplete Command",
        ChatColor.GRAY + "Please provide the key for an rpg data type."
        );
      return;
    }
    
    //Fetch the data type
    RPGDataType type = plugin.getRPGDataTypeManager().get(args[2]);
    
    // Failed to find data.
    if (type == null) {
      sender.sendMessage(
        ChatColor.RED + "Error!" + ChatColor.GRAY + " Invalid Key",
        ChatColor.GRAY + "Please provide a valid key for an rpg data type."
        );
      return;
    }
    
    // RPG Attributes
    if (type instanceof RPGAttributeType) {
      subCmdModifyAttribute(sender, key, args, entity, (RPGAttributeType) type);
      return;
    }
    
    // RPG Enchantments
    else if (type instanceof RPGEnchantmentType) {
      subCmdModifyEnchantment(sender, key, args, entity, (RPGEnchantmentType) type);
      return;
    }
    
    // RPG Potion Effects
    else if (type instanceof RPGPotionEffectType) {
      subCmdModifyPotion(sender, key, args, entity, (RPGPotionEffectType) type);
      return;
    }
    
    // RPG Skills
    else if (type instanceof RPGSkillType) {
      subCmdModifySkill(sender, key, args, entity, (RPGSkillType) type);
      return;
    }
    
    //RPG Generics
    else {
      subCmdModifyGeneric(sender, key, args, entity, type);
      return;
    }
  }
  
  /**
   * Modify attribute sub command logic
   * @param sender The command sender
   * @param key The entity key
   * @param args The additional arguments
   * @param entity The target entity.
   * @param type The data type being passed.
   */
  private void subCmdModifyGeneric(CommandSender sender, String key, String[] args, RPGEntity entity, RPGDataType type) {
    
    //Variables
    int primary = 0;
    int secondary = 0;
    String string = null;
    RPGGenericData data = entity.get(type);
    int index = 3;
    
    // One Valued Types
    if (type instanceof RPGDataOneValued) {
      // Check if we have too few values
      if (args.length == index) {
        sender.sendMessage(ChatColor.RED + "Error!" + ChatColor.GRAY + " Incomplete Command",
          ChatColor.GRAY + "Please provide a valid " + ChatColor.YELLOW + "primary" + ChatColor.GRAY + " value.",
          ChatColor.GRAY + "You may use the " + ChatColor.YELLOW + "~" + ChatColor.GRAY + " to prevent changes."
          );
        return;
      }
      
      // Parse the value from the index
      if (args[index] == "~") { primary = (data == null ? 0 : data.getPrimary()); }
      else {
         try { primary = Integer.parseInt(args[index]); }
         catch(NumberFormatException e) {
           sender.sendMessage(
             ChatColor.RED + "Error!" + ChatColor.GRAY + " Parsing Error",
             ChatColor.GRAY + "Failed to parse integer for " + ChatColor.YELLOW + "primary" + ChatColor.GRAY + " value."
             );
           return;
        }
      }
      
      //Increment the index for the next argument
      index++;
    }
    
    // Two Valued Types
    if (type instanceof RPGDataTwoValued) {
      // Check if we have too few values
      if (args.length == index) {
        sender.sendMessage(ChatColor.RED + "Error!" + ChatColor.GRAY + " Incomplete Command",
          ChatColor.GRAY + "Please provide a valid " + ChatColor.YELLOW + "secondary" + ChatColor.GRAY + " value.",
          ChatColor.GRAY + "You may use the " + ChatColor.YELLOW + "~" + ChatColor.GRAY + " to prevent changes."
          );
        return;
      }
      
      // Parse the value from the index
      if (args[index] == "~") { secondary = (data == null ? 0 : data.getSecondary()); }
      else {
         try { secondary = Integer.parseInt(args[index]); }
         catch(NumberFormatException e) {
           sender.sendMessage(
             ChatColor.RED + "Error!" + ChatColor.GRAY + " Parsing Error",
             ChatColor.GRAY + "Failed to parse integer for " + ChatColor.YELLOW + "secondary" + ChatColor.GRAY + " value."
             );
           return;
        }
      }
      
      //Increment the index for the next argument
      index++;
    }
    
    // String Valued Types
    if (type instanceof RPGDataString) {
      // Check if we have too few values
      if (args.length == index) {
        sender.sendMessage(ChatColor.RED + "Error!" + ChatColor.GRAY + " Incomplete Command",
          ChatColor.GRAY + "Please provide a valid " + ChatColor.YELLOW + "string" + ChatColor.GRAY + " value.",
          ChatColor.GRAY + "You may use the " + ChatColor.YELLOW + "~" + ChatColor.GRAY + " to prevent changes."
          );
        return;
      }
      
      // Parse the value from the index
      if (args[index] == "~") { string = (data == null ? null : data.getString()); }
      else { string = String.join(" ", Arrays.copyOfRange(args, index, args.length)); }
      
      //Increment the index for the next argument
      index++;
    }
    
    //Apply the change
    entity.set(type, primary, secondary, string);
  }
  
  /**
   * Modify attribute sub command logic
   * @param sender The command sender
   * @param key The entity key
   * @param args The additional arguments
   * @param entity The target entity.
   * @param type The data type being passed.
   */
  private void subCmdModifyAttribute(CommandSender sender, String key, String[] args, RPGEntity entity, RPGAttributeType type) {
    
    //Variables
    RPGAttributeData data = entity.get(type);
    
    //Attempt to fetch the primary value
    Integer primary = null;
    if (args.length == 3) {
      sender.sendMessage(ChatColor.RED + "Error!" + ChatColor.GRAY + " Incomplete Command",
        ChatColor.GRAY + "Please provide a valid " + ChatColor.YELLOW + "flat" + ChatColor.GRAY + " value.",
        ChatColor.GRAY + "You may use the " + ChatColor.YELLOW + "~" + ChatColor.GRAY + " to prevent changes."
        );
      return;
    }
    
    if (args[3] == "~") { primary = (data == null ? 0 : data.getFlat()); }
    else {
       try { primary = Integer.parseInt(args[3]); }
       catch(NumberFormatException e) {
         sender.sendMessage(
           ChatColor.RED + "Error!" + ChatColor.GRAY + " Parsing Error",
           ChatColor.GRAY + "Failed to parse integer for " + ChatColor.YELLOW + "flat" + ChatColor.GRAY + " value."
           );
         return;
      }
    }
    
    //Attempt to fetch the primary value
    Integer secondary = null;
    if (args.length == 4) {
      sender.sendMessage(ChatColor.RED + "Error!" + ChatColor.GRAY + " Incomplete Command",
        ChatColor.GRAY + "Please provide a valid " + ChatColor.YELLOW + "scaling" + ChatColor.GRAY + " value.",
        ChatColor.GRAY + "You may use the " + ChatColor.YELLOW + "~" + ChatColor.GRAY + " to prevent changes."
        );
      return;
    }
    
    if (args[4] == "~") { secondary = (data == null ? 0 : data.getScaling()); }
    else {
       try { secondary = Integer.parseInt(args[4]); }
       catch(NumberFormatException e) {
         sender.sendMessage(
           ChatColor.RED + "Error!" + ChatColor.GRAY + " Parsing Error",
           ChatColor.GRAY + "Failed to parse integer for " + ChatColor.YELLOW + "scaling" + ChatColor.GRAY + " value."
           );
         return;
      }
    }
    
    //Apply the change
    entity.set(type, primary, secondary);
    
    //Success Message
    sender.sendMessage(
      ChatColor.GREEN + "Success!" + ChatColor.GRAY + " Entity successfully modified"
      );
  }
  
  /**
   * Modify enchantment sub command logic
   * @param sender The command sender
   * @param key The entity key
   * @param args The additional arguments
   * @param entity The target entity.
   * @param type The data type being passed.
   */
  private void subCmdModifyEnchantment(CommandSender sender, String key, String[] args, RPGEntity entity, RPGEnchantmentType type) {
    
    //Variables
    RPGEnchantmentData data = entity.get(type);
    
    //Attempt to fetch the primary value
    Integer primary = null;
    if (args.length == 3) {
      sender.sendMessage(ChatColor.RED + "Error!" + ChatColor.GRAY + " Incomplete Command",
        ChatColor.GRAY + "Please provide a valid " + ChatColor.YELLOW + "level" + ChatColor.GRAY + " value.",
        ChatColor.GRAY + "You may use the " + ChatColor.YELLOW + "~" + ChatColor.GRAY + " to prevent changes."
        );
      return;
    }
    
    if (args[3] == "~") { primary = (data == null ? 0 : data.getLevel()); }
    else {
       try { primary = Integer.parseInt(args[3]); }
       catch(NumberFormatException e) {
         sender.sendMessage(
           ChatColor.RED + "Error!" + ChatColor.GRAY + " Parsing Error",
           ChatColor.GRAY + "Failed to parse integer for " + ChatColor.YELLOW + "level" + ChatColor.GRAY + " value."
           );
         return;
      }
    }
    
    //Apply the change
    entity.set(type, primary);
    
    //Success Message
    sender.sendMessage(
      ChatColor.GREEN + "Success!" + ChatColor.GRAY + " Entity successfully modified"
      );
  }
  
  /**
   * Modify potion sub command logic
   * @param sender The command sender
   * @param key The entity key
   * @param args The additional arguments
   * @param entity The target entity.
   * @param type The data type being passed.
   */
  private void subCmdModifyPotion(CommandSender sender, String key, String[] args, RPGEntity entity, RPGPotionEffectType type) {
    
    //Variables
    RPGPotionEffectData data = entity.get(type);
    
    //Attempt to fetch the primary value
    Integer primary = null;
    if (args.length == 3) {
      sender.sendMessage(ChatColor.RED + "Error!" + ChatColor.GRAY + " Incomplete Command",
        ChatColor.GRAY + "Please provide a valid " + ChatColor.YELLOW + "level" + ChatColor.GRAY + " value.",
        ChatColor.GRAY + "You may use the " + ChatColor.YELLOW + "~" + ChatColor.GRAY + " to prevent changes."
        );
      return;
    }
    
    if (args[3] == "~") { primary = (data == null ? 0 : data.getLevel()); }
    else {
       try { primary = Integer.parseInt(args[3]); }
       catch(NumberFormatException e) {
         sender.sendMessage(
           ChatColor.RED + "Error!" + ChatColor.GRAY + " Parsing Error",
           ChatColor.GRAY + "Failed to parse integer for " + ChatColor.YELLOW + "level" + ChatColor.GRAY + " value."
           );
         return;
      }
    }
    
    //Attempt to fetch the primary value
    Integer secondary = null;
    if (args.length == 4) {
      sender.sendMessage(ChatColor.RED + "Error!" + ChatColor.GRAY + " Incomplete Command",
        ChatColor.GRAY + "Please provide a valid " + ChatColor.YELLOW + "duration" + ChatColor.GRAY + " value.",
        ChatColor.GRAY + "You may use the " + ChatColor.YELLOW + "~" + ChatColor.GRAY + " to prevent changes."
        );
      return;
    }
    
    if (args[4] == "~") { secondary = (data == null ? 0 : data.getDuration()); }
    else {
       try { secondary = Integer.parseInt(args[4]); }
       catch(NumberFormatException e) {
         sender.sendMessage(
           ChatColor.RED + "Error!" + ChatColor.GRAY + " Parsing Error",
           ChatColor.GRAY + "Failed to parse integer for " + ChatColor.YELLOW + "duration" + ChatColor.GRAY + " value."
           );
         return;
      }
    }
    
    //Apply the change
    entity.set(type, primary, secondary);
    
    //Success Message
    sender.sendMessage(
      ChatColor.GREEN + "Success!" + ChatColor.GRAY + " Entity successfully modified"
      );
  }
  
  /**
   * Modify potion sub command logic
   * @param sender The command sender
   * @param key The entity key
   * @param args The additional arguments
   * @param entity The target entity.
   * @param type The data type being passed.
   */
  private void subCmdModifySkill(CommandSender sender, String key, String[] args, RPGEntity entity, RPGSkillType type) {
    
    //Variables
    RPGSkillData data = entity.get(type);
    
    //Attempt to fetch the primary value
    Integer primary = null;
    if (args.length == 3) {
      sender.sendMessage(ChatColor.RED + "Error!" + ChatColor.GRAY + " Incomplete Command",
        ChatColor.GRAY + "Please provide a valid " + ChatColor.YELLOW + "level" + ChatColor.GRAY + " value.",
        ChatColor.GRAY + "You may use the " + ChatColor.YELLOW + "~" + ChatColor.GRAY + " to prevent changes."
        );
      return;
    }
    
    if (args[3] == "~") { primary = (data == null ? 0 : data.getLevel()); }
    else {
       try { primary = Integer.parseInt(args[3]); }
       catch(NumberFormatException e) {
         sender.sendMessage(
           ChatColor.RED + "Error!" + ChatColor.GRAY + " Parsing Error",
           ChatColor.GRAY + "Failed to parse integer for " + ChatColor.YELLOW + "level" + ChatColor.GRAY + " value."
           );
         return;
      }
    }
    
    //Attempt to fetch the primary value
    Integer secondary = null;
    if (args.length == 4) {
      sender.sendMessage(ChatColor.RED + "Error!" + ChatColor.GRAY + " Incomplete Command",
        ChatColor.GRAY + "Please provide a valid " + ChatColor.YELLOW + "experience" + ChatColor.GRAY + " value.",
        ChatColor.GRAY + "You may use the " + ChatColor.YELLOW + "~" + ChatColor.GRAY + " to prevent changes."
        );
      return;
    }
    
    if (args[4] == "~") { secondary = (data == null ? 0 : data.getExperience()); }
    else {
       try { secondary = Integer.parseInt(args[4]); }
       catch(NumberFormatException e) {
         sender.sendMessage(
           ChatColor.RED + "Error!" + ChatColor.GRAY + " Parsing Error",
           ChatColor.GRAY + "Failed to parse integer for " + ChatColor.YELLOW + "experience" + ChatColor.GRAY + " value."
           );
         return;
      }
    }
    
    //Apply the change
    entity.set(type, primary, secondary);
    
    //Success Message
    sender.sendMessage(
      ChatColor.GREEN + "Success!" + ChatColor.GRAY + " Entity successfully modified"
      );
  }

  /**
   * Create subcommand logic
   * @param sender The command sender
   * @param args The command arguments
   * @param key The rpg entity's map key
   */
  private void subCmdCreate(CommandSender sender, String[] args, String key) {
    // rpgentity create <name>
    if (args.length == 2) {
      sender.sendMessage(
        ChatColor.RED + "Error!" + ChatColor.GRAY + " Incomplete Command",
        ChatColor.GRAY + "Please provide an entity type."
        );
      return;
    }
    
    // Exit if already registered
    if (plugin.getRPGEntityManager().has(args[1])) {
      sender.sendMessage(
        ChatColor.RED + "Error!" + ChatColor.GRAY + " Already Registered",
        ChatColor.GRAY + "Please use a different name for this entity."
        );
      return;
    }
    
    // Fetch the entity type
    EntityType type = null;
    try { type = EntityType.valueOf(args[2].toUpperCase());} catch(Exception e) { /* Ignore */ }
    
    // Exit if no entity type is found
    if (type == null) {
      sender.sendMessage(
        ChatColor.RED + "Error!" + ChatColor.GRAY + " Invalid Entity Type",
        ChatColor.GRAY + "Failed to find entity type \"" + args[2] + "\""
        );
      return;
    }
    
    //Success
    plugin.getRPGEntityManager().add(key, new RPGEntity(plugin, type));
    sender.sendMessage(
      ChatColor.GREEN + "Success!" + ChatColor.GRAY + " Added new rpg entity \"" + key + "\""
      );
  }
  
  /**
   * Give subcommand logic
   * @param sender The command sender
   * @param entity The rpg entity.
   */
  private void subCmdGive(CommandSender sender, RPGEntity entity, String key) {
    // Exit if no player
    Player player = (sender instanceof Player ? (Player) sender : null);
    if (player == null) {
      sender.sendMessage(
        ChatColor.RED + "Error!" + ChatColor.GRAY + " Player Only Command",
        ChatColor.GRAY + "This sub command requires a player command sender."
        );
      return;
    }
    
    // Exit if no item being held
    ItemStack item = player.getInventory().getItemInMainHand();
    if (item == null) {
      sender.sendMessage(
        ChatColor.RED + "Error!" + ChatColor.GRAY + " Invalid Item",
        ChatColor.GRAY + "Please hold an item."
        );
      return;
    }
    
    // Exit if invalid item meta
    ItemMeta meta = item.getItemMeta();
    if (meta == null) {
      sender.sendMessage(
        ChatColor.RED + "Error!" + ChatColor.GRAY + " Invalid Item",
        ChatColor.GRAY + "Please hold a valid item."
        );
      return;
    }
    
    //Success
    entity.apply(meta);
    meta.setLore(RPGDataUtil.generateLore(meta.getPersistentDataContainer()));
    item.setItemMeta(meta);
    sender.sendMessage(
      ChatColor.GREEN + "Success!" + ChatColor.GRAY + " Created \"" + key + "\" item"
      );
  }
  
  /**
   * Spawn subcommand logic
   * @param sender The command sender
   * @param entity The rpg entity.
   */
  private void subCmdSpawn(CommandSender sender, RPGEntity entity, String key) {
    // Exit if no player
    Player player = (sender instanceof Player ? (Player) sender : null);
    if (player == null) {
      sender.sendMessage(
        ChatColor.RED + "Error!" + ChatColor.GRAY + " Player Only Command",
        ChatColor.GRAY + "This sub command requires a player command sender"
        );
      return;
    }
    
    // Get the location
    Location location = player.getTargetBlock(null, 10).getLocation().add(0.5, 0, 0.5);
    
    //Adjust for non passible blocks
    if (!location.getBlock().isPassable()) {
      location = location.add(0, 1, 0);
    }
    
    //Success
    entity.spawn(location);
    sender.sendMessage(
      ChatColor.GREEN + "Success!" + ChatColor.GRAY + " Spawned \"" + key + "\""
      );
  }

  @Override
  public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
    // Variables
    updateKeys();
    sender.sendMessage(String.format("[%d] %s", args.length, String.join(", ", args)));
    
    // rpgentity <operation>
    if (args.length == 1) {
      return Arrays.asList("create", "give", "inspect", "modify", "remove", "spawn");
    }
    
    // rpgentity create <name> <type>
    if (args.length >= 2 && args[0].equalsIgnoreCase("create")) {
      
      // rpgentity create <name>
      if (args.length == 2) { return Arrays.asList("<Key>"); }
      
      // rpgentity create <name> <type>
      if (args.length == 3) {
        List<String> results = new ArrayList<String>();
        for(EntityType type : EntityType.values()) { results.add(type.name().toLowerCase()); }
        return getMatchingKeys(args[2], results);
      }
    }
    
    // rpgentity modify <name> <key> <value>
    if (args.length >= 2 && args[0].equalsIgnoreCase("modify")) {
      
      // rpgentity modify <name>
      if (args.length == 2) { return getMatchingKeys(args[1], entityKeys); }
      
      // rpgentity modify <name> <key>
      if (args.length == 3) { return getMatchingKeys(args[2], rpgKeys); }
      
      // rpgentity modify <name> <key> <value>
      RPGDataType type = plugin.getRPGDataTypeManager().get(args[2]);
      if (type == null) { return new ArrayList<>(); }
      
      // RPG Attributes
      // rpgitem modify <name> <key> <flat> <scaling>
      if (type instanceof RPGAttributeType) {
        if (args.length == 4) { return Arrays.asList("<flat>"); }
        if (args.length == 5) { return Arrays.asList("<scaling>"); }
        return new ArrayList<>();
      }
      
      // RPG Enchantments
      // rpgitem modify <name> <key> <level>
      else if (type instanceof RPGEnchantmentType) {
        if (args.length == 4) { return Arrays.asList("<level>"); }
        return new ArrayList<>();
      }
      
      // RPG Potion Effects
      // rpgitem modify <name> <key> <level> <duration>
      else if (type instanceof RPGPotionEffectType) {
        if (args.length == 4) { return Arrays.asList("<level>"); }
        if (args.length == 5) { return Arrays.asList("<duration>"); }
        return new ArrayList<>();
      }
      
      // RPG Skills
      // rpgitem modify <name> <key> <level> <experience>
      else if (type instanceof RPGSkillType) {
        if (args.length == 4) { return Arrays.asList("<level>"); }
        if (args.length == 5) { return Arrays.asList("<experience>"); }
        return new ArrayList<>();
      }
      
      //RPG Generics
      // rpgitem modify <name> <key> <values...>
      else {
        // RPG Two Valued
        if (type instanceof RPGDataTwoValued) {
          if (args.length == 4) { return Arrays.asList("<integer>"); }
          if (args.length == 5) { return Arrays.asList("<integer>"); }
          
          // If this is also a RPGDataString
          if (type instanceof RPGDataString) {
            if (args.length == 6) { return Arrays.asList("<string>"); }
          }
        }
        
        // RPG One Valued
        else if (type instanceof RPGDataOneValued) {
          if (args.length == 4) { return Arrays.asList("<integer>"); }
          
          // If this is also a RPGDataString
          if (type instanceof RPGDataString) {
            if (args.length == 5) { return Arrays.asList("<string>"); }
          }
        }
        
        // RPG Data String
        else if (type instanceof RPGDataString) {
          if (args.length == 4) { return Arrays.asList("<string>"); }
        }
      }
    }
    
    // rpgentity give <name>
    if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
      return getMatchingKeys(args[1], entityKeys);
    }
    
    // rpgentity inspect <name>
    if (args.length == 2 && args[0].equalsIgnoreCase("inspect")) {
      return getMatchingKeys(args[1], entityKeys);
    }
    
    // rpgentity remove <name>
    if (args.length == 2 && args[0].equalsIgnoreCase("remove")) {
      return getMatchingKeys(args[1], entityKeys);
    }
    
    // rpgentity spawn <name>
    if (args.length == 2 && args[0].equalsIgnoreCase("spawn")) {
      return getMatchingKeys(args[1], entityKeys);
    }
    
    //End
    return new ArrayList<>();
  }
  
  /**
   * Updates the keys after everything is registered.
   */
  public void updateKeys() {
    
    if (!rpgKeys.isEmpty()) { return; }
    
    //Fetch all the data keys available
    for(GenericRPGTypeManager<?> manager : plugin.getRPGDataTypeManager().getManagers()) {
      rpgKeys.addAll(manager.getKeys());
    }
    
    //Fetch all the entity keys
    entityKeys.addAll(plugin.getRPGEntityManager().getKeys());
    
    //Sort for easy reading
    Collections.sort(rpgKeys);
    Collections.sort(entityKeys);
  }
}
