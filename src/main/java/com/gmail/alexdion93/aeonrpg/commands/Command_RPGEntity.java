package com.gmail.alexdion93.aeonrpg.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import com.gmail.alexdion93.aeonrpg.AeonRPG;
import com.gmail.alexdion93.aeonrpg.data.interfaces.RPGDataString;
import com.gmail.alexdion93.aeonrpg.data.interfaces.RPGDataTwoValued;
import com.gmail.alexdion93.aeonrpg.data.type.RPGDataType;
import com.gmail.alexdion93.aeonrpg.managers.GenericRPGTypeManager;

/**
 * A command that allows the player to create, remove and modify stored data for
 * entities as well as spawn entities with that data.
 * @author Alex Dion
 *
 */
public class Command_RPGEntity implements CommandExecutor, TabCompleter {
  private AeonRPG plugin;
  private ArrayList<String> keys;

  /**
   * Constructor
   *
   * @param plugin the plugin tied to this
   */
  public Command_RPGEntity(AeonRPG plugin) {
    this.plugin = plugin;
    keys = new ArrayList<>();
    plugin.getCommand("rpgentity").setExecutor(this);
    plugin.getCommand("rpgentity").setTabCompleter(this);
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

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    // Exit
    return true;
  }

  @Override
  public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
    // Variables
    ArrayList<String> result = new ArrayList<String>();
    updateKeys();
    
    //End
    return result;
  }
}
