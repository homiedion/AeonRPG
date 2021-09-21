package com.gmail.alexdion93.aeonrpg.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;

import com.gmail.alexdion93.aeonrpg.AeonRPG;
import com.gmail.alexdion93.aeonrpg.data.interfaces.RPGDataOneValued;
import com.gmail.alexdion93.aeonrpg.data.interfaces.RPGDataString;
import com.gmail.alexdion93.aeonrpg.data.interfaces.RPGDataTwoValued;
import com.gmail.alexdion93.aeonrpg.data.type.RPGAttributeType;
import com.gmail.alexdion93.aeonrpg.data.type.RPGDataType;
import com.gmail.alexdion93.aeonrpg.data.type.RPGEnchantmentType;
import com.gmail.alexdion93.aeonrpg.data.type.RPGPotionEffectType;
import com.gmail.alexdion93.aeonrpg.data.type.RPGSkillType;
import com.gmail.alexdion93.aeonrpg.data.type.RPGDataType.RPGDataAlignment;
import com.gmail.alexdion93.aeonrpg.managers.GenericRPGTypeManager;

/**
 * A command that allows the player to create, remove and modify stored data for
 * entities as well as spawn entities with that data.
 * @author Alex Dion
 *
 */
public class Command_RPGPlayer implements CommandExecutor, TabCompleter {
  private AeonRPG plugin;
  private ArrayList<String> keys;

  /**
   * Constructor
   *
   * @param plugin the plugin tied to this
   */
  public Command_RPGPlayer(AeonRPG plugin) {
    this.plugin = plugin;
    keys = new ArrayList<>();
    plugin.getCommand("rpgplayer").setExecutor(this);
    plugin.getCommand("rpgplayer").setTabCompleter(this);
  }
  
  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    
    // <command> 
    if (args.length == 0) {
      sender.sendMessage(ChatColor.RED + "Error!" + ChatColor.GRAY + " Incomplete Command",
        ChatColor.GRAY + "Please provide a player's name."
        );
      return true;
    }
    
    //Fetch the player
    Player player = Bukkit.getPlayer(args[0]);
    if (player == null) {
      sender.sendMessage(ChatColor.RED + "Error!" + ChatColor.GRAY + " Invalid Player",
        ChatColor.GRAY + "Please provide a valid player"
        );
      return true;
    }
    
    // <command> <player>
    if (args.length == 1) {
      sender.sendMessage(ChatColor.RED + "Error!" + ChatColor.GRAY + " Incomplete Command",
        ChatColor.GRAY + "Try one of the following...",
        ChatColor.YELLOW + "  /" + command.getName() + ChatColor.GRAY + " <player> inspect",
        ChatColor.YELLOW + "  /" + command.getName() + ChatColor.GRAY + " <player> set <key> <value>"
        );
      return true;
    }
    
    // <command> <player> inspect
    if (args[1].equalsIgnoreCase("inspect")) {
      subCmdInspect(sender, player);
      return true;
    }
    
    // <command> <player> set <key> <value>
    if (args[1].equalsIgnoreCase("set")) {
      
      //Not enough arguments
      if (args.length < 4) {
        sender.sendMessage(
          ChatColor.RED + "Error!" + ChatColor.GRAY + " Incomplete Command",
          ChatColor.GRAY + "Please provide a " + ChatColor.YELLOW + "key" + ChatColor.GRAY
            + " and a " + ChatColor.YELLOW + "value" + ChatColor.GRAY + ".",
          ChatColor.YELLOW + "  /" + command.getName() + ChatColor.GRAY + " set <key> <value>"
          );
      }
      
      //Ensure the type is valid
      String key = args[2];
      RPGDataType type = plugin.getRPGDataTypeManager().get(key);
      if (type == null) {
        sender.sendMessage(
          ChatColor.RED + "Error!" + ChatColor.GRAY + " Invalid Key",
          ChatColor.GRAY + "Please provide a valid " + ChatColor.YELLOW + "key" + ChatColor.GRAY + ".",
          ChatColor.YELLOW + "  /" + command.getName() + ChatColor.GRAY + " set <key> <value>"
          );
      }
      
      // RPG Attributes
      if (type instanceof RPGAttributeType) {
        subCmdModifyAttribute(sender, key, args, player, (RPGAttributeType) type);
        return true;
      }
      
      // RPG Enchantments
      else if (type instanceof RPGEnchantmentType) {
        subCmdModifyEnchantment(sender, key, args, player, (RPGEnchantmentType) type);
        return true;
      }
      
      // RPG Potion Effects
      else if (type instanceof RPGPotionEffectType) {
        subCmdModifyPotion(sender, key, args, player, (RPGPotionEffectType) type);
        return true;
      }
      
      // RPG Skills
      else if (type instanceof RPGSkillType) {
        subCmdModifySkill(sender, key, args, player, (RPGSkillType) type);
        return true;
      }
      
      //RPG Generics
      else {
        subCmdModifyGeneric(sender, key, args, player, type);
        return true;
      }
    }
    
    // Exit
    return true;
  }

  @Override
  public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
    // Variables
    updateKeys();
    
    // rpgplayer <player>
    if (args.length == 1) {
      List<String> result = new ArrayList<String>();
      for(Player player : Bukkit.getOnlinePlayers()) {
        if (args[0].isEmpty() && !player.getName().toLowerCase().contains(args[0])) { continue; }
        result.add(player.getName());
      }
      return result;
    }
    
    // rpgplayer <player> <operation>
    if (args.length == 2) {
      List<String> result = new ArrayList<String>();
      for(String option : Arrays.asList("set", "inspect")) {
        if (args[1].isEmpty() && !option.toLowerCase().contains(args[1])) { continue; }
        result.add(option);
      }
      return result;
    }
    
    // rpgplayer <player> set <key>
    if (args.length == 3 && args[1].equalsIgnoreCase("set")) {
      List<String> result = new ArrayList<String>();
      
      for(String key : keys) {
        if (!args[2].isEmpty() && key.toLowerCase().contains(args[2])) { continue; }
        result.add(key);
      }
      
      return result;
    }
    
    // rpgplayer <player> set <key> <value>
    if (args.length >= 4 && args[1].equalsIgnoreCase("set")) {
      RPGDataType type = plugin.getRPGDataTypeManager().get(args[2]);
      if (type == null) { return new ArrayList<String>(); }
      
      // RPG Attributes
      // rpgplayer <player> set <key> <flat> <scaling>
      if (type instanceof RPGAttributeType) {
        if (args.length == 4) { return Arrays.asList("<flat>"); }
        if (args.length == 5) { return Arrays.asList("<scaling>"); }
        return new ArrayList<String>();
      }
      
      // RPG Enchantments
      // rpgplayer <player> set <key> <level>
      else if (type instanceof RPGEnchantmentType) {
        if (args.length == 4) { return Arrays.asList("<level>"); }
        return new ArrayList<String>();
      }
      
      // RPG Potion Effects
      // rpgplayer <player> set <key> <level> <duration>
      else if (type instanceof RPGPotionEffectType) {
        if (args.length == 4) { return Arrays.asList("<level>"); }
        if (args.length == 5) { return Arrays.asList("<duration>"); }
        return new ArrayList<String>();
      }
      
      // RPG Skills
      // rpgplayer <player> set <key> <level> <experience>
      else if (type instanceof RPGSkillType) {
        if (args.length == 4) { return Arrays.asList("<level>"); }
        if (args.length == 5) { return Arrays.asList("<experience>"); }
        return new ArrayList<String>();
      }
      
      //RPG Generics
      // rpgplayer <player> set <key> <values...>
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
    
    //No further arguments
    return new ArrayList<String>();
  }
  
    /**
     * Inspects the data on a container.
     * @param sender The command sender.
     * @param holder The persistent data holder.
     */
    private void subCmdInspect(CommandSender sender, PersistentDataHolder holder) {
      
      PersistentDataContainer data = holder.getPersistentDataContainer();
      sender.sendMessage(ChatColor.GOLD + "" + ChatColor.UNDERLINE + "        Inspection        ");
      
      //Attributes
      sender.sendMessage(ChatColor.GOLD + "  Attributes:");
      for(RPGAttributeType type : plugin.getRPGDataTypeManager().getAttributeManager().getTypes()) {
        
        int flat = type.getFlat(data);
        int scaling = type.getScaling(data);
        
        if (flat == 0 && scaling == 0) { continue; }
        
        boolean isNegative = (type.getAlignment() == RPGDataAlignment.NEGATIVE);
        String positive = (isNegative ? ChatColor.RED : ChatColor.GREEN) + "+";
        String negative = (isNegative ? ChatColor.GREEN : ChatColor.RED) + "";
        
        sender.sendMessage("    " + ChatColor.YELLOW + type.getDisplayName() + ": " + ChatColor.WHITE +
            (flat > 0 ? positive : negative) + flat + ChatColor.WHITE
            + " & " + (scaling > 0 ? positive : negative) + scaling + "%");
      }
      
      //Enchantments
      sender.sendMessage(ChatColor.GOLD + "  Enchantments:");
      for(RPGEnchantmentType type : plugin.getRPGDataTypeManager().getEnchantmentManager().getTypes()) {
        
        int level = type.getLevel(data);
        
        if (level == 0) { continue; }
        
        boolean isNegative = (type.getAlignment() == RPGDataAlignment.NEGATIVE);
        String positive = (isNegative ? ChatColor.RED : ChatColor.GREEN) + "+";
        String negative = (isNegative ? ChatColor.GREEN : ChatColor.RED) + "";
        
        sender.sendMessage("    " + ChatColor.YELLOW + type.getDisplayName() + ": " + ChatColor.WHITE +
            (level > 0 ? positive : negative) + "Lv." + level + ChatColor.WHITE);
      }
      
      //Potion Effects
      sender.sendMessage(ChatColor.GOLD + "  Potion Effects:");
      for(RPGPotionEffectType type : plugin.getRPGDataTypeManager().getPotionEffectManager().getTypes()) {
        
        int level = type.getLevel(data);
        int duration = type.getDuration(data);
        
        if (level == 0 && duration == 0) { continue; }
        
        boolean isNegative = (type.getAlignment() == RPGDataAlignment.NEGATIVE);
        String positive = (isNegative ? ChatColor.RED : ChatColor.GREEN) + "+";
        String negative = (isNegative ? ChatColor.GREEN : ChatColor.RED) + "";
        
        sender.sendMessage("    " + ChatColor.YELLOW + type.getDisplayName() + ": " + ChatColor.WHITE +
            (level > 0 ? positive : negative) + "Lv." + level + ChatColor.WHITE
            + " & " + (duration > 0 ? positive : negative) + duration + "s");
      }
      
      //Skills
      sender.sendMessage(ChatColor.GOLD + "  Skills:");
      for(RPGSkillType type : plugin.getRPGDataTypeManager().getSkillManager().getTypes()) {
        
        int level = type.getLevel(data);
        int experience = type.getExperience(data);
        
        if (level == 0 && experience == 0) { continue; }
        
        int required = type.getExperienceRequired(level);
        boolean isNegative = (type.getAlignment() == RPGDataAlignment.NEGATIVE);
        String positive = (isNegative ? ChatColor.RED : ChatColor.GREEN) + "+";
        String negative = (isNegative ? ChatColor.GREEN : ChatColor.RED) + "";
        
        sender.sendMessage("    " + ChatColor.YELLOW + type.getDisplayName() + ": " + ChatColor.WHITE +
            (level > 0 ? positive : negative) + "Lv." + level + ChatColor.WHITE
            + " & " + (experience > 0 ? positive : negative) + experience + " / " + required);
      }
    }
  
    /**
     * Sub Command for modifying attributes
     * @param sender The command sender.
     * @param key The key being used.
     * @param args The arguments passed.
     * @param player The player being modified.
     * @param type The rpg attribute.
     */
    private void subCmdModifyAttribute(CommandSender sender, String key, String[] args, Player player, RPGAttributeType type) {
      
      //Variables
      PersistentDataContainer data = player.getPersistentDataContainer();
      
      //Attempt to parse out the primary value
      Integer primary = null;
      if (args.length == 3) {
        sender.sendMessage(ChatColor.RED + "Error!" + ChatColor.GRAY + " Incomplete Command",
          ChatColor.GRAY + "Please provide a valid " + ChatColor.YELLOW + "flat" + ChatColor.GRAY + " value.",
          ChatColor.GRAY + "You may use the " + ChatColor.YELLOW + "~" + ChatColor.GRAY + " to prevent changes."
          );
        return;
      }
      
      if (args[3] == "~") {
        primary = data.getOrDefault(type.getPrimaryKey(), PersistentDataType.INTEGER, 0);
      }
      else {
         try {
           primary = Integer.parseInt(args[3]);
         }
         catch(NumberFormatException e) {
           sender.sendMessage(
             ChatColor.RED + "Error!" + ChatColor.GRAY + " Parsing Error",
             ChatColor.GRAY + "Failed to parse integer for " + ChatColor.YELLOW + "flat" + ChatColor.GRAY + " value."
             );
           return;
         }
      }
      
      //Attempt to parse out the secondary value
      Integer secondary = null;
      if (args.length == 4) {
        sender.sendMessage(
          ChatColor.RED + "Error!" + ChatColor.GRAY + " Incomplete Command",
          ChatColor.GRAY + "Please provide a valid " + ChatColor.YELLOW + "scaling" + ChatColor.GRAY + " value."
          );
        return;
      }
      
      if (args[4] == "~") {
        secondary = data.getOrDefault(type.getSecondaryKey(), PersistentDataType.INTEGER, 0);
      }
      else {
         try {
           secondary = Integer.parseInt(args[4]);
         }
         catch(NumberFormatException e) {
           sender.sendMessage(
             ChatColor.RED + "Error!" + ChatColor.GRAY + " Parsing Error",
             ChatColor.GRAY + "Failed to parse integer for " + ChatColor.YELLOW + "scaling" + ChatColor.GRAY + " value."
             );
           return;
         }
      }
      
      //Make the change
      type.setFlat(data, primary);
      type.setScaling(data, secondary);
      
      //Success Message
      sender.sendMessage(
        ChatColor.GREEN + "Success!" + ChatColor.GRAY + " Player successfully modified"
        );
    }
    
  /**
   * Sub Command for modifying enchantments
   * @param sender The command sender.
   * @param key The key being used.
   * @param args The arguments passed.
   * @param player The player being modified.
   * @param type The rpg enchantment.
   */
  private void subCmdModifyEnchantment(CommandSender sender, String key, String[] args, Player player, RPGEnchantmentType type) {
    
    //Variables
    PersistentDataContainer data = player.getPersistentDataContainer();
    
    //Attempt to parse out the primary value
    Integer primary = null;
    if (args.length == 3) {
      sender.sendMessage(ChatColor.RED + "Error!" + ChatColor.GRAY + " Incomplete Command",
        ChatColor.GRAY + "Please provide a valid " + ChatColor.YELLOW + "level" + ChatColor.GRAY + " value.",
        ChatColor.GRAY + "You may use the " + ChatColor.YELLOW + "~" + ChatColor.GRAY + " to prevent changes."
        );
      return;
    }
    
    if (args[3] == "~") {
      primary = data.getOrDefault(type.getPrimaryKey(), PersistentDataType.INTEGER, 0);
    }
    else {
       try {
         primary = Integer.parseInt(args[3]);
       }
       catch(NumberFormatException e) {
         sender.sendMessage(
           ChatColor.RED + "Error!" + ChatColor.GRAY + " Parsing Error",
           ChatColor.GRAY + "Failed to parse integer for " + ChatColor.YELLOW + "level" + ChatColor.GRAY + " value."
           );
         return;
       }
    }
    
    //Make the change
    type.setLevel(data, primary);
    
    //Success Message
    sender.sendMessage(
      ChatColor.GREEN + "Success!" + ChatColor.GRAY + " Player successfully modified"
      );
  }
  
  /**
   * Sub Command for modifying generics
   * @param sender The command sender.
   * @param key The key being used.
   * @param args The arguments passed.
   * @param player The player being modified.
   * @param type The rpg data type.
   */
  private void subCmdModifyGeneric(CommandSender sender, String key, String[] args, Player player, RPGDataType type) {
    
    PersistentDataContainer data = player.getPersistentDataContainer();
    Integer primary = null;
    Integer secondary = null;
    String str = null;
    
    // <command> set <key> <value>
    if (type instanceof RPGDataTwoValued) {
      
      RPGDataTwoValued alt = (RPGDataTwoValued) type;
      
      //Fetch the primary value
      if (args.length == 3) {
        sender.sendMessage(
          ChatColor.RED + "Error!" + ChatColor.GRAY + " Incomplete Command",
          ChatColor.GRAY + "Please provide a valid " + ChatColor.YELLOW + "primary" + ChatColor.GRAY + " value.",
          ChatColor.GRAY + "You may use the " + ChatColor.YELLOW + "~" + ChatColor.GRAY + " to prevent changes."
          );
        return;
      }
      
      if (args[3] == "~") { primary = data.getOrDefault(alt.getPrimaryKey(), PersistentDataType.INTEGER, 0); }
      else {
         try { primary = Integer.parseInt(args[3]); }
         catch(NumberFormatException e) {
           sender.sendMessage(
             ChatColor.RED + "Error!" + ChatColor.GRAY + " Parsing Error",
             ChatColor.GRAY + "Failed to parse integer for " + ChatColor.YELLOW + "primary" + ChatColor.GRAY + " value."
             );
           return;
         }
      }
      
      //Fetch the secondary value
      if (args.length == 4) {
        sender.sendMessage(
          ChatColor.RED + "Error!" + ChatColor.GRAY + " Incomplete Command",
          ChatColor.GRAY + "Please provide a valid " + ChatColor.YELLOW + "secondary" + ChatColor.GRAY + " value.",
          ChatColor.GRAY + "You may use the " + ChatColor.YELLOW + "~" + ChatColor.GRAY + " to prevent changes."
          );
        return;
      }
      
      if (args[4] == "~") { secondary = data.getOrDefault(alt.getSecondaryKey(), PersistentDataType.INTEGER, 0); }
      else {
         try { secondary = Integer.parseInt(args[4]); }
         catch(NumberFormatException e) {
           sender.sendMessage(
             ChatColor.RED + "Error!" + ChatColor.GRAY + " Parsing Error",
             ChatColor.GRAY + "Failed to parse integer for " + ChatColor.YELLOW + "secondary" + ChatColor.GRAY + " value."
             );
           return;
         }
      }
      
      // If this is also a RPGDataString
      if (type instanceof RPGDataString) {
        RPGDataString strType = (RPGDataString) type;
        NamespacedKey strKey = strType.getStringKey();
        int start = 5;
        
        //Fetch the string value
        if (args.length == start) {
          sender.sendMessage(
            ChatColor.RED + "Error!" + ChatColor.GRAY + " Incomplete Command",
            ChatColor.GRAY + "Please provide a valid " + ChatColor.YELLOW + "string" + ChatColor.GRAY + " value.",
            ChatColor.GRAY + "You may use the " + ChatColor.YELLOW + "~" + ChatColor.GRAY + " to prevent changes."
            );
          return;
        }
        
        if (args[start] == "~") { str = data.getOrDefault(strKey, PersistentDataType.STRING, null); }
        else { str = String.join(" ", Arrays.copyOfRange(args, start, args.length)); }
        
        //Set string value
        if (str == null) { type.remove(data, strKey, PersistentDataType.STRING, null); } 
        else { type.set(data, strKey, PersistentDataType.STRING, str); }
      }
      
      //Set primary value
      if (primary == 0) { type.remove(data, alt.getPrimaryKey(), PersistentDataType.INTEGER, 0); } 
      else { type.set(data, alt.getPrimaryKey(), PersistentDataType.INTEGER, primary); }
      
      //Set secondary value
      if (secondary == 0) { type.remove(data, alt.getSecondaryKey(), PersistentDataType.INTEGER, 0); } 
      else { type.set(data, alt.getSecondaryKey(), PersistentDataType.INTEGER, secondary); }
    }
    
    // RPG One Valued
    else if (type instanceof RPGDataOneValued) {
      
      RPGDataOneValued alt = (RPGDataOneValued) type;
      
      //Fetch the primary value
      if (args.length == 3) {
        sender.sendMessage(
          ChatColor.RED + "Error!" + ChatColor.GRAY + " Incomplete Command",
          ChatColor.GRAY + "Please provide a valid " + ChatColor.YELLOW + "primary" + ChatColor.GRAY + " value.",
          ChatColor.GRAY + "You may use the " + ChatColor.YELLOW + "~" + ChatColor.GRAY + " to prevent changes."
          );
        return;
      }
      
      if (args[3] == "~") { primary = data.getOrDefault(alt.getPrimaryKey(), PersistentDataType.INTEGER, 0); }
      else {
         try { primary = Integer.parseInt(args[3]); }
         catch(NumberFormatException e) {
           sender.sendMessage(
             ChatColor.RED + "Error!" + ChatColor.GRAY + " Parsing Error",
             ChatColor.GRAY + "Failed to parse integer for " + ChatColor.YELLOW + "primary" + ChatColor.GRAY + " value."
             );
           return;
         }
      }
      
      // If this is also a RPGDataString
      if (type instanceof RPGDataString) {
        RPGDataString strType = (RPGDataString) type;
        NamespacedKey strKey = strType.getStringKey();
        int start = 4;
        
        //Fetch the string value
        if (args.length == start) {
          sender.sendMessage(
            ChatColor.RED + "Error!" + ChatColor.GRAY + " Incomplete Command",
            ChatColor.GRAY + "Please provide a valid " + ChatColor.YELLOW + "string" + ChatColor.GRAY + " value.",
            ChatColor.GRAY + "You may use the " + ChatColor.YELLOW + "~" + ChatColor.GRAY + " to prevent changes."
            );
          return;
        }
        
        if (args[start] == "~") { str = data.getOrDefault(strKey, PersistentDataType.STRING, null); }
        else { str = String.join(" ", Arrays.copyOfRange(args, start, args.length)); }
        
        //Set string value
        if (str == null) { type.remove(data, strKey, PersistentDataType.STRING, null); } 
        else { type.set(data, strKey, PersistentDataType.STRING, str); }
      }
    }
    
    // RPG Data String
    else if (type instanceof RPGDataString) {
      RPGDataString strType = (RPGDataString) type;
      NamespacedKey strKey = strType.getStringKey();
      int start = 3;
      
      //Fetch the string value
      if (args.length == start) {
        sender.sendMessage(
          ChatColor.RED + "Error!" + ChatColor.GRAY + " Incomplete Command",
          ChatColor.GRAY + "Please provide a valid " + ChatColor.YELLOW + "string" + ChatColor.GRAY + " value.",
          ChatColor.GRAY + "You may use the " + ChatColor.YELLOW + "~" + ChatColor.GRAY + " to prevent changes."
          );
        return;
      }
      
      if (args[start] == "~") { str = data.getOrDefault(strKey, PersistentDataType.STRING, null); }
      else { str = String.join(" ", Arrays.copyOfRange(args, start, args.length)); }
      
      //Set string value
      if (str == null) { type.remove(data, strKey, PersistentDataType.STRING, null); } 
      else { type.set(data, strKey, PersistentDataType.STRING, str); }
    }
    
    //Success Message
    sender.sendMessage(
      ChatColor.GREEN + "Success!" + ChatColor.GRAY + " Player successfully modified"
      );
  }
  
  /**
   * Sub Command for modifying potion effects
   * @param sender The command sender.
   * @param key The key being used.
   * @param args The arguments passed.
   * @param player The player being modified.
   * @param type The rpg potion effect.
   */
  private void subCmdModifyPotion(CommandSender sender, String key, String[] args, Player player, RPGPotionEffectType type) {
    
    //Variables
    PersistentDataContainer data = player.getPersistentDataContainer();
    
    //Attempt to parse out the primary value
    Integer primary = null;
    if (args.length == 3) {
      sender.sendMessage(ChatColor.RED + "Error!" + ChatColor.GRAY + " Incomplete Command",
        ChatColor.GRAY + "Please provide a valid " + ChatColor.YELLOW + "level" + ChatColor.GRAY + " value.",
        ChatColor.GRAY + "You may use the " + ChatColor.YELLOW + "~" + ChatColor.GRAY + " to prevent changes."
        );
      return;
    }
    
    if (args[3] == "~") { primary = data.getOrDefault(type.getPrimaryKey(), PersistentDataType.INTEGER, 0); }
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
    
    //Attempt to parse out the secondary value
    Integer secondary = null;
    if (args.length == 4) {
      sender.sendMessage(
        ChatColor.RED + "Error!" + ChatColor.GRAY + " Incomplete Command",
        ChatColor.GRAY + "Please provide a valid " + ChatColor.YELLOW + "duration" + ChatColor.GRAY + " value.",
        ChatColor.GRAY + "You may use the " + ChatColor.YELLOW + "~" + ChatColor.GRAY + " to prevent changes."
        );
      return;
    }
    
    if (args[4] == "~") { secondary = data.getOrDefault(type.getSecondaryKey(), PersistentDataType.INTEGER, 0); }
    else {
       try { secondary = Integer.parseInt(args[4]); }
       catch(NumberFormatException e) {
         sender.sendMessage(ChatColor.RED + "Error!" + ChatColor.GRAY + " Parsing Error",
           ChatColor.GRAY + "Failed to parse integer for " + ChatColor.YELLOW + "duration" + ChatColor.GRAY + " value."
           );
         return;
       }
    }
    
    //Apply the effect
    type.apply(player, primary, secondary);
    
    //Success Message
    sender.sendMessage(
      ChatColor.GREEN + "Success!" + ChatColor.GRAY + " Player successfully modified"
      );
  }
  
  /**
   * Sub Command for modifying skills
   * @param sender The command sender.
   * @param key The key being used.
   * @param args The arguments passed.
   * @param player The player being modified.
   * @param type The rpg skill.
   */
  private void subCmdModifySkill(CommandSender sender, String key, String[] args, Player player, RPGSkillType type) {
    
    //Variables
    PersistentDataContainer data = player.getPersistentDataContainer();
    
    //Attempt to parse out the primary value
    Integer primary = null;
    if (args.length == 3) {
      sender.sendMessage(ChatColor.RED + "Error!" + ChatColor.GRAY + " Incomplete Command",
        ChatColor.GRAY + "Please provide a valid " + ChatColor.YELLOW + "level" + ChatColor.GRAY + " value.",
        ChatColor.GRAY + "You may use the " + ChatColor.YELLOW + "~" + ChatColor.GRAY + " to prevent changes."
        );
      return;
    }
    
    if (args[3] == "~") { primary = data.getOrDefault(type.getPrimaryKey(), PersistentDataType.INTEGER, 0); }
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
    
    //Attempt to parse out the secondary value
    Integer secondary = null;
    if (args.length == 4) {
      sender.sendMessage(
        ChatColor.RED + "Error!" + ChatColor.GRAY + " Incomplete Command",
        ChatColor.GRAY + "Please provide a valid " + ChatColor.YELLOW + "experience" + ChatColor.GRAY + " value.",
        ChatColor.GRAY + "You may use the " + ChatColor.YELLOW + "~" + ChatColor.GRAY + " to prevent changes."
        );
      return;
    }
    
    if (args[4] == "~") { secondary = data.getOrDefault(type.getSecondaryKey(), PersistentDataType.INTEGER, 0); }
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
    
    //Make the change
    type.setLevel(data, primary);
    type.setExperience(data, secondary);
    
    //Success Message
    sender.sendMessage(
      ChatColor.GREEN + "Success!" + ChatColor.GRAY + " Player successfully modified"
      );
  }
  
  
  /**
   * Updates the keys after everything is registered.
   */
  public void updateKeys() {
    
    if (!keys.isEmpty()) { return; }
    
    //Fetch all the keys available
    for(GenericRPGTypeManager<?> manager : plugin.getRPGDataTypeManager().getManagers()) {
      keys.addAll(manager.getKeys());
    }
  }
}
