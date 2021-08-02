package com.gmail.alexdion93.aeonrpg.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.KnowledgeBookMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.SpawnEggMeta;
import org.bukkit.inventory.meta.SuspiciousStewMeta;
import org.bukkit.inventory.meta.TropicalFishBucketMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import com.gmail.alexdion93.aeonrpg.AeonRPG;
import com.gmail.alexdion93.aeonrpg.data.interfaces.RPGDataString;
import com.gmail.alexdion93.aeonrpg.data.interfaces.RPGDataTwoValued;
import com.gmail.alexdion93.aeonrpg.data.interfaces.RPGDataValued;
import com.gmail.alexdion93.aeonrpg.data.type.RPGDataType;
import com.gmail.alexdion93.aeonrpg.managers.GenericRPGTypeManager;
import com.gmail.alexdion93.inventoryequipevent.events.PlayerEquipmentChangeEvent;
import com.gmail.alexdion93.inventoryequipevent.events.UpdateCause;

public class RPGDataUtil {

  private static AeonRPG plugin;
  private RPGDataUtil() {}
  
  /**
   * Edits data derived from RPGDataValued on an entity
   *
   * @param entity The entity being targetted
   * @param type   The type being added
   * @param value  The integer being assigned
   * @return A string detailing the success. Mostly used for command output.
   * @throws NullPointerException if the player is null
   * @throws NullPointerException if the data type cannot be found
   * @throws ClassCastException   if we cannot cast to the right type
   */
  public static String editData(LivingEntity entity, RPGDataType type, int value) {

    // Data Validation
    if (entity == null) {
      throw new NullPointerException("Item cannot be found");
    }
    if (type == null) {
      throw new NullPointerException("RPGDataType cannot be found");
    }
    if (!(type instanceof RPGDataValued)) {
      throw new ClassCastException("RPGDataType does not implement RPGDataValued");
    }

    // Variables
    String name = (entity.getCustomName() == null ? entity.getName() : entity.getCustomName());
    PersistentDataContainer data = entity.getPersistentDataContainer();
    NamespacedKey key = type.getNamespacedKey();
    String msg = ChatColor.YELLOW + "Added " + type.getDisplayName() + " to " + name;

    // Modifies the data on the entity, removing it if necessary
    if ((value == 0) || (!type.isNegativeAllowed() && (value < 0))) {
      data.remove(key);
      msg = (ChatColor.YELLOW + "Removed " + type.getDisplayName() + " from " + name);
    } else {
      data.set(key, PersistentDataType.INTEGER, value);
    }

    // Trigger any modifications to the entity based on the type
    type.modifyTarget(key, entity, value);

    // Call an event so plugins can detect this change
    if (entity instanceof Player) {
      Bukkit.getPluginManager()
          .callEvent(new PlayerEquipmentChangeEvent((Player) entity, null, EquipmentSlot.HAND, UpdateCause.OTHER));
    }

    // Return success or failure
    return msg;
  }

  /**
   * Edits data derived from RPGDataTwoValued on an entity
   *
   * @param entity   The item being targetted
   * @param type     The type being added
   * @param value    The integer being assigned
   * @param altValue The alternate integer being assigned
   * @return A string detailing the success. Mostly used for command output.
   * @throws NullPointerException if the entity is null
   * @throws NullPointerException if the data type cannot be found
   * @throws ClassCastException   if we cannot cast to the right type
   */
  public static String editData(LivingEntity entity, RPGDataType type, int value, int altValue) {

    // Data Validation
    if (entity == null) {
      throw new NullPointerException("Entity cannot be found");
    }
    if (type == null) {
      throw new NullPointerException("RPGDataType cannot be found");
    }
    if (!(type instanceof RPGDataTwoValued)) {
      throw new ClassCastException("RPGDataType does not implement RPGDataTwoValued");
    }

    // Variables
    String name = (entity.getCustomName() == null ? entity.getName() : entity.getCustomName());
    PersistentDataContainer data = entity.getPersistentDataContainer();
    RPGDataTwoValued altType = (RPGDataTwoValued) type;
    NamespacedKey key = type.getNamespacedKey();
    NamespacedKey altKey = altType.getAltNamespacedKey();
    String msg = ChatColor.YELLOW + "Added " + type.getDisplayName() + " to " + name;

    // Modifies the data on the entity, removing it if necessary
    if ((value == 0) || (!type.isNegativeAllowed() && (value < 0))) {
      data.remove(key);
      data.remove(altKey);
      msg = (ChatColor.YELLOW + "Removed " + type.getDisplayName() + " from " + name);
    } else {
      data.set(key, PersistentDataType.INTEGER, value);
      data.set(altKey, PersistentDataType.INTEGER, altValue);
    }

    // Trigger any modifications to the entity based on the type
    type.modifyTarget(entity, value);
    type.modifyTarget(altType.getAltNamespacedKey(), entity, altValue);

    // Call an event so plugins can detect this change
    if (entity instanceof Player) {
      Bukkit.getPluginManager()
          .callEvent(new PlayerEquipmentChangeEvent((Player) entity, null, EquipmentSlot.HAND, UpdateCause.OTHER));
    }

    // Return success or failure
    return msg;
  }

  /**
   * Edits data derived from RPGDataString on an entity
   *
   * @param entity The entity being targetted
   * @param type   The type being added
   * @param value  The string being assigned
   * @return A string detailing the success. Mostly used for command output.
   * @throws NullPointerException if the entity is null
   * @throws NullPointerException if the data type cannot be found
   * @throws ClassCastException   if we cannot cast to the right type
   */
  public static String editData(LivingEntity entity, RPGDataType type, String value) {

    // Data Validation
    if (entity == null) {
      throw new NullPointerException("Item cannot be found");
    }
    if (type == null) {
      throw new NullPointerException("RPGDataType cannot be found");
    }
    if (!(type instanceof RPGDataString)) {
      throw new ClassCastException("RPGDataType does not implement RPGDataString");
    }

    // Variables
    String name = (entity.getCustomName() == null ? entity.getName() : entity.getCustomName());
    PersistentDataContainer data = entity.getPersistentDataContainer();
    NamespacedKey key = type.getNamespacedKey();
    String msg = ChatColor.YELLOW + "Added " + type.getDisplayName() + " to " + name;

    // Modifies the data on the entity, removing it if necessary
    if ((value == null) || value.isEmpty()) {
      data.remove(key);
      msg = (ChatColor.YELLOW + "Removed " + type.getDisplayName() + " from " + name);
    } else {
      data.set(key, PersistentDataType.STRING, value);
    }

    // Trigger any modifications to the entity based on the type
    type.modifyTarget(entity, value);

    // Call an event so plugins can detect this change
    if (entity instanceof Player) {
      Bukkit.getPluginManager()
          .callEvent(new PlayerEquipmentChangeEvent((Player) entity, null, EquipmentSlot.HAND, UpdateCause.OTHER));
    }

    // Return success or failure
    return msg;
  }

  /**
   * Edits data derived from RPGDataValued on an item
   *
   * @param player The player being targetted
   * @param item   The item being targetted
   * @param type   The type being added
   * @param value  The integer being assigned
   * @return A string detailing the success. Mostly used for command output.
   * @throws NullPointerException     if the item is null
   * @throws IllegalArgumentException if the item is AIR
   * @throws NullPointerException     if the item has not meta
   * @throws NullPointerException     if the data type cannot be found
   * @throws ClassCastException       if we cannot cast to the right type
   */
  public static String editData(Player player, ItemStack item, RPGDataType type, int value) {

    // Data Validation
    if (item == null) {
      throw new NullPointerException("Item cannot be found");
    }
    if (item.getType() == Material.AIR) {
      throw new IllegalArgumentException("Item material cannot be air");
    }
    if (item.getItemMeta() == null) {
      throw new NullPointerException("Item has no item meta");
    }
    if (type == null) {
      throw new NullPointerException("RPGDataType cannot be found");
    }
    if (!(type instanceof RPGDataValued)) {
      throw new ClassCastException("RPGDataType does not implement RPGDataValued");
    }

    // Variables
    ItemMeta meta = item.getItemMeta();
    PersistentDataContainer data = meta.getPersistentDataContainer();
    RPGDataValued altType = (RPGDataValued) type;
    NamespacedKey key = type.getNamespacedKey();
    String msg = ChatColor.YELLOW + "Added " + type.getDisplayName() + " to the held item";

    // Modifies the data on the item, removing it if necessary
    if ((value == 0) || (!type.isNegativeAllowed() && (value < 0))) {
      data.remove(key);
      msg = (ChatColor.YELLOW + "Removed " + type.getDisplayName() + " from the held item");
    } else {
      data.set(key, PersistentDataType.INTEGER, value);
    }

    // Trigger any modifications to the item's meta based on the type
    altType.modifyItem(meta, item.getType(), value);

    // Update the item's lore.
    meta.setLore(generateLore(data));

    // Update the item meta
    item.setItemMeta(meta);

    // If necessary, call an event so the plugin can detect this change
    if (player != null) {
      Bukkit.getPluginManager()
          .callEvent(new PlayerEquipmentChangeEvent(player, item, EquipmentSlot.HAND, UpdateCause.OTHER));
    }

    // Return success or failure
    return msg;
  }

  /**
   * Edits data derived from RPGDataTwoValued on an item
   *
   * @param player   The player being targetted
   * @param item     The item being targetted
   * @param type     The type being added
   * @param value    The integer being assigned
   * @param altValue The alternate integer being assigned
   * @return A string detailing the success. Mostly used for command output.
   * @throws NullPointerException     if the item is null
   * @throws IllegalArgumentException if the item is AIR
   * @throws NullPointerException     if the item has not meta
   * @throws NullPointerException     if the data type cannot be found
   * @throws ClassCastException       if we cannot cast to the right type
   */
  public static String editData(Player player, ItemStack item, RPGDataType type, int value, int altValue) {

    // Data Validation
    if (item == null) {
      throw new NullPointerException("Item cannot be found");
    }
    if (item.getType() == Material.AIR) {
      throw new IllegalArgumentException("Item material cannot be air");
    }
    if (item.getItemMeta() == null) {
      throw new NullPointerException("Item has no item meta");
    }
    if (type == null) {
      throw new NullPointerException("RPGDataType cannot be found");
    }
    if (!(type instanceof RPGDataTwoValued)) {
      throw new ClassCastException("RPGDataType does not implement RPGDataTwoValued");
    }

    // Variables
    ItemMeta meta = item.getItemMeta();
    PersistentDataContainer data = meta.getPersistentDataContainer();
    RPGDataTwoValued altType = (RPGDataTwoValued) type;
    NamespacedKey key = type.getNamespacedKey();
    NamespacedKey altKey = altType.getAltNamespacedKey();
    String msg = ChatColor.YELLOW + "Added " + type.getDisplayName() + " to the held item";

    // Modifies the data on the item, removing it if necessary
    if ((value == 0) || (!type.isNegativeAllowed() && (value < 0))) {
      data.remove(key);
      data.remove(altKey);
      msg = (ChatColor.YELLOW + "Removed " + type.getDisplayName() + " from the held item");
    } else {
      data.set(key, PersistentDataType.INTEGER, value);
      data.set(altKey, PersistentDataType.INTEGER, altValue);
    }

    // Trigger any modifications to the item's meta based on the type
    altType.modifyItem(meta, item.getType(), value, altValue);

    // Update the item's lore.
    meta.setLore(generateLore(data));

    // Update the item meta
    item.setItemMeta(meta);

    // If necessary, call an event so the plugin can detect this change
    if (player != null) {
      Bukkit.getPluginManager()
          .callEvent(new PlayerEquipmentChangeEvent(player, item, EquipmentSlot.HAND, UpdateCause.OTHER));
    }

    // Return success or failure
    return msg;
  }

  /**
   * Edits data derived from RPGDataString on an item
   *
   * @param player The player being targetted
   * @param item   The item being targetted
   * @param type   The type being added
   * @param value  The string being assigned
   * @return A string detailing the success. Mostly used for command output.
   * @throws NullPointerException     if the item is null
   * @throws IllegalArgumentException if the item is AIR
   * @throws NullPointerException     if the item has not meta
   * @throws NullPointerException     if the data type cannot be found
   * @throws ClassCastException       if we cannot cast to the right type
   */
  public static String editData(Player player, ItemStack item, RPGDataType type, String value) {

    // Data Validation
    if (item == null) {
      throw new NullPointerException("Item cannot be found");
    }
    if (item.getType() == Material.AIR) {
      throw new IllegalArgumentException("Item material cannot be air");
    }
    if (item.getItemMeta() == null) {
      throw new NullPointerException("Item has no item meta");
    }
    if (type == null) {
      throw new NullPointerException("RPGDataType cannot be found");
    }
    if (!(type instanceof RPGDataString)) {
      throw new ClassCastException("RPGDataType does not implement RPGDataString");
    }

    // Variables
    ItemMeta meta = item.getItemMeta();
    PersistentDataContainer data = meta.getPersistentDataContainer();
    RPGDataString altType = (RPGDataString) type;
    NamespacedKey key = type.getNamespacedKey();
    String msg = ChatColor.YELLOW + "Added " + type.getDisplayName() + " to the held item";

    // Modifies the data on the item, removing it if necessary
    if ((value == null) || value.isEmpty()) {
      data.remove(key);
      msg = (ChatColor.YELLOW + "Removed " + type.getDisplayName() + " from the held item");
    } else {
      data.set(key, PersistentDataType.STRING, value);
    }

    // Trigger any modifications to the item's meta based on the type
    altType.modifyItem(meta, item.getType(), value);

    // Update the item's lore.
    meta.setLore(generateLore(data));

    // Update the item meta
    item.setItemMeta(meta);

    // If necessary, call an event so the plugin can detect this change
    if (player != null) {
      Bukkit.getPluginManager()
          .callEvent(new PlayerEquipmentChangeEvent(player, item, EquipmentSlot.HAND, UpdateCause.OTHER));
    }

    // Return success or failure
    return msg;
  }

  /**
   * Generates the appropriate lore for the item based on the data contained on
   * it. Any data types that provide an empty (length = 0) string "" will not have
   * their lore added to them.
   *
   * @param data   The data we're turning into lore
   * @return an array list of lore or null if its empty
   * @throws IllegalArgumentException if the plugin is null
   * @throws IllegalArgumentException if the data is null
   */
  public static ArrayList<String> generateLore(final PersistentDataContainer data) {

    // Data Validation
    if (plugin == null) {
      throw new IllegalArgumentException("RPGItemUtil wasn't initialized properly");
    }
    if (data == null) {
      throw new IllegalArgumentException("PersistentDataContainer cannot be null");
    }

    // Variables
    boolean entryAdded = false;
    final ArrayList<String> lore = new ArrayList<>();
    final ArrayList<GenericRPGTypeManager<?>> managers = plugin.getRPGDataTypeManager().getManagers();

    // Loop through each manager
    for (final GenericRPGTypeManager<?> manager : managers) {
      // Null Manager
      if (manager == null) {
        continue;
      }

      // Reset the added flag;
      entryAdded = false;

      // Null check
      if ((manager.getKeys() == null) || manager.getKeys().isEmpty()) {
        continue;
      }

      // Loop through each type
      for (final String key : manager.getKeys()) {

        // Get the Attribute
        final RPGDataType type = manager.get(key);

        // Skip conditions
        if (type == null) {
          continue;
        }

        // Two Valued Types
        if (type instanceof RPGDataTwoValued) {

          // Cast as the correct type
          final RPGDataTwoValued altType = ((RPGDataTwoValued) type);

          // Fetch the value
          if (!data.has(type.getNamespacedKey(), PersistentDataType.INTEGER)) {
            continue;
          }
          final int value = data.get(type.getNamespacedKey(), PersistentDataType.INTEGER);

          // Fetch the alt value
          if (!data.has(altType.getAltNamespacedKey(), PersistentDataType.INTEGER)) {
            continue;
          }
          final int altValue = data.get(altType.getAltNamespacedKey(), PersistentDataType.INTEGER);

          // Generate and add the string
          for (String loreString : altType.toLoreString(value, altValue).split("\n")) {
            if (loreString.isEmpty()) {
              continue;
            }
            lore.add(loreString);
          }
        }

        // One Valued Types
        else if (type instanceof RPGDataValued) {

          // Cast as the correct type
          final RPGDataValued altType = ((RPGDataValued) type);

          // Fetch the value
          if (!data.has(type.getNamespacedKey(), PersistentDataType.INTEGER)) {
            continue;
          }
          final int value = data.get(type.getNamespacedKey(), PersistentDataType.INTEGER);

          // Generate and add the string
          for (String loreString : altType.toLoreString(value).split("\n")) {
            if (loreString.isEmpty()) {
              continue;
            }
            lore.add(loreString);
          }
        }

        // String Valued Types
        else if (type instanceof RPGDataString) {

          // Cast as the correct type
          final RPGDataString altType = ((RPGDataString) type);

          // Fetch the value
          if (!data.has(type.getNamespacedKey(), PersistentDataType.STRING)) {
            continue;
          }
          final String value = data.get(type.getNamespacedKey(), PersistentDataType.STRING);

          // Generate and add the string
          for (String loreString : altType.toLoreString(value).split("\n")) {
            if (loreString.isEmpty()) {
              continue;
            }
            lore.add(loreString);
          }
        }

        // Toggle the flag
        entryAdded = true;
      }

      // Between each category we should add a space
      if (entryAdded) {
        lore.add(" ");
      }
    }

    // Return null if no lore
    if (lore.isEmpty()) {
      return null;
    }

    // Remove trailing empty lines
    while (lore.get(lore.size() - 1).trim().isEmpty()) {
      lore.remove(lore.size() - 1);
    }

    // Return
    return lore;
  }

  /**
   * Initializes this utility class
   *
   * @param plugin The plugin tied to this class.
   */
  public static void init(AeonRPG plugin) {
    RPGDataUtil.plugin = plugin;
  }

  /**
   * Attempts to save an item to the provided path
   *
   * @param path   The path we'll be saving to
   * @throws NullPointerException if the path is null
   * @return An item stack
   */
  public static ItemStack loadItem(String path) {

    // Data Validation
    if (path == null) {
      throw new NullPointerException("File path cannot be null");
    }

    // Variables
    File file = new File(plugin.getDataFolder(), path);
    YamlConfiguration config = null;
    ItemMeta meta;
    PersistentDataContainer data;
    ItemStack item = null;

    // Exit if file doesn't exist
    if (!file.exists()) {
      return item;
    }

    // Initialize Config
    config = new YamlConfiguration();

    // Attempt to load the file
    try {
      config.load(file);
    } catch (IOException | InvalidConfigurationException e) {
      e.printStackTrace();
      return item;
    }

    // Initialize Item
    if (!config.contains("Material")) {
      return item;
    }

    item = new ItemStack(Material.getMaterial(config.getString("Material", "AIR").toUpperCase()));
    if (item.getType() == Material.AIR) {
      return item;
    }

    // Fetch Item Meta
    meta = item.getItemMeta();

    // Load Custom Model Data
    if (config.contains("Meta.Custom Model Data")) {
      meta.setCustomModelData(config.getInt("Meta.Custom Model Data"));
    }

    // Load Display Name
    if (config.contains("Meta.Display Name")) {
      meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', config.getString("Meta.Display Name")));
    }

    // Load unbreakable status
    if (config.contains("Meta.Unbreakable")) {
      meta.setUnbreakable(config.getBoolean("Meta.Unbreakable"));
    }

    // Load Enchantments
    if (config.contains("Meta.Enchantment")) {
      for (Enchantment enchantment : Enchantment.values()) {
        String enchPath = "Meta.Enchantment." + enchantment.getKey().getKey();
        if (!config.contains(enchPath)) {
          continue;
        }
        meta.addEnchant(enchantment, config.getInt(enchPath), true);
      }
    }

    // Load Item Flags
    if (config.contains("Meta.Item Flag")) {
      for (String value : config.getStringList("Meta.Item Flag")) {
        meta.addItemFlags(ItemFlag.valueOf(value));
      }
    }

    // Load Lore
    if (config.contains("Meta.Lore")) {

      ArrayList<String> lore = new ArrayList<String>();
      for (String line : config.getStringList("Meta.Lore")) {
        lore.add(ChatColor.translateAlternateColorCodes('&', line));
      }

      meta.setLore(lore);
    }

    // TODO: Handle specific meta types
    if (meta instanceof BannerMeta) {
    } else if (meta instanceof BookMeta) {
    } else if (meta instanceof CompassMeta) {
    } else if (meta instanceof CrossbowMeta) {
    } else if (meta instanceof FireworkEffectMeta) {
    } else if (meta instanceof KnowledgeBookMeta) {
    } else if (meta instanceof LeatherArmorMeta) {
    } else if (meta instanceof MapMeta) {
    } else if (meta instanceof PotionMeta) {
    } else if (meta instanceof SkullMeta) {
    } else if (meta instanceof SpawnEggMeta) {
    } else if (meta instanceof SuspiciousStewMeta) {
    } else if (meta instanceof TropicalFishBucketMeta) {
    }

    // Load Attributes
    if (config.contains("Attribute")) {

      for (String name : config.getConfigurationSection("Attribute").getKeys(false)) {
        for (String uuid : config.getConfigurationSection("Attribute." + name).getKeys(false)) {
          meta.addAttributeModifier(Attribute.valueOf(name),
              new AttributeModifier(UUID.randomUUID(),
                  config.getString("Attribute." + name + "." + uuid + ".NAME", null),
                  config.getDouble("Attribute." + name + "." + uuid + ".AMOUNT", 0.0),
                  Operation.valueOf(config.getString("Attribute." + name + "." + uuid + ".OPERATION", null)),
                  EquipmentSlot.valueOf(config.getString("Attribute." + name + "." + uuid + ".SLOT", null))));
        }
      }
    }

    // Load RPG Data
    if (config.contains("Data")) {

      data = meta.getPersistentDataContainer();

      for (GenericRPGTypeManager<?> manager : plugin.getRPGDataTypeManager().getManagers()) {
        for (String key : manager.getKeys()) {
          if (!config.contains("Data." + key)) {
            continue;
          }
          RPGDataType type = manager.get(key);
          data.set(type.getNamespacedKey(), PersistentDataType.INTEGER, config.getInt("Data." + key));

          if (!config.contains("Data." + key + ".Alt")) {
            continue;
          }
          if (type instanceof RPGDataTwoValued) {
            data.set(((RPGDataTwoValued) type).getAltNamespacedKey(), PersistentDataType.INTEGER,
                config.getInt("Data." + key + ".Alt"));
          }
        }
      }
    }

    // Update the meta
    item.setItemMeta(meta);

    // Return
    return item;

  }

  /**
   * Causes all uuids stored within an item's attributes to be randomized. The
   * goal is to solve an issue where items with the same attribute uuid not
   * stacking effects with each other.
   *
   * @param item The itemstack we're modifying
   * @return The modified item stack
   * @throws NullPointerException if the item is null
   */
  public static ItemStack randomizeAttributeUUID(ItemStack item) {

    // Data Validation
    if (item == null) {
      throw new NullPointerException("Provided item cannot be null");
    }
    ItemMeta meta = item.getItemMeta();
    if (meta == null) {
      throw new NullPointerException("Provided item lacks meta data");
    }

    // Null Attribute Check
    if (meta.getAttributeModifiers() == null) {
      return item;
    }

    // Loop through all attributes and their modifiers
    // Because the lists provided are immutable we should be able to make
    // modifications within this loop.
    for (Attribute attribute : meta.getAttributeModifiers().keySet()) {

      for (AttributeModifier modifier : meta.getAttributeModifiers(attribute)) {

        // Add a copy instance
        meta.addAttributeModifier(attribute,
            new AttributeModifier(modifier.getName(), modifier.getAmount(), modifier.getOperation()));

        // Remove this instance of the attribute
        meta.removeAttributeModifier(attribute, modifier);
      }
    }

    // Update the item
    item.setItemMeta(meta);

    // Return
    return item;
  }

  /**
   * Attempts to save an item to the provided path
   *
   * @param path   The path we'll be saving to
   * @param item   The itemstack we'll be saving
   * @throws NullPointerException if the file path is null
   * @throws NullPointerException if the itemstack is null
   */
  public static void saveItem(String path, ItemStack item) {

    // Data Validation
    if (path == null) {
      throw new NullPointerException("Provided filepath cannot be null");
    }
    if (item == null) {
      throw new NullPointerException("Provided itemstack cannot be null");
    }

    // Variables
    File file = new File(plugin.getDataFolder(), path);
    YamlConfiguration config = null;
    ItemMeta meta;
    PersistentDataContainer data;

    // Create the folder if it doesn't exist
    if (!file.exists()) {
      file.getParentFile().mkdirs();
    }

    // Initialize Config
    config = new YamlConfiguration();

    // Save Basic Information
    config.set("Material", item.getType().name());

    // Fetch Item Meta
    meta = item.getItemMeta();

    // Save Custom Model Data
    if (meta.hasCustomModelData()) {
      config.set("Meta.Custom Model Data", meta.getCustomModelData());
    }

    // Save Display Name
    if (meta.hasDisplayName()) {
      config.set("Meta.Display Name", meta.getDisplayName());
    }

    // Save Unbreakable Status
    config.set("Meta.Unbreakable", meta.isUnbreakable());

    // Save Enchantments
    if (meta.hasEnchants()) {
      for (Enchantment enchantment : meta.getEnchants().keySet()) {
        config.set("Meta.Enchantment." + enchantment.getKey().getKey(), meta.getEnchantLevel(enchantment));
      }
    }

    // Save Item Flags
    if (meta.getItemFlags().size() != 0) {
      ArrayList<String> flags = new ArrayList<String>();
      for (ItemFlag flag : meta.getItemFlags()) {
        flags.add(flag.name());
      }
      config.set("Meta.Item Flag", flags);
    }

    // Save Lore
    if (meta.hasLore()) {
      ArrayList<String> lore = new ArrayList<String>();
      for (String line : meta.getLore()) {
        lore.add(line.replace('§', '&'));
      }
      config.set("Meta.Lore", lore);
    }

    // TODO: Handle specific meta types
    if (meta instanceof BannerMeta) {
    } else if (meta instanceof BookMeta) {
    } else if (meta instanceof CompassMeta) {
    } else if (meta instanceof CrossbowMeta) {
    } else if (meta instanceof FireworkEffectMeta) {
    } else if (meta instanceof KnowledgeBookMeta) {
    } else if (meta instanceof LeatherArmorMeta) {
    } else if (meta instanceof MapMeta) {
    } else if (meta instanceof PotionMeta) {
    } else if (meta instanceof SkullMeta) {
    } else if (meta instanceof SpawnEggMeta) {
    } else if (meta instanceof SuspiciousStewMeta) {
    } else if (meta instanceof TropicalFishBucketMeta) {
    }

    // Save Attributes
    if (meta.hasAttributeModifiers()) {
      for (Attribute attribute : Attribute.values()) {
        int counter = 0;
        Collection<AttributeModifier> modifiers = meta.getAttributeModifiers(attribute);
        if (modifiers == null) {
          continue;
        }

        for (AttributeModifier modifier : modifiers) {
          config.set("Attribute." + attribute.name() + "." + counter + ".NAME", modifier.getName());
          config.set("Attribute." + attribute.name() + "." + counter + ".OPERATION", modifier.getOperation().name());
          config.set("Attribute." + attribute.name() + "." + counter + ".SLOT", modifier.getSlot().name());
          config.set("Attribute." + attribute.name() + "." + counter + ".AMOUNT", modifier.getAmount());
          counter++;
        }
      }
    }

    // Save RPG Data
    data = meta.getPersistentDataContainer();
    for (GenericRPGTypeManager<?> manager : plugin.getRPGDataTypeManager().getManagers()) {

      for (String key : manager.getKeys()) {
        RPGDataType type = manager.get(key);
        config.set("Data." + key, data.get(type.getNamespacedKey(), PersistentDataType.INTEGER));

        if (type instanceof RPGDataTwoValued) {
          config.set("Data." + key + ".Alt",
              data.get(((RPGDataTwoValued) type).getAltNamespacedKey(), PersistentDataType.INTEGER));
        }
      }
    }

    // Save
    try {
      config.save(file);
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  /**
   * Copies persistent data between two containers
   * @param source The source of the data
   * @param target The destination of the data
   */
  public static void copyData(PersistentDataContainer source, PersistentDataContainer target) {
    
    for(NamespacedKey key : source.getKeys()) {
      
      if (source.has(key, PersistentDataType.BYTE)) {
        target.set(key, PersistentDataType.BYTE, source.get(key, PersistentDataType.BYTE));
      }
      
      else if (source.has(key, PersistentDataType.BYTE_ARRAY)) {
        target.set(key, PersistentDataType.BYTE_ARRAY, source.get(key, PersistentDataType.BYTE_ARRAY));
      }
      
      else if (source.has(key, PersistentDataType.DOUBLE)) {
        target.set(key, PersistentDataType.DOUBLE, source.get(key, PersistentDataType.DOUBLE));
      }
      
      else if (source.has(key, PersistentDataType.FLOAT)) {
        target.set(key, PersistentDataType.FLOAT, source.get(key, PersistentDataType.FLOAT));
      }
      
      else if (source.has(key, PersistentDataType.INTEGER)) {
        target.set(key, PersistentDataType.INTEGER, source.get(key, PersistentDataType.INTEGER));
      }
      
      else if (source.has(key, PersistentDataType.INTEGER_ARRAY)) {
        target.set(key, PersistentDataType.INTEGER_ARRAY, source.get(key, PersistentDataType.INTEGER_ARRAY));
      }
      
      else if (source.has(key, PersistentDataType.LONG)) {
        target.set(key, PersistentDataType.LONG, source.get(key, PersistentDataType.LONG));
      }
      
      else if (source.has(key, PersistentDataType.LONG_ARRAY)) {
        target.set(key, PersistentDataType.LONG_ARRAY, source.get(key, PersistentDataType.LONG_ARRAY));
      }
      
      else if (source.has(key, PersistentDataType.SHORT)) {
        target.set(key, PersistentDataType.SHORT, source.get(key, PersistentDataType.SHORT));
      }
      
      else if (source.has(key, PersistentDataType.STRING)) {
        target.set(key, PersistentDataType.STRING, source.get(key, PersistentDataType.STRING));
      }
      
      else if (source.has(key, PersistentDataType.TAG_CONTAINER)) {
        target.set(key, PersistentDataType.TAG_CONTAINER, source.get(key, PersistentDataType.TAG_CONTAINER));
      }
      
      else if (source.has(key, PersistentDataType.TAG_CONTAINER_ARRAY)) {
        target.set(key, PersistentDataType.TAG_CONTAINER_ARRAY, source.get(key, PersistentDataType.TAG_CONTAINER_ARRAY));
      }
    }
  }
}
