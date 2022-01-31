package com.gmail.alexdion93.aeonrpg.util;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Axolotl;
import org.bukkit.entity.Player;
import org.bukkit.entity.TropicalFish;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.AxolotlBucketMeta;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.BundleMeta;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.KnowledgeBookMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.Repairable;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.SuspiciousStewMeta;
import org.bukkit.inventory.meta.TropicalFishBucketMeta;
import org.bukkit.inventory.meta.BookMeta.Generation;
import org.bukkit.map.MapView;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import com.gmail.alexdion93.aeonrpg.AeonRPG;
import com.gmail.alexdion93.aeonrpg.data.interfaces.*;
import com.gmail.alexdion93.aeonrpg.data.type.*;
import com.gmail.alexdion93.aeonrpg.data.type.RPGDataType.RPGDataAlignment;
import com.gmail.alexdion93.aeonrpg.managers.*;

public class RPGDataUtil {

  private static AeonRPG plugin;
  
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
  public static ArrayList<String> generateLore(PersistentDataContainer data) {

    // Data Validation
    if (plugin == null) {
      throw new IllegalArgumentException("RPGItemUtil wasn't initialized properly");
    }
    if (data == null) {
      throw new IllegalArgumentException("PersistentDataContainer cannot be null");
    }

    // Variables
    boolean entryAdded = false;
    ArrayList<String> lore = new ArrayList<>();
    ArrayList<GenericRPGTypeManager<?>> managers = plugin.getRPGDataTypeManager().getManagers();

    // Loop through each manager
    for (GenericRPGTypeManager<?> manager : managers) {
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
      for (String key : manager.getKeys()) {

        // Get the Attribute
        RPGDataType type = manager.get(key);

        // Skip conditions
        if (type == null) { continue; }
        
        // Two Valued Types
        if (type instanceof RPGDataTwoValued) {
          
          // Cast as the correct type
          RPGDataTwoValued altType = ((RPGDataTwoValued) type);
          int value = data.getOrDefault(altType.getPrimaryKey(), PersistentDataType.INTEGER, 0);
          int altValue = data.getOrDefault(altType.getSecondaryKey(), PersistentDataType.INTEGER, 0);

          //Ensure one value is present
          if (!data.has(altType.getPrimaryKey(), PersistentDataType.INTEGER) &&
              !data.has(altType.getSecondaryKey(), PersistentDataType.INTEGER)) {
            continue;
          }
          
          // Generate and add the string
          for (String loreString : type.toLoreString(value, altValue).split("\n")) {
            if (loreString.isEmpty()) { continue; }
            lore.add(loreString);
          }
        }

        // One Valued Types
        else if (type instanceof RPGDataOneValued) {

          // Cast as the correct type
          RPGDataOneValued altType = ((RPGDataOneValued) type);

          // Fetch the value
          if (!data.has(altType.getPrimaryKey(), PersistentDataType.INTEGER)) {
            continue;
          }
          int value = data.get(altType.getPrimaryKey(), PersistentDataType.INTEGER);

          // Generate and add the string
          for (String loreString : type.toLoreString(value).split("\n")) {
            if (loreString.isEmpty()) {
              continue;
            }
            lore.add(loreString);
          }
        }

        // String Valued Types
        else if (type instanceof RPGDataString) {

          // Cast as the correct type
          RPGDataString altType = ((RPGDataString) type);

          // Fetch the value
          if (!data.has(altType.getStringKey(), PersistentDataType.STRING)) {
            continue;
          }
          String value = data.get(altType.getStringKey(), PersistentDataType.STRING);

          // Generate and add the string
          for (String loreString : type.toLoreString(value).split("\n")) {
            if (loreString.isEmpty()) { continue; }
            lore.add(loreString);
          }
        }

        // Toggle the flag
        entryAdded = true;
      }

      // Between each category we should add a space
      if (entryAdded) { lore.add(" "); }
    }

    // Return null if no lore
    if (lore.isEmpty()) { return null; }

    // Remove trailing empty lines
    while (!lore.isEmpty() && lore.get(lore.size() - 1).trim().isEmpty()) {
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
   * Loads an item stack from a YAML config file.
   * @param section The configuration section we're reading from.
   * @param path The path we're reading from
   * @return An itemstack or null
   */
  @SuppressWarnings("deprecation")
  public static ItemStack loadItem(ConfigurationSection section) {
    
    //Validate the material and amount
    ItemStack item = null;
    int amount = section.getInt("amount", 0);
    Material material = Material.matchMaterial(section.getString("material", "AIR"));
    
    if (amount <= 0 || material == null) { return null; }
    if (material == Material.AIR || material == Material.CAVE_AIR || material == Material.VOID_AIR) { return null; }
    
    //Create the item
    item = new ItemStack(material, amount);
    
    //Enchantments
    for(Enchantment ench : Enchantment.values()) {
      int level = section.getInt("enchantment." + ench.getKey().getKey(), 0);
      if (level == 0) { continue; }
      item.addUnsafeEnchantment(ench, level);
    }
    
    //Fetch Item Meta
    ItemMeta meta = item.getItemMeta();
    
    //Attributes
    if (section.isConfigurationSection("attribute")) {
      ConfigurationSection subsection = section.getConfigurationSection(".attribute");
      for(String key : subsection.getKeys(false)) {
        
        Attribute attribute = Attribute.valueOf(subsection.getString(key + ".type", ""));
        String name = subsection.getString(key + ".name", "Name");
        double value = subsection.getDouble(key + ".amount", 0.0);
        Operation operation = Operation.valueOf(subsection.getString(key + ".operation", "N/A"));
        EquipmentSlot slot = null;
        try { slot = EquipmentSlot.valueOf(subsection.getString(key + ".slot", "N/A")); }
        catch (Exception e) {/* Ignore */}
        
        if (slot == null || operation == null) { continue; }
        AttributeModifier modifier = new AttributeModifier(UUID.randomUUID(), name, value, operation, slot);
        
        if (attribute == null || modifier == null) { continue; }
        meta.addAttributeModifier(attribute, modifier);
      }
    }
    
    //Custom Model Data
    if (section.isInt("custom model data")) {
      meta.setCustomModelData(section.getInt("custom model data", 0));
    }
    
    //Display Name
    if (section.isString("name")) {
      meta.setDisplayName(section.getString("name"));
    }
    
    //Lore
    if (section.isList("lore")) {
      meta.setLore(section.getStringList("lore"));
    }
    
    //Item Flags
    if (section.isList("item flags")) {
      for(String flag : section.getStringList("item flags")) {
        ItemFlag f = ItemFlag.valueOf(flag);
        if (f == null) { continue; }
        meta.addItemFlags(f);
      }
    }
    
    //Unbreakable
    meta.setUnbreakable(section.getBoolean("unbreakable", false));
    
    //Special Meta
    if (meta instanceof AxolotlBucketMeta) {
      AxolotlBucketMeta m = (AxolotlBucketMeta) meta;
      Axolotl.Variant variant = Axolotl.Variant.valueOf(section.getString("axolotl", "N/A"));
      if (variant != null) { m.setVariant(variant); }
    }
    
    if (meta instanceof BannerMeta) {
      BannerMeta m = (BannerMeta) meta;
      
      for(int i = 0; i < m.getPatterns().size(); i++) {
        PatternType type = PatternType.valueOf(section.getString("banner." + i + ".name", "N/A"));
        DyeColor color = DyeColor.valueOf(section.getString("banner." + i + ".color", "N/A"));
        if (type == null || color == null) { continue; }
        m.addPattern(new Pattern(color, type));
        
      }
    }
    
    if (meta instanceof BookMeta) {
      BookMeta m = (BookMeta) meta;
      
      
      m.setAuthor(section.getString("book.author", "Unknown"));
      m.setGeneration(Generation.valueOf(section.getString("book.generation","TATTERED")));
      if (section.isList("book.pages")) {
        m.setPages(section.getStringList("book.pages"));
      }
      m.setTitle(section.getString("book.title", "Untitled Journal"));
    }
    
    if (meta instanceof BundleMeta) {
      BundleMeta m = (BundleMeta) meta;
      ConfigurationSection subsection = section.getConfigurationSection("bundle");
      if (subsection != null) {
        for(String key : subsection.getKeys(false)) {
          ItemStack temp = loadItem(subsection.getConfigurationSection("" + key));
          if (temp == null) { continue; } 
          m.addItem(item);
        }
      }
    }
    
    if (meta instanceof CompassMeta) {
      CompassMeta m = (CompassMeta) meta;
      
      m.setLodestoneTracked(section.getBoolean("book.compass.lodestone.tracked", false));
      
      int x = section.getInt("book.compass.lodestone.x", 0);
      int y = section.getInt("book.compass.lodestone.y", 0);
      int z = section.getInt("book.compass.lodestone.z", 0);
      World world = Bukkit.getWorld(section.getString("book.compass.lodestone.world", "world"));
      if (world != null) { m.setLodestone( new Location(world,x,y,z)); }
    }
    
    if (meta instanceof CrossbowMeta) {
      CrossbowMeta m = (CrossbowMeta) meta;
      
      if (section.isConfigurationSection("crossbow")) {
        ConfigurationSection subsection = section.getConfigurationSection("crossbow");
        for(String key : subsection.getKeys(false)) {
          ItemStack stack = loadItem(subsection.getConfigurationSection("" + key));
          if (stack != null) { m.addChargedProjectile(item); }
        }
      }
    }
    
    if (meta instanceof Damageable) {
      Damageable m = (Damageable) meta;
      m.setDamage(section.getInt("damage", 0));
    }
    
    if (meta instanceof EnchantmentStorageMeta) {
      EnchantmentStorageMeta m = (EnchantmentStorageMeta) meta;
      for(Enchantment ench : Enchantment.values()) {
        if (section.isInt("stored enchantments" + ench.getKey().getKey())) {
          m.addEnchant(ench, section.getInt("stored enchantments" + ench.getKey().getKey(), 1), true);
        }
      }
    }
    
    if (meta instanceof FireworkEffectMeta) {
      FireworkEffectMeta m = (FireworkEffectMeta) meta;
      FireworkEffect.Builder builder = FireworkEffect.builder();
      
      if (section.isString("firework effect.type")) {
        builder.with(FireworkEffect.Type.valueOf(section.getString("firework effect.type")));
      }
      
      if (section.isList("firework effect.colors")) {
        for(int color : section.getIntegerList("firework effect.colors")) {
          builder.withColor(Color.fromRGB(color));
        }
      }
      
      if (section.isList("firework effect.fadeColors")) {
        for(int color : section.getIntegerList("firework effect.fadeColors")) {
          builder.withFade(Color.fromRGB(color));
        }
      }
      
      builder.flicker(section.getBoolean("firework effect.flicker", false));
      builder.trail(section.getBoolean("firework effect.trail", false));
      
      try {
        FireworkEffect e = builder.build();
        if (e != null) { m.setEffect(e); }
      }
      catch(Exception e) { e.printStackTrace(); }
    }
    
    if (meta instanceof FireworkMeta) {
      FireworkMeta m = (FireworkMeta) meta;
      ConfigurationSection subsection = section.getConfigurationSection("firework.effects");
      FireworkEffect.Builder builder;
      
      m.setPower(section.getInt("firework.power", 1));
      
      for(String key : subsection.getKeys(false)) {
        builder = FireworkEffect.builder();
        
        if (subsection.isString(key + ".type")) {
          builder.with(FireworkEffect.Type.valueOf(subsection.getString(key + ".type")));
        }
        
        if (subsection.isList(key + ".colors")) {
          for(int color : subsection.getIntegerList(key + ".colors")) {
            builder.withColor(Color.fromRGB(color));
          }
        }
        
        if (subsection.isList(key + ".fadeColors")) {
          for(int color : subsection.getIntegerList(key + ".fadeColors")) {
            builder.withFade(Color.fromRGB(color));
          }
        }
        
        builder.flicker(subsection.getBoolean(key + ".flicker", false));
        builder.trail(subsection.getBoolean(key + ".trail", false));
        
        try {
          FireworkEffect e = builder.build();
          if (e != null) { m.addEffect(e); }
        }
        catch(Exception e) { e.printStackTrace(); }
      }
    }
    
    if (meta instanceof KnowledgeBookMeta) {
      KnowledgeBookMeta m = (KnowledgeBookMeta) meta;
     if (section.isList("recipies")) for(String key : section.getStringList("recipies")) {
       NamespacedKey nsk = NamespacedKey.minecraft(key);
       if (nsk != null) { m.addRecipe(nsk); }
     }
    }
    
    if (meta instanceof LeatherArmorMeta) {
      LeatherArmorMeta m = (LeatherArmorMeta) meta;
      if (section.isInt("leather")) {
        m.setColor(Color.fromRGB(section.getInt("leather",0)));
      }
    }
    
    if (meta instanceof MapMeta) {
      MapMeta m = (MapMeta) meta;
      
      if (section.isInt("map.color")) {
        m.setColor(Color.fromRGB(section.getInt("map.color", 0)));
      }
      
      if (section.isString("map.name")) {
        m.setLocationName(section.getString("map.name", "Unknown Location"));
      }
      
      if (section.isInt("map id")) {
        m.setMapView(Bukkit.getMap(section.getInt("map id", 0)));
      }
    }
    
    if (meta instanceof PotionMeta) {
      PotionMeta m = (PotionMeta) meta;
      PotionType pType = PotionType.UNCRAFTABLE;
      try {
        pType = PotionType.valueOf(section.getString("potion.type", ""));
      }
      catch (Exception e) { /* Ignore */ }
      
      boolean extended = section.getBoolean("potion.extended", false);
      boolean upgraded = section.getBoolean("potion.upgraded", false);
      m.setBasePotionData(new PotionData(pType, extended, upgraded));
      
      ConfigurationSection subsection = section.getConfigurationSection("potion.custom");
      if (subsection != null) {
        
        for(String key : subsection.getKeys(false)) {
          
          PotionEffectType type = PotionEffectType.getByName(subsection.getString(key + ".amplifier", ""));
          int duration = subsection.getInt(key + ".duration", 0);
          int amplifier = subsection.getInt(key + ".amplifier", 0);
          
          if (type == null) { continue; }
          m.addCustomEffect(new PotionEffect(type, duration, amplifier), false);
        }
      }
    }
    
    if (meta instanceof Repairable) {
      Repairable m = (Repairable) meta;
      m.setRepairCost(section.getInt("repair cost", 0));
    }
    
    if (meta instanceof SkullMeta) {
      SkullMeta m = (SkullMeta) meta;
      if (section.contains("skull")) {
        m.setOwningPlayer(Bukkit.getOfflinePlayer(UUID.fromString(section.getString("skull"))));
      }
    }
    
    if (meta instanceof SuspiciousStewMeta) {
      SuspiciousStewMeta m = (SuspiciousStewMeta) meta;
      
      for(PotionEffectType type : PotionEffectType.values()) {
        int amplifier = section.getInt("stew." + type.getName() + ".amplifier", 0);
        int duration = section.getInt("stew." + type.getName() + ".duration", 0);
        
        if (!section.contains("stew." + type.getName())) { continue; }
        m.addCustomEffect(new PotionEffect(type, duration, amplifier), true);
      }
    }
    
    if (meta instanceof TropicalFishBucketMeta) {
      TropicalFishBucketMeta m = (TropicalFishBucketMeta) meta;
      
      if (section.isString("fish.body")) {
        m.setBodyColor(DyeColor.valueOf(section.getString("fish.body")));
      }
      
      if (section.isString("fish.pattern.body")) {
        m.setPattern(TropicalFish.Pattern.valueOf(section.getString("fish.pattern.body")));
      }
      
      if (section.isString("fish.pattern.color")) {
        m.setPatternColor(DyeColor.valueOf(section.getString("fish.pattern.color")));
      }
    }
    
    // Load RPG Data
    if (section.contains("data")) {
      // Variables
      PersistentDataContainer data = meta.getPersistentDataContainer();

      //Load the rpg data
      loadRPGData(section.getConfigurationSection("data"), data);
      
      //Generate the lore
      meta.setLore(generateLore(data));
    }
    
    //Update Item Meta
    item.setItemMeta(meta);
    
    //Return the item
    return item;
  }
  
  /**
   * Loadsd rpg data from the provided configuration section.
   * @param section The configuration section.
   * @param data The data container.
   */
  public static void loadRPGData(ConfigurationSection section, PersistentDataContainer data) {
    
    //Attributes
    GenericRPGTypeManager<RPGAttributeType> attributes = plugin.getRPGDataTypeManager().getAttributeManager();
    for (String key : attributes.getKeys()) {
      RPGAttributeType type = attributes.get(key);
      int flat = section.getInt(key + ".flat", 0);
      int scaling = section.getInt(key + ".scaling", 0);
      
      type.setFlat(data, flat);
      type.setScaling(data, scaling);
    }
    
    //Enchantments
    GenericRPGTypeManager<RPGEnchantmentType> enchantments = plugin.getRPGDataTypeManager().getEnchantmentManager();
    for (String key : enchantments.getKeys()) {
      RPGEnchantmentType type = enchantments.get(key);
      int level = section.getInt(key + ".level", 0);
      
      type.setLevel(data, level);
    }
    
    //Generics
    GenericRPGTypeManager<RPGDataType> generics = plugin.getRPGDataTypeManager().getGenericManager();
    for (String key : generics.getKeys()) {
      RPGDataType type = generics.get(key);
      NamespacedKey nsk;
      
      //Load Primary Data
      if (type instanceof RPGDataOneValued) {
        
        RPGDataOneValued alt = (RPGDataOneValued) type;
        int value = section.getInt(key + ".primary", 0);
        nsk = alt.getPrimaryKey();
        
        if (value == 0) { type.remove(data, nsk, PersistentDataType.INTEGER, 0); }
        else { type.set(data, nsk, PersistentDataType.INTEGER, value); }
      }
      
      //Load Secondary Data
      if (type instanceof RPGDataTwoValued) {
        
        RPGDataTwoValued alt = (RPGDataTwoValued) type;
        int value = section.getInt(key + ".primary", 0);
        nsk = alt.getSecondaryKey();
        
        if (value == 0) { type.remove(data, nsk, PersistentDataType.INTEGER, 0); }
        else { type.set(data, nsk, PersistentDataType.INTEGER, value); }
      }

      //Load Data String
      if (type instanceof RPGDataString) {
        RPGDataString alt = (RPGDataString) type;
        String value = section.getString(key + ".string", null);
        nsk = alt.getStringKey();
        
        type.set(data, nsk, PersistentDataType.STRING, value);
      }
    }
    
    //Potion Effects
    GenericRPGTypeManager<RPGPotionEffectType> potionEffects = plugin.getRPGDataTypeManager().getPotionEffectManager();
    for (String key : potionEffects.getKeys()) {
      RPGPotionEffectType type = potionEffects.get(key);
      int level = type.getLevel(data);
      int duration = type.getDuration(data);
      
      section.set(key + ".level", level == 0 ? null : level);
      section.set(key + ".duration", duration == 0 ? null : duration);
    }
    
    //Skills
    GenericRPGTypeManager<RPGSkillType> skills = plugin.getRPGDataTypeManager().getSkillManager();
    for (String key : skills.getKeys()) {
      RPGSkillType type = skills.get(key);
      int level = type.getLevel(data);
      int experience = type.getExperience(data);
      
      section.set(key + ".level", level == 0 ? null : level);
      section.set(key + ".experience", experience == 0 ? null : experience);
    }
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
   * Saves an item to a YAML config file in a easily readable form.
   * @param section The target configuration.
   * @param path The path we're saving to.
   * @param item The item being saved.
   */
  public static void saveItem(ConfigurationSection section, ItemStack item) {
    
    //Base Data
    section.set("amount", item.getAmount());
    section.set("material", item.getType().name());
    
    //Enchantments
    for(Enchantment ench : item.getEnchantments().keySet()) {
      section.set("enchantment." + ench.getKey().getKey(), item.getEnchantmentLevel(ench));
    }
    
    //ItemMeta
    ItemMeta meta = item.getItemMeta();
    
    //Attributes
    if (meta.hasAttributeModifiers()) {
      int i = 0;
      for(Attribute attribute : Attribute.values()) {
        if (!meta.hasAttributeModifiers()) {continue;}
        if (meta.getAttributeModifiers(attribute) == null) {continue;}
        
        for(AttributeModifier modifier : meta.getAttributeModifiers(attribute)) {
          if (modifier == null) { continue; }
          String attrPath = "attribute." + i + ".";
          section.set(attrPath + "type", attribute.name());
          section.set(attrPath + "name", modifier.getName());
          section.set(attrPath + "amount", modifier.getAmount());
          section.set(attrPath + "operation", modifier.getOperation().name());
          section.set(attrPath + "slot", modifier.getSlot() != null ? modifier.getSlot().name() : "null");

          i++;
        }
      }
    }
    
    //Custom Model Data
    if (meta.hasCustomModelData()) {
      section.set("custom model data", meta.getCustomModelData());
    }
    
    //Display Name
    if (meta.hasDisplayName()) {
      section.set("name", meta.getDisplayName());
    }
    
    //Lore
    if (meta.hasLore()) {
      section.set("lore", meta.getLore());
    }
    
    //Item Flags
    if (!meta.getItemFlags().isEmpty()) {
      ArrayList<String> itemFlags = new ArrayList<String>();
      for(ItemFlag flag : meta.getItemFlags()) { itemFlags.add(flag.name()); }
      section.set("item flags", itemFlags);
    }
    
    //Unbreakable
    if (meta.isUnbreakable()) {
      section.set("unbreakable", meta.isUnbreakable());
    }
    
    //Special forms of item meta
    if (meta instanceof AxolotlBucketMeta) {
      AxolotlBucketMeta m = (AxolotlBucketMeta) meta;
      if (m.hasVariant()) { section.set("axolotl", m.getVariant().name()); }
    }
    
    if (meta instanceof BannerMeta) {
      BannerMeta m = (BannerMeta) meta;
      for(int i = 0; i < m.getPatterns().size(); i++) {
        Pattern pattern = m.getPatterns().get(i);
        section.set("banner." + i + ".name", pattern.getPattern().name());
        section.set("banner." + i + ".color", pattern.getColor().name());
      }
      
    }
    
    if (meta instanceof BookMeta) {
      BookMeta m = (BookMeta) meta;
      
      if (m.hasAuthor()) { section.set("book.author", m.getAuthor());}
      if (m.hasGeneration()) { section.set("book.generation", m.getGeneration().name());}
      if (m.hasPages()) { section.set("book.pages", m.getPages());}
      if (m.hasTitle()) { section.set("book.title", m.getTitle());}
    }
    
    if (meta instanceof BundleMeta) {
      BundleMeta m = (BundleMeta) meta;
      
      if (m.hasItems()) {
        int i = 0;
        for(ItemStack stack : m.getItems()) {
          saveItem(section.getConfigurationSection("bundle." + i), stack);
          i++;
        }
      }
    }
    
    if (meta instanceof CompassMeta) {
      CompassMeta m = (CompassMeta) meta;
      section.set("book.compass.lodestone.tracked", m.isLodestoneTracked());
      if (m.hasLodestone()) {
        Location loc = m.getLodestone();
        section.set("book.compass.lodestone.x", loc.getBlockX());
        section.set("book.compass.lodestone.y", loc.getBlockY());
        section.set("book.compass.lodestone.z", loc.getBlockZ());
        section.set("book.compass.lodestone.world", loc.getWorld().getName());
      }
    }
    
    if (meta instanceof CrossbowMeta) {
      CrossbowMeta m = (CrossbowMeta) meta;
      if (m.hasChargedProjectiles()) {
        int i = 0;
        for(ItemStack stack : m.getChargedProjectiles()) {
          saveItem(section.getConfigurationSection("crossbow." + i), stack);
          i++;
        }
      }
    }
    
    if (meta instanceof Damageable) {
      Damageable m = (Damageable) meta;
      if (m.hasDamage()) { section.set("damage", m.getDamage()); }
    }
    
    if (meta instanceof EnchantmentStorageMeta) {
      EnchantmentStorageMeta m = (EnchantmentStorageMeta) meta;
      if (m.hasStoredEnchants()) {
        for(Enchantment ench : m.getStoredEnchants().keySet()) {
          section.set("stored enchantments." + ench.getKey().getKey(), m.getStoredEnchants().get(ench));
        }
      }
    }
    
    if (meta instanceof FireworkEffectMeta) {
      FireworkEffectMeta m = (FireworkEffectMeta) meta;
      
      if (m.hasEffect()) {
        section.set("firework effect.type", m.getEffect().getType().name());
        
        List<Integer> colors = new ArrayList<Integer>();
        for(Color color : m.getEffect().getColors()) { colors.add(color.asRGB()); }
        section.set("firework effect.colors", colors);
        
        List<Integer> fadeColors = new ArrayList<Integer>();
        for(Color color : m.getEffect().getColors()) { fadeColors.add(color.asRGB()); }
        section.set("firework effect.fadeColors", fadeColors);
        
        section.set("firework effect.flicker", m.getEffect().hasFlicker());
        section.set("firework effect.trail", m.getEffect().hasTrail());
      }
    }
    
    if (meta instanceof FireworkMeta) {
      FireworkMeta m = (FireworkMeta) meta;
      section.set("firework.power", m.getPower());
      if (m.hasEffects()) {
        int i = 0;
        for(FireworkEffect effect : m.getEffects()) {
          section.set("firework.effects." + i + ".type", effect.getType().name());
          
          List<Integer> colors = new ArrayList<Integer>();
          for(Color color : effect.getColors()) { colors.add(color.asRGB()); }
          section.set("firework.effects." + i + ".colors", colors);
          
          
          List<Integer> fadeColors = new ArrayList<Integer>();
          for(Color color : effect.getColors()) { colors.add(color.asRGB()); }
          section.set("firework.effects." + i + ".fadeColors", fadeColors);
          
          section.set("firework.effects." + i + ".flicker", effect.hasFlicker());
          section.set("firework.effects." + i + ".trail", effect.hasTrail());
        }
      }
    }
    
    if (meta instanceof KnowledgeBookMeta) {
      KnowledgeBookMeta m = (KnowledgeBookMeta) meta;
      if (m.hasRecipes()) {
        List<String> recipies = new ArrayList<String>();
        for(NamespacedKey key : m.getRecipes()) { recipies.add(key.getKey()); }
        section.set("recipies", recipies);
      }
    }
    
    if (meta instanceof LeatherArmorMeta) {
      LeatherArmorMeta m = (LeatherArmorMeta) meta;
      section.set("leather", m.getColor().asRGB());
    }
    
    if (meta instanceof MapMeta) {
      MapMeta m = (MapMeta) meta;
      
      if (m.hasColor()) {section.set("map.color", m.getColor().asRGB());}
      if (m.hasLocationName()) {section.set("map.name", m.getLocationName());}
      if (m.hasMapView()) {
        MapView view = m.getMapView();
        section.set("map id", view.getId());
      }
    }
    
    if (meta instanceof PotionMeta) {
      PotionMeta m = (PotionMeta) meta;
      
      section.set("potion.type", m.getBasePotionData().getType().name());
      section.set("potion.extended", m.getBasePotionData().isExtended());
      section.set("potion.upgraded", m.getBasePotionData().isUpgraded());
      section.set("potion.color", m.getColor());
      
      if (m.hasCustomEffects()) {
        int i = 0;
        for(PotionEffect potion : m.getCustomEffects()) {
          section.set("potion.custom." + i + ".amplifier", potion.getAmplifier());
          section.set("potion.custom." + i + ".duration", potion.getDuration());
          section.set("potion.custom." + i + ".type", potion.getType().getName());
          i++;
        }
      }
    }
    
    if (meta instanceof Repairable) {
      Repairable m = (Repairable) meta;
      if (m.hasRepairCost()) {
        section.set("repair cost", m.getRepairCost());
      }
    }
    
    if (meta instanceof SkullMeta) {
      SkullMeta m = (SkullMeta) meta;
      if (m.hasOwner()) {
        section.set("skull", m.getOwningPlayer().getUniqueId().toString());
      }
    }
    
    if (meta instanceof SuspiciousStewMeta) {
      SuspiciousStewMeta m = (SuspiciousStewMeta) meta;
      if (m.hasCustomEffects()) {
        
        for(PotionEffect effect : m.getCustomEffects()) {
          section.set("stew." + effect.getType().getName() + ".amplifier", effect.getAmplifier());
          section.set("stew." + effect.getType().getName() + ".duration", effect.getDuration());
        }
      }
    }
    
    if (meta instanceof TropicalFishBucketMeta) {
      TropicalFishBucketMeta m = (TropicalFishBucketMeta) meta;
      if (m.hasVariant()) {
        section.set("fish.body", m.getBodyColor().name());
        section.set("fish.pattern.type", m.getPattern().name());
        section.set("fish.pattern.color", m.getPatternColor().name());
      }
    }
    
    //Save RPG Data
    PersistentDataContainer data = meta.getPersistentDataContainer();
    if (!section.isConfigurationSection("data")) { section.createSection("data"); }
    saveRPGData(section.getConfigurationSection("data"), data);
  }

  
  /**
   * Saves the rpg data
   * @param section The configuration section we're saving to.
   * @param data The data we're saving.
   */
  public static void saveRPGData(ConfigurationSection section, PersistentDataContainer data) {
    
    //Attributes
    GenericRPGTypeManager<RPGAttributeType> attributes = plugin.getRPGDataTypeManager().getAttributeManager();
    for (String key : attributes.getKeys()) {
      RPGAttributeType type = attributes.get(key);
      int flat = type.getFlat(data);
      int scaling = type.getScaling(data);
      
      section.set(key + ".flat", flat == 0 ? null : flat);
      section.set(key + ".scaling", scaling == 0 ? null : scaling);
    }
    
    //Enchantments
    GenericRPGTypeManager<RPGEnchantmentType> enchantments = plugin.getRPGDataTypeManager().getEnchantmentManager();
    for (String key : enchantments.getKeys()) {
      RPGEnchantmentType type = enchantments.get(key);
      int level = type.getLevel(data);
      
      section.set(key + ".level", level == 0 ? null : level);
    }
    
    //Generics
    GenericRPGTypeManager<RPGDataType> generics = plugin.getRPGDataTypeManager().getGenericManager();
    for (String key : generics.getKeys()) {
      RPGDataType type = generics.get(key);
      NamespacedKey nsk;
      
      //Save Primary Data
      if (type instanceof RPGDataOneValued) {
        
        RPGDataOneValued alt = (RPGDataOneValued) type;
        nsk = alt.getPrimaryKey();
        int value = type.get(data, nsk, PersistentDataType.INTEGER, 0);
        section.set(key + ".primary", value == 0 ? null : value);
      }
      
      //Save Secondary Data
      if (type instanceof RPGDataTwoValued) {
        
        RPGDataTwoValued alt = (RPGDataTwoValued) type;
        nsk = alt.getSecondaryKey();
        
        int value = type.get(data, nsk, PersistentDataType.INTEGER, 0);
        section.set(key + ".secondary", value == 0 ? null : value);
      }

      //Save Data String
      if (type instanceof RPGDataString) {
        RPGDataString alt = (RPGDataString) type;
        nsk = alt.getStringKey();
        
        String value = type.get(data, nsk, PersistentDataType.STRING, null);
        section.set(key + ".string", value);
      }
    }
    
    //Potion Effects
    GenericRPGTypeManager<RPGPotionEffectType> potionEffects = plugin.getRPGDataTypeManager().getPotionEffectManager();
    for (String key : potionEffects.getKeys()) {
      RPGPotionEffectType type = potionEffects.get(key);
      int level = type.getLevel(data);
      int duration = type.getDuration(data);
      
      section.set(key + ".level", level == 0 ? null : level);
      section.set(key + ".duration", duration == 0 ? null : duration);
    }
    
    //Skills
    GenericRPGTypeManager<RPGSkillType> skills = plugin.getRPGDataTypeManager().getSkillManager();
    for (String key : skills.getKeys()) {
      RPGSkillType type = skills.get(key);
      int level = type.getLevel(data);
      int experience = type.getExperience(data);
      
      section.set(key + ".level", level == 0 ? null : level);
      section.set(key + ".experience", experience == 0 ? null : experience);
    }
  }

  
  /**
   * Inspects the data container and sends it to the player.
   * @param player The target player.
   * @param data The data being inspected.
   */
  public static void inspect(Player player, PersistentDataContainer data) {
    //Send the information
    player.sendMessage(ChatColor.GOLD + "" + ChatColor.UNDERLINE + "        Inspection        ");
    
    //Attributes
    player.sendMessage(ChatColor.GOLD + "  Attributes:");
    for(RPGAttributeType type : plugin.getRPGDataTypeManager().getAttributeManager().getTypes()) {
      
      int flat = type.getFlat(data);
      int scaling = type.getScaling(data);
      if (flat == 0 && scaling == 0) { continue; }
      
      boolean isNegative = (type.getAlignment() == RPGDataAlignment.NEGATIVE);
      String positive = (isNegative ? ChatColor.RED : ChatColor.GREEN) + "+";
      String negative = (isNegative ? ChatColor.GREEN : ChatColor.RED) + "";
      
      player.sendMessage("    " + ChatColor.YELLOW + type.getDisplayName() + ": " + ChatColor.WHITE +
          (flat > 0 ? positive : negative) + flat + ChatColor.WHITE
          + " & " + (scaling > 0 ? positive : negative) + scaling + "%");
    }
    
    //Enchantments
    player.sendMessage(ChatColor.GOLD + "  Enchantments:");
    for(RPGEnchantmentType type : plugin.getRPGDataTypeManager().getEnchantmentManager().getTypes()) {
      
      int level = type.getLevel(data);
      
      if (level == 0) { continue; }
      
      boolean isNegative = (type.getAlignment() == RPGDataAlignment.NEGATIVE);
      String positive = (isNegative ? ChatColor.RED : ChatColor.GREEN) + "+";
      String negative = (isNegative ? ChatColor.GREEN : ChatColor.RED) + "";
      
      player.sendMessage("    " + ChatColor.YELLOW + type.getDisplayName() + ": " + ChatColor.WHITE +
          (level > 0 ? positive : negative) + "Lv." + level + ChatColor.WHITE);
    }
    
    //Potion Effects
    player.sendMessage(ChatColor.GOLD + "  Potion Effects:");
    for(RPGPotionEffectType type : plugin.getRPGDataTypeManager().getPotionEffectManager().getTypes()) {
      
      int level = type.getLevel(data);
      int duration = type.getDuration(data);
      
      if (level == 0 && duration == 0) { continue; }
      
      boolean isNegative = (type.getAlignment() == RPGDataAlignment.NEGATIVE);
      String positive = (isNegative ? ChatColor.RED : ChatColor.GREEN) + "+";
      String negative = (isNegative ? ChatColor.GREEN : ChatColor.RED) + "";
      
      player.sendMessage("    " + ChatColor.YELLOW + type.getDisplayName() + ": " + ChatColor.WHITE +
          (level > 0 ? positive : negative) + "Lv." + level + ChatColor.WHITE
          + " & " + (duration > 0 ? positive : negative) + duration + "s");
    }
    
    //Skills
    player.sendMessage(ChatColor.GOLD + "  Skills:");
    for(RPGSkillType type : plugin.getRPGDataTypeManager().getSkillManager().getTypes()) {
      
      int level = type.getLevel(data);
      int experience = type.getExperience(data);
      
      if (level == 0 && experience == 0) { continue; }
      
      boolean isNegative = (type.getAlignment() == RPGDataAlignment.NEGATIVE);
      String positive = (isNegative ? ChatColor.RED : ChatColor.GREEN) + "+";
      String negative = (isNegative ? ChatColor.GREEN : ChatColor.RED) + "";
      
      player.sendMessage("    " + ChatColor.YELLOW + type.getDisplayName() + ": " + ChatColor.WHITE +
          (level > 0 ? positive : negative) + "Lv." + level + ChatColor.WHITE
          + " & " + (experience > 0 ? positive : negative) + experience + "% Exp");
    }
  }
  
  /**
   * Private Constructor
   */
  private RPGDataUtil() {}
}
