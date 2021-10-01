package com.gmail.alexdion93.aeonrpg.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
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
import com.gmail.alexdion93.aeonrpg.util.RPGDataUtil;

/**
 * A command that allows the user to modify the rpg data present within an item.
 * This command treats values of ~ as unchanged.
 * @author Alex Dion
 *
 */
public class Command_RPGItem implements CommandExecutor, TabCompleter {
  private AeonRPG plugin;
  private ArrayList<String> keys;

  /**
   * Constructor
   *
   * @param plugin the plugin tied to this
   */
  public Command_RPGItem(AeonRPG plugin) {
    this.plugin = plugin;
    keys = new ArrayList<>();
    plugin.getCommand("rpgitem").setExecutor(this);
    plugin.getCommand("rpgitem").setTabCompleter(this);
  }
  
  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    
    //Ensure a player is using this command
    if (!(sender instanceof Player)) {
      sender.sendMessage(
        ChatColor.RED + "Error!" + ChatColor.GRAY + " Invalid Sender",
        ChatColor.GRAY + "This command must be used by a player."
        );
      return true;
    }
    
    // Fetch the player
    Player player = (Player) sender;
    
    // rpgitem
    if (args.length == 0) {
      sender.sendMessage(ChatColor.RED + "Error!" + ChatColor.GRAY + " Incomplete Command",
        ChatColor.GRAY + "Try one of the following...",
        ChatColor.YELLOW + "  /rpgitem" + ChatColor.GRAY + " inspect",
        ChatColor.YELLOW + "  /rpgitem" + ChatColor.GRAY + " load <file>",
        ChatColor.YELLOW + "  /rpgitem" + ChatColor.GRAY + " modify <key> <value>",
        ChatColor.YELLOW + "  /rpgitem" + ChatColor.GRAY + " save <file>"
        );
      return true;
    }
    
    // rpgitem load <file>
    if (args[0].equalsIgnoreCase("save")) {
      subCmdLoad(player, args);
      return true;
    }
    
    // Ensure the player has an item for the following commands
    ItemStack item = player.getInventory().getItemInMainHand();
    ItemMeta meta = item.getItemMeta();
    if (item == null || meta == null) {
      sender.sendMessage(
        ChatColor.RED + "Error!" + ChatColor.GRAY + " No Item",
        ChatColor.GRAY + "You must be holding an item to use this command."
        );
      return true;
    }
    
    // rpgitem inspect
    if (args[0].equalsIgnoreCase("inspect")) {
      subCmdInspect(sender, meta);
      return true;
    }
    
    // rpgitem modify <key> <value>
    if (args[0].equalsIgnoreCase("modify")) {
      subCmdModify(sender, args, item);
      return true;
    }
    
    // rpgitem save <file>
    if (args[0].equalsIgnoreCase("save")) {
      subCmdSave(sender, args, item);
      return true;
    }
    
    // Exit
    return true;
  }

  @Override
  public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
    // Variables
    updateKeys();
    
    // rpgitem <operation>
    if (args.length == 1) {
      List<String> result = new ArrayList<>();
      for(String option : Arrays.asList("inspect", "modify")) {
        if (args[0].isEmpty() && !option.toLowerCase().contains(args[0])) { continue; }
        result.add(option);
      }
      return result;
    }
    
    // rpgitem load <file>
    if (args.length == 2 && args[0].equalsIgnoreCase("load")) {
      List<String> result = new ArrayList<>();
      
      File directory = new File(plugin.getDataFolder().getAbsolutePath() + "/Items");
      if (directory.exists()) {
        
        if (directory.listFiles().length == 0) { return Arrays.asList("<No Files Found>"); }
        
        for(File file : directory.listFiles()) {
          if (!file.getName().endsWith(".yml")) { continue; }
          result.add(file.getName());
        }
      }
      
      return result;
    }
    
    // rpgitem load <file>
    if (args.length == 2 && args[0].equalsIgnoreCase("save")) {
      return Arrays.asList("<filename>");
    }
    
    // rpgitem modify <key>
    if (args.length == 2 && args[0].equalsIgnoreCase("modify")) {
      List<String> result = new ArrayList<>();
      
      for(String key : keys) {
        if (!args[1].isEmpty() && key.toLowerCase().contains(args[1])) { continue; }
        result.add(key);
      }
      
      return result;
    }
    
    // rpgitem modify <key> <value>
    if (args.length >= 3 && args[0].equalsIgnoreCase("modify")) {
      RPGDataType type = plugin.getRPGDataTypeManager().get(args[1]);
      if (type == null) { return new ArrayList<>(); }
      
      // RPG Attributes
      // rpgitem modify <key> <flat> <scaling>
      if (type instanceof RPGAttributeType) {
        if (args.length == 3) { return Arrays.asList("<flat>"); }
        if (args.length == 4) { return Arrays.asList("<scaling>"); }
        return new ArrayList<>();
      }
      
      // RPG Enchantments
      // rpgitem modify <key> <level>
      else if (type instanceof RPGEnchantmentType) {
        if (args.length == 3) { return Arrays.asList("<level>"); }
        return new ArrayList<>();
      }
      
      // RPG Potion Effects
      // rpgitem modify <key> <level> <duration>
      else if (type instanceof RPGPotionEffectType) {
        if (args.length == 3) { return Arrays.asList("<level>"); }
        if (args.length == 4) { return Arrays.asList("<duration>"); }
        return new ArrayList<>();
      }
      
      // RPG Skills
      // rpgitem modify <key> <level> <experience>
      else if (type instanceof RPGSkillType) {
        if (args.length == 3) { return Arrays.asList("<level>"); }
        if (args.length == 4) { return Arrays.asList("<experience>"); }
        return new ArrayList<>();
      }
      
      //RPG Generics
      // rpgitem modify <key> <values...>
      else {
        // RPG Two Valued
        if (type instanceof RPGDataTwoValued) {
          if (args.length == 3) { return Arrays.asList("<integer>"); }
          if (args.length == 4) { return Arrays.asList("<integer>"); }
          
          // If this is also a RPGDataString
          if (type instanceof RPGDataString) {
            if (args.length == 5) { return Arrays.asList("<string>"); }
          }
        }
        
        // RPG One Valued
        else if (type instanceof RPGDataOneValued) {
          if (args.length == 3) { return Arrays.asList("<integer>"); }
          
          // If this is also a RPGDataString
          if (type instanceof RPGDataString) {
            if (args.length == 4) { return Arrays.asList("<string>"); }
          }
        }
        
        // RPG Data String
        else if (type instanceof RPGDataString) {
          if (args.length == 3) { return Arrays.asList("<string>"); }
        }
      }
    }
    
    //No further arguments
    return new ArrayList<>();
  }
  
  /**
   * Sub command save logic
   * @param sender The command sender
   * @param args The arguments provided
   * @param item The item stack being modified
   */
  public void subCmdSave(CommandSender sender, String args[], ItemStack item) {
    // Variables
    int index = 1;
    String filename = String.join(" ", Arrays.copyOfRange(args, index, args.length));
    String directory = plugin.getDataFolder().getAbsolutePath() + "/Items/";
    File file = new File(directory + filename + (filename.endsWith(".yml") ? "" : ".yml"));
    YamlConfiguration config = new YamlConfiguration();
    
    //Save to the config
    RPGDataUtil.saveItem(config, item);
    
    //Save the config
    try { config.save(file); }
    catch (Exception e) {
      e.printStackTrace();
      sender.sendMessage(ChatColor.RED + "Error!" + ChatColor.GRAY + " " + e.getClass().getSimpleName(),
        ChatColor.GRAY + "See the console for a detailed report."
        );
    }
    
    //Success Message
    sender.sendMessage(
      ChatColor.GREEN + "Success!" + ChatColor.GRAY + " Item successfully saved"
      );
  }
  
  /**
   * Sub command load logic
   * @param player The command sender
   * @param args The arguments provided
   */
  public void subCmdLoad(Player player, String args[]) {
    // Variables
    int index = 1;
    String filename = String.join(" ", Arrays.copyOfRange(args, index, args.length));
    String directory = plugin.getDataFolder().getAbsolutePath() + "/Items/";
    File file = new File(directory + filename);
    YamlConfiguration config = new YamlConfiguration();
    
    //Load the configuration
    try {
      config.load(file);
      
      //Give the item to the player
      for(ItemStack leftover : player.getInventory().addItem(RPGDataUtil.loadItem(config)).values()) {
        player.getWorld().dropItem(player.getLocation(), leftover);
      }
      
    }
    catch (Exception e) {
      e.printStackTrace();
      player.sendMessage(ChatColor.RED + "Error!" + ChatColor.GRAY + " " + e.getClass().getSimpleName(),
        ChatColor.GRAY + "See the console for a detailed report."
        );
    }
    
    //Success Message
    player.sendMessage(
      ChatColor.GREEN + "Success!" + ChatColor.GRAY + " Item successfully loaded"
      );
  }
  
  /**
   * Sub command modify logic
   * @param sender The command sender
   * @param args The arguments provided
   * @param item The item stack being modified
   */
  private void subCmdModify(CommandSender sender, String[] args, ItemStack item) {
    //Not enough arguments
    if (args.length < 3) {
      sender.sendMessage(
        ChatColor.RED + "Error!" + ChatColor.GRAY + " Incomplete Command",
        ChatColor.GRAY + "Please provide a " + ChatColor.YELLOW + "key" + ChatColor.GRAY
          + " and a " + ChatColor.YELLOW + "value" + ChatColor.GRAY + ".",
        ChatColor.YELLOW + "  /rpgitem" + ChatColor.GRAY + " modify <key> <value>"
        );
    }
    
    //Ensure the type is valid
    String key = args[1];
    RPGDataType type = plugin.getRPGDataTypeManager().get(key);
    if (type == null) {
      sender.sendMessage(
        ChatColor.RED + "Error!" + ChatColor.GRAY + " Invalid Key",
        ChatColor.GRAY + "Please provide a valid " + ChatColor.YELLOW + "key" + ChatColor.GRAY + ".",
        ChatColor.YELLOW + "  /rpgitem" + ChatColor.GRAY + " modify <key> <value>"
        );
    }
    
    // RPG Attributes
    if (type instanceof RPGAttributeType) {
      subCmdModifyAttribute(sender, key, args, item, (RPGAttributeType) type);
      return;
    }
    
    // RPG Enchantments
    else if (type instanceof RPGEnchantmentType) {
      subCmdModifyEnchantment(sender, key, args, item, (RPGEnchantmentType) type);
      return;
    }
    
    // RPG Potion Effects
    else if (type instanceof RPGPotionEffectType) {
      subCmdModifyPotion(sender, key, args, item, (RPGPotionEffectType) type);
      return;
    }
    
    // RPG Skills
    else if (type instanceof RPGSkillType) {
      subCmdModifySkill(sender, key, args, item, (RPGSkillType) type);
      return;
    }
    
    //RPG Generics
    else {
      subCmdModifyGeneric(sender, key, args, item, type);
      return;
    }
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
      
      boolean isNegative = (type.getAlignment() == RPGDataAlignment.NEGATIVE);
      String positive = (isNegative ? ChatColor.RED : ChatColor.GREEN) + "+";
      String negative = (isNegative ? ChatColor.GREEN : ChatColor.RED) + "";
      
      sender.sendMessage("    " + ChatColor.YELLOW + type.getDisplayName() + ": " + ChatColor.WHITE +
          (level > 0 ? positive : negative) + "Lv." + level + ChatColor.WHITE
          + " & " + (experience > 0 ? positive : negative) + experience + "% Exp");
    }
  }

  /**
   * Sub Command for modifying attributes
   * @param sender The command sender.
   * @param key The key being used.
   * @param args The arguments passed.
   * @param item The item being modified.
   * @param type The rpg attribute.
   */
  private void subCmdModifyAttribute(CommandSender sender, String key, String[] args, ItemStack item, RPGAttributeType type) {
    
    //Variables
    ItemMeta meta = item.getItemMeta();
    PersistentDataContainer data = meta.getPersistentDataContainer();
    
    //Attempt to parse out the primary value
    Integer primary = null;
    if (args.length == 2) {
      sender.sendMessage(ChatColor.RED + "Error!" + ChatColor.GRAY + " Incomplete Command",
        ChatColor.GRAY + "Please provide a valid " + ChatColor.YELLOW + "flat" + ChatColor.GRAY + " value.",
        ChatColor.GRAY + "You may use the " + ChatColor.YELLOW + "~" + ChatColor.GRAY + " to prevent changes."
        );
      return;
    }
    
    if (args[2] == "~") {
      primary = data.getOrDefault(type.getPrimaryKey(), PersistentDataType.INTEGER, 0);
    }
    else {
       try {
         primary = Integer.parseInt(args[2]);
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
    if (args.length == 3) {
      sender.sendMessage(
        ChatColor.RED + "Error!" + ChatColor.GRAY + " Incomplete Command",
        ChatColor.GRAY + "Please provide a valid " + ChatColor.YELLOW + "scaling" + ChatColor.GRAY + " value."
        );
      return;
    }
    
    if (args[3] == "~") {
      secondary = data.getOrDefault(type.getSecondaryKey(), PersistentDataType.INTEGER, 0);
    }
    else {
       try {
         secondary = Integer.parseInt(args[3]);
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
    meta.setLore(RPGDataUtil.generateLore(data));
    item.setItemMeta(meta);
    
    //Success Message
    sender.sendMessage(
      ChatColor.GREEN + "Success!" + ChatColor.GRAY + " Item successfully modified"
      );
  }
    
  /**
   * Sub Command for modifying enchantments
   * @param sender The command sender.
   * @param key The key being used.
   * @param args The arguments passed.
   * @param item The item being modified.
   * @param type The rpg enchantment.
   */
  private void subCmdModifyEnchantment(CommandSender sender, String key, String[] args, ItemStack item, RPGEnchantmentType type) {
    
    //Variables
    ItemMeta meta = item.getItemMeta();
    PersistentDataContainer data = meta.getPersistentDataContainer();
    
    //Attempt to parse out the primary value
    Integer primary = null;
    if (args.length == 2) {
      sender.sendMessage(ChatColor.RED + "Error!" + ChatColor.GRAY + " Incomplete Command",
        ChatColor.GRAY + "Please provide a valid " + ChatColor.YELLOW + "level" + ChatColor.GRAY + " value.",
        ChatColor.GRAY + "You may use the " + ChatColor.YELLOW + "~" + ChatColor.GRAY + " to prevent changes."
        );
      return;
    }
    
    if (args[2] == "~") {
      primary = data.getOrDefault(type.getPrimaryKey(), PersistentDataType.INTEGER, 0);
    }
    else {
       try {
         primary = Integer.parseInt(args[2]);
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
    meta.setLore(RPGDataUtil.generateLore(data));
    item.setItemMeta(meta);
    
    //Success Message
    sender.sendMessage(
      ChatColor.GREEN + "Success!" + ChatColor.GRAY + " Item successfully modified"
      );
  }
  
  /**
   * Sub Command for modifying generics
   * @param sender The command sender.
   * @param key The key being used.
   * @param args The arguments passed.
   * @param item The item being modified.
   * @param type The rpg data type.
   */
  private void subCmdModifyGeneric(CommandSender sender, String key, String[] args, ItemStack item, RPGDataType type) {
    
    ItemMeta meta = item.getItemMeta();
    PersistentDataContainer data = meta.getPersistentDataContainer();
    int primary = 0;
    int secondary = 0;
    String string = null;
    int index = 2;
    
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
      
      //Fetch the alternate class
      RPGDataOneValued alt = ((RPGDataOneValued) type);
      
      // Parse the value from the index
      if (args[index] == "~") { primary = (data == null ? 0 : type.get(data, alt.getPrimaryKey(), PersistentDataType.INTEGER, 0)); }
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
      
      //Set the value
      type.set(data, alt.getPrimaryKey(), PersistentDataType.INTEGER, primary);
      
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
      
      //Fetch the alternate class
      RPGDataTwoValued alt = ((RPGDataTwoValued) type);
      
      // Parse the value from the index
      if (args[index] == "~") { secondary = (data == null ? 0 : type.get(data, alt.getSecondaryKey(), PersistentDataType.INTEGER, 0)); }
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
      
      //Set the value
      type.set(data, alt.getSecondaryKey(), PersistentDataType.INTEGER, secondary);
      
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
      
      //Fetch the alternate class
      RPGDataString alt = ((RPGDataString) type);
      
      // Parse the value from the index
      if (args[index] == "~") { string = (data == null ? null : type.get(data, alt.getStringKey(), PersistentDataType.STRING, null)); }
      else { string = String.join(" ", Arrays.copyOfRange(args, index, args.length)); }
      
      //Set the value
      type.set(data, alt.getStringKey(), PersistentDataType.STRING, string);
      
      //Increment the index for the next argument
      index++;
    }
    
    //Update the item
    meta.setLore(RPGDataUtil.generateLore(data));
    item.setItemMeta(meta);
    
    //Success Message
    sender.sendMessage(
      ChatColor.GREEN + "Success!" + ChatColor.GRAY + " Item successfully modified"
      );
  }
  
  /**
   * Sub Command for modifying potion effects
   * @param sender The command sender.
   * @param key The key being used.
   * @param args The arguments passed.
   * @param item The item being modified.
   * @param type The rpg potion effect.
   */
  private void subCmdModifyPotion(CommandSender sender, String key, String[] args, ItemStack item, RPGPotionEffectType type) {
    
    //Variables
    ItemMeta meta = item.getItemMeta();
    PersistentDataContainer data = meta.getPersistentDataContainer();
    
    //Attempt to parse out the primary value
    Integer primary = null;
    if (args.length == 2) {
      sender.sendMessage(ChatColor.RED + "Error!" + ChatColor.GRAY + " Incomplete Command",
        ChatColor.GRAY + "Please provide a valid " + ChatColor.YELLOW + "level" + ChatColor.GRAY + " value.",
        ChatColor.GRAY + "You may use the " + ChatColor.YELLOW + "~" + ChatColor.GRAY + " to prevent changes."
        );
      return;
    }
    
    if (args[2] == "~") { primary = data.getOrDefault(type.getPrimaryKey(), PersistentDataType.INTEGER, 0); }
    else {
       try { primary = Integer.parseInt(args[2]); }
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
    if (args.length == 3) {
      sender.sendMessage(
        ChatColor.RED + "Error!" + ChatColor.GRAY + " Incomplete Command",
        ChatColor.GRAY + "Please provide a valid " + ChatColor.YELLOW + "duration" + ChatColor.GRAY + " value.",
        ChatColor.GRAY + "You may use the " + ChatColor.YELLOW + "~" + ChatColor.GRAY + " to prevent changes."
        );
      return;
    }
    
    if (args[3] == "~") { secondary = data.getOrDefault(type.getSecondaryKey(), PersistentDataType.INTEGER, 0); }
    else {
       try { secondary = Integer.parseInt(args[3]); }
       catch(NumberFormatException e) {
         sender.sendMessage(ChatColor.RED + "Error!" + ChatColor.GRAY + " Parsing Error",
           ChatColor.GRAY + "Failed to parse integer for " + ChatColor.YELLOW + "duration" + ChatColor.GRAY + " value."
           );
         return;
       }
    }
    
    //Make the change
    type.setLevel(data, primary);
    type.setDuration(data, secondary);
    meta.setLore(RPGDataUtil.generateLore(data));
    item.setItemMeta(meta);
    
    //Success Message
    sender.sendMessage(
      ChatColor.GREEN + "Success!" + ChatColor.GRAY + " Item successfully modified"
      );
  }
  
  /**
   * Sub Command for modifying skills
   * @param sender The command sender.
   * @param key The key being used.
   * @param args The arguments passed.
   * @param item The item being modified.
   * @param type The rpg skill.
   */
  private void subCmdModifySkill(CommandSender sender, String key, String[] args, ItemStack item, RPGSkillType type) {
    
    //Variables
    ItemMeta meta = item.getItemMeta();
    PersistentDataContainer data = meta.getPersistentDataContainer();
    
    //Attempt to parse out the primary value
    Integer primary = null;
    if (args.length == 2) {
      sender.sendMessage(ChatColor.RED + "Error!" + ChatColor.GRAY + " Incomplete Command",
        ChatColor.GRAY + "Please provide a valid " + ChatColor.YELLOW + "level" + ChatColor.GRAY + " value.",
        ChatColor.GRAY + "You may use the " + ChatColor.YELLOW + "~" + ChatColor.GRAY + " to prevent changes."
        );
      return;
    }
    
    if (args[2] == "~") { primary = data.getOrDefault(type.getPrimaryKey(), PersistentDataType.INTEGER, 0); }
    else {
       try { primary = Integer.parseInt(args[2]); }
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
    if (args.length == 3) {
      sender.sendMessage(
        ChatColor.RED + "Error!" + ChatColor.GRAY + " Incomplete Command",
        ChatColor.GRAY + "Please provide a valid " + ChatColor.YELLOW + "experience" + ChatColor.GRAY + " value.",
        ChatColor.GRAY + "You may use the " + ChatColor.YELLOW + "~" + ChatColor.GRAY + " to prevent changes."
        );
      return;
    }
    
    if (args[3] == "~") { secondary = data.getOrDefault(type.getSecondaryKey(), PersistentDataType.INTEGER, 0); }
    else {
       try { secondary = Integer.parseInt(args[3]); }
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
    meta.setLore(RPGDataUtil.generateLore(data));
    item.setItemMeta(meta);
    
    //Success Message
    sender.sendMessage(
      ChatColor.GREEN + "Success!" + ChatColor.GRAY + " Item successfully modified"
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
    
    Collections.sort(keys);
  }
}
