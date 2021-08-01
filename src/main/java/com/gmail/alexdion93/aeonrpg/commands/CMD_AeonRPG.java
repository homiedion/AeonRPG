package com.gmail.alexdion93.aeonrpg.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import com.gmail.alexdion93.aeonrpg.AeonRPG;
import com.gmail.alexdion93.aeonrpg.data.interfaces.RPGDataString;
import com.gmail.alexdion93.aeonrpg.data.interfaces.RPGDataTwoValued;
import com.gmail.alexdion93.aeonrpg.data.interfaces.RPGDataValued;
import com.gmail.alexdion93.aeonrpg.data.type.RPGDataType;
import com.gmail.alexdion93.aeonrpg.managers.GenericRPGTypeManager;
import com.gmail.alexdion93.aeonrpg.util.RPGDataUtil;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.ComponentBuilder.FormatRetention;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class CMD_AeonRPG implements CommandExecutor, TabCompleter {

  /*
   * Object Variables
   */
  private final AeonRPG plugin;

  /**
   * Constructor
   *
   * @param plugin the plugin tied to this
   */
  public CMD_AeonRPG(final AeonRPG plugin) {
    this.plugin = plugin;
    plugin.getCommand("aeonrpg").setExecutor(this);
    plugin.getCommand("aeonrpg").setTabCompleter(this);
  }

  /**
   * The command "aeonrpg copy"
   *
   * @param sender  the command sender
   * @param command the command being sent
   * @param label   the command label
   * @param args    the command's arguments
   */
  public void cmdCopy(final CommandSender sender, final Command command, final String label, final String[] args) {

    // Exit if not player
    if (!(sender instanceof Player)) {
      return;
    }

    final Player player = (Player) sender;
    final PlayerInventory inventory = player.getInventory();
    final ItemStack item = inventory.getItemInMainHand();

    // Exit if null item
    if ((item == null) || (item.getType() == Material.AIR)) {
      return;
    }

    // Debug Test: Give copy of the item to the player
    ItemStack copy = new ItemStack(item);
    ItemMeta meta = item.getItemMeta();

    meta.setDisplayName("Copy Cat");
    copy.setItemMeta(meta);

    inventory.addItem(RPGDataUtil.randomizeAttributeUUID(copy));
  }

  /**
   * The command "aeonrpg edit item"
   *
   * @param sender  the command sender
   * @param command the command being sent
   * @param label   the command label
   * @param args    the command's arguments
   */
  public void cmdEditItem(final CommandSender sender, final Command command, final String label, final String[] args) {

    // Data Validation
    if (!(sender instanceof Player)) {
      throw new ClassCastException("This command must be run by a player");
    }

    // Variables
    final Player player = (Player) sender;
    final PlayerInventory inventory = player.getInventory();
    final ItemStack item = inventory.getItemInMainHand();
    final RPGDataType type = plugin.getRPGDataTypeManager().get(getArgument(args, 2));
    final String value = getArgument(args, 3);
    final String altValue = getArgument(args, 4);

    // Data Validation
    if (type == null) {
      throw new NullPointerException("Cannot find data type by the specified key");
    }
    if (value == null) {
      throw new NullPointerException("This type requires a value");
    }

    // Perform operations on the RPGDataTwoValued
    if (type instanceof RPGDataTwoValued) {
      // Variables
      int v, a;

      // Data Validation
      if (altValue == null) {
        throw new NullPointerException("This type requires an second value");
      }

      try {
        v = Integer.parseInt(value);
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException("The primary value cannot be parsed to an integer");
      }

      try {
        a = Integer.parseInt(altValue);
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException("The secondary value cannot be parsed to an integer");
      }

      // Add data
      try {
        String result = RPGDataUtil.editData(player, item, type, v, a);
        player.sendMessage(result);
      } catch (Exception e) {
        player.sendMessage(ChatColor.RED + e.getMessage());
      }
    }

    // Perform operations on the RPGDataValued
    else if (type instanceof RPGDataValued) {
      // Variables
      int v;

      // Data Validation
      try {
        v = Integer.parseInt(value);
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException("The primary value cannot be parsed to an integer");
      }

      // Add data
      try {
        String result = RPGDataUtil.editData(player, item, type, v);
        player.sendMessage(result);
      } catch (Exception e) {
        player.sendMessage(ChatColor.RED + e.getMessage());
      }

    }

    // Perform operations on the RPGDataString
    else if (type instanceof RPGDataString) {
      // Add data
      try {
        String result = RPGDataUtil.editData(player, item, type, value);
        player.sendMessage(result);
      } catch (Exception e) {
        player.sendMessage(ChatColor.RED + e.getMessage());
      }
    }
  }

  /**
   * The command "aeonrpg edit player [player] type value alt_value"
   *
   * @param sender  the command sender
   * @param command the command being sent
   * @param label   the command label
   * @param args    the command's arguments
   */
  public void cmdEditPlayer(final CommandSender sender, final Command command, final String label,
      final String[] args) {

    // Variables
    final Player player = Bukkit.getPlayer(getArgument(args, 2));
    final RPGDataType type = plugin.getRPGDataTypeManager().get(getArgument(args, 3));
    final String value = getArgument(args, 4);
    final String altValue = getArgument(args, 5);

    // Data Validation
    if (player == null) {
      throw new NullPointerException("Cannot find player named " + getArgument(args, 2) + "");
    }
    if (type == null) {
      throw new NullPointerException("Cannot find RPGDataType named " + getArgument(args, 3) + "");
    }
    if (value == null) {
      throw new NullPointerException("No primary value provided");
    }

    // Perform operations on the RPGDataTwoValued
    if (type instanceof RPGDataTwoValued) {
      // Variables
      int v, a;

      // Data Validation
      if (altValue == null) {
        throw new NullPointerException("This type requires an second value");
      }

      try {
        v = Integer.parseInt(value);
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException("The primary value cannot be parsed to an integer");
      }

      try {
        a = Integer.parseInt(altValue);
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException("The secondary value cannot be parsed to an integer");
      }

      // Add data
      try {
        String result = RPGDataUtil.editData(player, type, v, a);
        player.sendMessage(result);
      } catch (Exception e) {
        player.sendMessage(ChatColor.RED + e.getMessage());
      }
    }

    // Perform operations on the RPGDataValued
    else if (type instanceof RPGDataValued) {

      // Variables
      int v;

      // Data Validation
      try {
        v = Integer.parseInt(value);
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException("The primary value cannot be parsed to an integer");
      }

      // Add data
      try {
        String result = RPGDataUtil.editData(player, type, v);
        player.sendMessage(result);
      } catch (Exception e) {
        player.sendMessage(ChatColor.RED + e.getMessage());
      }
    }

    // Perform operations on the RPGDataString
    else if (type instanceof RPGDataString) {
      // Add data
      try {
        String result = RPGDataUtil.editData(player, type, value);
        player.sendMessage(result);
      } catch (Exception e) {
        player.sendMessage(ChatColor.RED + e.getMessage());
      }
    }
  }

  /**
   * The command "aeonrpg inspect item"
   *
   * @param sender  the command sender
   * @param command the command being sent
   * @param label   the command label
   * @param args    the command's arguments
   */
  public void cmdInspectItem(final CommandSender sender, final Command command, final String label,
      final String[] args) {

    // Data Validation
    if (!(sender instanceof Player)) {
      throw new IllegalArgumentException("This command can only be run by players");
    }

    // Variables
    Player player = (Player) sender;
    ItemStack item = player.getInventory().getItemInMainHand();
    PersistentDataContainer data;

    // Data Validation
    if (item == null) {
      throw new NullPointerException("No item provided");
    }
    if (item.getType() == Material.AIR) {
      throw new IllegalArgumentException("Item cannot be air");
    }
    if (item.getItemMeta() == null) {
      throw new NullPointerException("Item does not have meta");
    }

    // Fetch the data
    data = item.getItemMeta().getPersistentDataContainer();

    // Loop all managers
    for (final GenericRPGTypeManager<?> manager : plugin.getRPGDataTypeManager().getManagers()) {

      // Loop all keys stored within
      for (String key : manager.getKeys()) {

        RPGDataType type = manager.get(key);
        NamespacedKey nsk = type.getNamespacedKey();

        if (type instanceof RPGDataTwoValued) {
          if (!data.has(nsk, PersistentDataType.INTEGER)) {
            continue;
          }
          RPGDataTwoValued altType = (RPGDataTwoValued) type;
          NamespacedKey ask = altType.getAltNamespacedKey();
          // sender.sendMessage(ChatColor.GOLD + type.getDisplayName() + ": " +
          // ChatColor.RESET + data.get(nsk, PersistentDataType.INTEGER) + " & " +
          // data.get(ask, PersistentDataType.INTEGER));
          // Send Formatted Message
          player.spigot().sendMessage(new ComponentBuilder(type.getDisplayName()).color(ChatColor.GOLD)
              .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.GRAY + type.getDescription())))
              .append(
                  ": " + data.get(nsk, PersistentDataType.INTEGER) + " & " + data.get(ask, PersistentDataType.INTEGER))
              .retain(FormatRetention.EVENTS).create());
        }

        else if (type instanceof RPGDataValued) {
          if (!data.has(nsk, PersistentDataType.INTEGER)) {
            continue;
          }
          // sender.sendMessage(ChatColor.GOLD + type.getDisplayName() + ": " +
          // ChatColor.RESET + data.get(nsk, PersistentDataType.INTEGER));
          player.spigot()
              .sendMessage(new ComponentBuilder(type.getDisplayName()).color(ChatColor.GOLD)
                  .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.GRAY + type.getDescription())))
                  .append(": " + data.get(nsk, PersistentDataType.INTEGER)).retain(FormatRetention.EVENTS).create());
        }

        else if (type instanceof RPGDataString) {
          if (!data.has(nsk, PersistentDataType.STRING)) {
            continue;
          }
          // sender.sendMessage(ChatColor.GOLD + type.getDisplayName() + ": " +
          // ChatColor.RESET + data.get(nsk, PersistentDataType.STRING));

          // Send Formatted Message
          player.spigot()
              .sendMessage(new ComponentBuilder(type.getDisplayName()).color(ChatColor.GOLD)
                  .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.GRAY + type.getDescription())))
                  .append(": " + data.get(nsk, PersistentDataType.STRING)).retain(FormatRetention.EVENTS).create());
        }
      }
    }
  }

  /**
   * The command "aeonrpg inspect player"
   *
   * @param sender  the command sender
   * @param command the command being sent
   * @param label   the command label
   * @param args    the command's arguments
   */
  public void cmdInspectPlayer(final CommandSender sender, final Command command, final String label,
      final String[] args) {
    // Variables
    Player player;
    PersistentDataContainer data;

    // Attempt to fetch the player
    if (hasArgument(args, 2)) {
      player = Bukkit.getPlayer(getArgument(args, 2));
    } else {
      if (sender instanceof Player) {
        player = (Player) sender;
      } else {
        throw new IllegalArgumentException("A player must be specified to run this command from console");
      }
    }

    // Data Validation
    if (player == null) {
      throw new NullPointerException("Cannot find online player by the specified name");
    }

    // Fetch the data
    data = player.getPersistentDataContainer();

    // Swap the player to the sender if possible
    player = null;
    if (sender instanceof Player) {
      player = (Player) sender;
    }

    // Loop all managers
    for (final GenericRPGTypeManager<?> manager : plugin.getRPGDataTypeManager().getManagers()) {

      // Loop all keys stored within
      for (String key : manager.getKeys()) {

        RPGDataType type = manager.get(key);
        NamespacedKey nsk = type.getNamespacedKey();

        if (type instanceof RPGDataTwoValued) {
          if (!data.has(nsk, PersistentDataType.INTEGER)) {
            continue;
          }
          RPGDataTwoValued altType = (RPGDataTwoValued) type;
          NamespacedKey ask = altType.getAltNamespacedKey();
          if (player == null) {
            sender.sendMessage(ChatColor.GOLD + type.getDisplayName() + ": " + ChatColor.RESET
                + data.get(nsk, PersistentDataType.INTEGER) + " & " + data.get(ask, PersistentDataType.INTEGER));
          } else {
            player.spigot().sendMessage(new ComponentBuilder(type.getDisplayName()).color(ChatColor.GOLD)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.GRAY + type.getDescription())))
                .append(": " + data.get(nsk, PersistentDataType.INTEGER) + " & "
                    + data.get(ask, PersistentDataType.INTEGER))
                .retain(FormatRetention.EVENTS).create());
          }

        }

        else if (type instanceof RPGDataValued) {
          if (!data.has(nsk, PersistentDataType.INTEGER)) {
            continue;
          }
          if (player == null) {
            sender.sendMessage(ChatColor.GOLD + type.getDisplayName() + ": " + ChatColor.RESET
                + data.get(nsk, PersistentDataType.INTEGER));
          } else {
            player.spigot().sendMessage(new ComponentBuilder(type.getDisplayName()).color(ChatColor.GOLD)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.GRAY + type.getDescription())))
                .append(": " + data.get(nsk, PersistentDataType.INTEGER)).retain(FormatRetention.EVENTS).create());
          }
        }

        else if (type instanceof RPGDataString) {
          if (!data.has(nsk, PersistentDataType.STRING)) {
            continue;
          }
          if (player == null) {
            sender.sendMessage(ChatColor.GOLD + type.getDisplayName() + ": " + ChatColor.RESET
                + data.get(nsk, PersistentDataType.STRING));
          } else {
            player.spigot().sendMessage(new ComponentBuilder(type.getDisplayName()).color(ChatColor.GOLD)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.GRAY + type.getDescription())))
                .append(": " + data.get(nsk, PersistentDataType.STRING)).retain(FormatRetention.EVENTS).create());
          }
        }
      }
    }
  }

  /**
   * The command "aeonrpg load [filename]"
   *
   * @param sender  the command sender
   * @param command the command being sent
   * @param label   the command label
   * @param args    the command's arguments
   */
  public void cmdLoad(final CommandSender sender, final Command command, final String label, final String[] args) {
    // Exit if not player
    if (!(sender instanceof Player)) {
      return;
    }

    Player player = (Player) sender;
    String name = getArgument(args, 1);

    // Name Check
    if ((name == null) || name.trim().isEmpty()) {
      player.sendMessage(ChatColor.RED + "Invalid file name!");
      return;
    }

    ItemStack item = RPGDataUtil.loadItem("Item/" + (name == null ? sender.getName() : name) + ".yml");

    // Result Check
    if ((item == null) || (item.getType() == Material.AIR)) {
      player.sendMessage(ChatColor.RED + "Failed to create an item!");
      return;
    }

    // Give the item to the player
    player.getInventory().addItem(item);
  }

  /**
   * The command "aeonrpg save [filename]"
   *
   * @param sender  the command sender
   * @param command the command being sent
   * @param label   the command label
   * @param args    the command's arguments
   */
  public void cmdSave(final CommandSender sender, final Command command, final String label, final String[] args) {
    // Exit if not player
    if (!(sender instanceof Player)) {
      return;
    }

    Player player = (Player) sender;
    ItemStack item = player.getInventory().getItemInMainHand();
    String name = getArgument(args, 1);

    if ((item == null) || (item.getType() == Material.AIR)) {
      player.sendMessage(ChatColor.RED + "You must be holding an item to use this command!");
      return;
    }

    // Save the item
    RPGDataUtil.saveItem("Item/" + (name == null ? sender.getName() : name) + ".yml", item);
  }

  /**
   * Test Command used for debug purposes
   *
   * @param sender  The command sender
   * @param command The command being sent
   * @param label   The command's label
   * @param args    The command's arguments
   */
  private void cmdTest(CommandSender sender, Command command, String label, String[] args) {

    if (!(sender instanceof Player)) {
      throw new IllegalArgumentException("This command can only be run by a player");
    }
    Player player = (Player) sender;

    Bukkit.broadcastMessage("TEST COMMAND RUN");
    Bukkit.broadcastMessage("" + player.getAttribute(Attribute.GENERIC_ARMOR).getValue());
  }

  /**
   * Returns an argument from an array if present
   *
   * @param args  The array of arguments
   * @param index The index we're targetting
   * @return The argument or null
   */
  public String getArgument(final String[] args, final int index) {
    if (!hasArgument(args, index)) {
      return null;
    }
    return args[index];
  }

  /**
   * Returns if the argument array has an argument at the position
   *
   * @param args  The array of arguments
   * @param index The index we're targetting
   * @return True if present
   */
  public boolean hasArgument(final String[] args, final int index) {
    // Invalid Index
    if (index < 0) {
      return false;
    }

    // Invalid Arguments Array
    if ((args == null) || (args.length == 0)) {
      return false;
    }

    // Index Out of Bounds
    if (args.length <= index) {
      return false;
    }

    // Argument in the position
    return true;
  }

  /**
   * Returns if the argument array contains a parameter at the specified index
   *
   * @param args  The array of arguments
   * @param arg   The argument we're looking for
   * @param index The index we're targetting
   * @return True if present
   */
  public boolean isArgument(final String[] args, final String arg, final int index) {
    // No argument present
    if (!hasArgument(args, index)) {
      return false;
    }

    // Invalid Argument
    if (arg == null) {
      return false;
    }

    // True if args[index] is the same as arg
    return (args[index].equalsIgnoreCase(arg));
  }

  @Override
  public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {

    // Strip chat colors
    for (int i = 0; i < args.length; i++) {
      args[i] = ChatColor.stripColor(args[i]);
    }

    // aeonrpg edit <player/item>
    if (isArgument(args, "edit", 0)) {
      if (isArgument(args, "item", 1)) {
        try {
          cmdEditItem(sender, command, label, args);
        } catch (Exception e) {
          sender.sendMessage(ChatColor.RED + e.getMessage());
        }
      }

      else if (isArgument(args, "player", 1)) {
        try {
          cmdEditPlayer(sender, command, label, args);
        } catch (Exception e) {
          sender.sendMessage(ChatColor.RED + e.getMessage());
        }
      }
    }

    // aeonrpg inspect <player/item>
    else if (isArgument(args, "inspect", 0)) {
      if (isArgument(args, "item", 1)) {
        try {
          cmdInspectItem(sender, command, label, args);
        } catch (Exception e) {
          sender.sendMessage(ChatColor.RED + e.getMessage());
        }
      }

      else if (isArgument(args, "player", 1)) {
        try {
          cmdInspectPlayer(sender, command, label, args);
        } catch (Exception e) {
          sender.sendMessage(ChatColor.RED + e.getMessage());
        }
      }
    }

    // aeonrpg load <filename>
    else if (isArgument(args, "load", 0)) {
      try {
        cmdLoad(sender, command, label, args);
      } catch (Exception e) {
        sender.sendMessage(ChatColor.RED + e.getMessage());
      }
    }

    // aeonrpg save [filename]
    else if (isArgument(args, "save", 0)) {
      try {
        cmdSave(sender, command, label, args);
      } catch (Exception e) {
        sender.sendMessage(ChatColor.RED + e.getMessage());
      }
    }

    // aeonrpg test
    else if (isArgument(args, "test", 0)) {
      try {
        cmdTest(sender, command, label, args);
      } catch (Exception e) {
        sender.sendMessage(ChatColor.RED + e.getMessage());
      }
    }

    // Exit
    return true;
  }

  @Override
  public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
    // Variables
    ArrayList<String> result = new ArrayList<String>();

    // Strip chat colors from arguments
    for (int i = 0; i < args.length; i++) {
      args[i] = ChatColor.stripColor(args[i]);
    }

    // aeonrpg edit <item/player>
    if (isArgument(args, "edit", 0)) {

      // aeonrpg edit item <key> <value>
      if (isArgument(args, "item", 1)) {

        // End
        if (hasArgument(args, 5)) {
        }

        // aeonrpg edit item <key> <integer> <integer>
        else if (hasArgument(args, 4)) {
          RPGDataType type = plugin.getRPGDataTypeManager().get(args[4]);
          if ((type != null) && (type instanceof RPGDataTwoValued)) {
            result.add("<integer>");
          }
        }

        // aeonrpg edit item <key> <integer>
        else if (hasArgument(args, 3)) {
          result.add("<integer>");
        }

        // aeonrpg edit item <key>
        else if (hasArgument(args, 2)) {
          for (final GenericRPGTypeManager<?> manager : plugin.getRPGDataTypeManager().getManagers()) {
            for (String key : manager.getKeys()) {
              key = key.toLowerCase();
              if (key.contains(args[2].toLowerCase())) {
                result.add(key);
              }
            }
          }
        }
      }

      // aeonrpg edit player <target> <key> <value>
      else if (isArgument(args, "player", 1)) {
        // End
        if (hasArgument(args, 6)) {
        }

        // aeonrpg edit item <player> <key> <integer> <integer>
        else if (hasArgument(args, 5)) {
          RPGDataType type = plugin.getRPGDataTypeManager().get(args[5]);
          if ((type != null) && (type instanceof RPGDataTwoValued)) {
            result.add("<integer>");
          }
        }

        // aeonrpg edit item <player> <key> <integer>
        else if (hasArgument(args, 4)) {
          result.add("<integer>");
        }

        // aeonrpg edit player <player> <key>
        else if (hasArgument(args, 3)) {
          for (final GenericRPGTypeManager<?> manager : plugin.getRPGDataTypeManager().getManagers()) {
            for (String key : manager.getKeys()) {
              key = key.toLowerCase();
              if (key.contains(args[3].toLowerCase())) {
                result.add(key);
              }
            }
          }
        }

        // aeonrpg edit player <player>
        else if (hasArgument(args, 2)) {
          for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getName().contains(args[2])) {
              result.add(player.getName());
            }
          }
        }
      }

      // aeonrpg edit ???
      else if (hasArgument(args, 1)) {
        if ("item".contains(args[1])) {
          result.add("item");
        }
        if ("player".contains(args[1])) {
          result.add("player");
        }
      }
    }

    // aeonrpg inspect <item/player>
    else if (isArgument(args, "inspect", 0)) {

      // aeonrpg inspect player
      if (isArgument(args, "item", 1)) {
      }

      // aeonrpg inspect player <target>
      else if (isArgument(args, "player", 1)) {

        if (hasArgument(args, 3)) {
        } else if (hasArgument(args, 2)) {
          for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getName().contains(args[2])) {
              result.add(player.getName());
            }
          }
        }
      }

      // aeonrpg edit ???
      else if (hasArgument(args, 1)) {
        if ("item".contains(args[1])) {
          result.add("item");
        }
        if ("player".contains(args[1])) {
          result.add("player");
        }
      }
    }

    // aeonrpg save [filename]
    else if (isArgument(args, "save", 0)) {
      if (hasArgument(args, 2)) {
      } else if (hasArgument(args, 1)) {
        result.add("<filename>");
      }
    }

    // aeonrpg load <filename>
    else if (isArgument(args, "load", 0)) {
      if (hasArgument(args, 2)) {
      } else if (hasArgument(args, 1)) {
        result.add("<filename>");
      }
    }

    else {
      if (hasArgument(args, 0) && !hasArgument(args, 1)) {
        if ("edit".contains(args[0])) {
          result.add("edit");
        }
        if ("inspect".contains(args[0])) {
          result.add("inspect");
        }
        if ("load".contains(args[0])) {
          result.add("load");
        }
        if ("save".contains(args[0])) {
          result.add("save");
        }
      }
    }

    return result;
  }
}
