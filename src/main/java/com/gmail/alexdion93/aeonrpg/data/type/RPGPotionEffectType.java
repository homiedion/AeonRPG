package com.gmail.alexdion93.aeonrpg.data.type;

import java.util.HashSet;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import com.gmail.alexdion93.aeonrpg.data.enums.RPGDataAlignment;
import com.gmail.alexdion93.aeonrpg.data.interfaces.RPGDataLevelIndependent;
import com.gmail.alexdion93.aeonrpg.data.interfaces.RPGDataTwoValued;
import com.gmail.alexdion93.aeonrpg.util.RPGPotionUtil;
import com.gmail.alexdion93.aeonrpg.util.RomanNumeral;

public abstract class RPGPotionEffectType extends RPGDataType implements RPGDataLevelIndependent, RPGDataTwoValued {

  private final NamespacedKey altKey;
  private HashSet<UUID> affectedMobs;

  /**
   * Constructor
   *
   * @param plugin      The targe tplugin.
   * @param key         The key of this type.
   * @param displayName The display name of this type.
   * @param description This type's description
   */
  public RPGPotionEffectType(final JavaPlugin plugin, final String key, final String displayName,
      final String description) {
    super(plugin, "potioneffect." + key, displayName, description);
    altKey = new NamespacedKey(plugin, "potioneffect." + key + ".alt");
    affectedMobs = new HashSet<UUID>();
  }

  /**
   * Returns the set of mobs affected by this effect type.
   *
   * @return the set of mobs affected by this effect type.
   */
  public HashSet<UUID> getAffectedMobs() {
    return affectedMobs;
  }

  @Override
  public RPGDataAlignment getAlignment() {
    return RPGDataAlignment.POSITIVE;
  }

  @Override
  public NamespacedKey getAltNamespacedKey() {
    return altKey;
  }

  /**
   * Returns the default alt value of this type.
   *
   * @return The default alt value of this type.
   */
  @Override
  public int getDefaultAltValue() {
    return 0;
  }

  /**
   * Returns the default value of this type.
   *
   * @return The default value of this type.
   */
  @Override
  public int getDefaultValue() {
    return 0;
  }

  /**
   * Returns if this is an instant potion effect.
   *
   * @return True if this is an instant potion effect.
   */
  public abstract boolean isInstant();

  @Override
  public boolean isLevelIndependent() {
    return false;
  }

  @Override
  public boolean isNegativeAllowed() {
    return false;
  }

  @Override
  public void modifyEntity(Entity entity, int level, int duration) {
    if (!(entity instanceof LivingEntity)) {
      return;
    }
    RPGPotionUtil.applyPotionEffect((LivingEntity) entity, this, duration, level);
  }

  @Override
  public void modifyEntityDefault(Entity entity, int level, int duration) {
  }

  /**
   * Modifies the item this is placed on
   */
  @Override
  public void modifyItem(ItemMeta meta, Material material, int value) {
    meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
  }

  /**
   * Triggers when the potion effect is applied.
   *
   * @param entity   The entity being targetted.
   * @param level    The level of the effect.
   * @param duration The remaining duration of the effect.
   */
  public abstract void onPotionApply(LivingEntity entity, int level, int duration);

  /**
   * Triggers when the potion effect ticks.
   *
   * @param entity   The entity being targetted.
   * @param level    The level of the effect.
   * @param duration The remaining duration of the effect.
   */
  public abstract void onPotionTick(LivingEntity entity, int level, int duration);

  @Override
  public String toLoreString(final int value) {
    return toLoreString(0, value);
  }

  /**
   * Displays the string used as item lore.
   *
   * @param value    The potion effect amplifier.
   * @param altValue The potion effect duration in seconds.
   * @return A formatted string.
   */
  @Override
  public String toLoreString(final int value, final int altValue) {

    // Variables
    final int minutes = (altValue / 60);
    final int seconds = (altValue % 60);
    final ChatColor color = (getAlignment() == RPGDataAlignment.NEGATIVE ? ChatColor.RED : ChatColor.GRAY);
    final String romanNumeral = (isLevelIndependent() && (value > 0) ? "" : " " + RomanNumeral.parse(value));
    final String display = color + getDisplayName() + romanNumeral + " (" + minutes + ":" + (seconds < 10 ? "0" : "")
        + seconds + ")";

    // Return
    return display;
  }
}
