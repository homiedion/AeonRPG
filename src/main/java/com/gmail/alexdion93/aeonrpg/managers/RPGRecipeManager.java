package com.gmail.alexdion93.aeonrpg.managers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.inventory.BlastingRecipe;
import org.bukkit.inventory.CampfireRecipe;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.SmithingRecipe;
import org.bukkit.inventory.SmokingRecipe;
import org.bukkit.inventory.StonecuttingRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import com.gmail.alexdion93.aeonrpg.data.interfaces.RPGDataString;
import com.gmail.alexdion93.aeonrpg.data.interfaces.RPGDataTwoValued;
import com.gmail.alexdion93.aeonrpg.data.interfaces.RPGDataValued;
import com.gmail.alexdion93.aeonrpg.data.type.RPGDataType;
import com.gmail.alexdion93.aeonrpg.util.RPGDataUtil;

/**
 * A manager used to modify crafting recipies when the server starts up
 * @author Alex Dion
 *
 */
public class RPGRecipeManager implements Listener {

  private ArrayList<Recipe> recipes;

  /**
   * Constructor
   */
  public RPGRecipeManager() {
    // Generate list of recipies
    recipes = new ArrayList<Recipe>();
    Iterator<Recipe> iterator = Bukkit.recipeIterator();
    while (iterator.hasNext()) {
      recipes.add(iterator.next());
    }
  }

  /**
   * Triggers when the server startup or reload has completed
   *
   * @param event The event being fired
   */
  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void onServerLoad(ServerLoadEvent event) {
    Bukkit.clearRecipes();
    for (Recipe recipe : recipes) {
      Bukkit.addRecipe(recipe);
    }
    recipes.clear();
    recipes = null;
  }

  /**
   * Searches for any recipes with the result being the provided target and adds
   * the rpg data to it.
   *
   * @param target The target we're looking for.
   * @param type   The RPGDataType we're adding
   * @param values The values being assigned. Single valued data types use one,
   *               Two valued data types use two.
   */
  public void setRPGDataValued(Material target, RPGDataType type, Object... values) {

    // Zero length value check
    if (values.length == 0) {
      return;
    }

    int i = 0;

    // Loop through recipes
    for (Recipe recipe : recipes) {
      ItemStack item = recipe.getResult();
      boolean isMatch = item.getType() == target;

      // Update the recipie if its result is correct
      if (isMatch) {

        // Fetch the item's meta
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
          continue;
        }

        // Add the data to the item
        PersistentDataContainer data = meta.getPersistentDataContainer();

        if (type instanceof RPGDataTwoValued) {
          int value = 0;
          int altValue = 0;

          // Attempt to cast the value to integer
          try {
            value = (int) values[0];
          } catch (Exception e) {
            e.printStackTrace();
          }

          // Attempt to cast the value to integer
          if (values.length >= 2) {
            try {
              altValue = (int) values[1];
            } catch (Exception e) {
              e.printStackTrace();
            }
          }

          data.set(type.getNamespacedKey(), PersistentDataType.INTEGER, value);
          data.set(type.getNamespacedKey(), PersistentDataType.INTEGER, altValue);

          ((RPGDataTwoValued) type).modifyItem(meta, item.getType(), value, altValue);
        } else if (type instanceof RPGDataValued) {
          int value = 0;

          // Attempt to cast the value to integer
          try {
            value = (int) values[0];
          } catch (Exception e) {
            e.printStackTrace();
          }

          // Add the rpg data
          data.set(type.getNamespacedKey(), PersistentDataType.INTEGER, value);
          ((RPGDataValued) type).modifyItem(meta, item.getType(), value);
        } else if (type instanceof RPGDataString) {
          String value = "";

          // Attempt to parse the value
          try {
            value = (String) values[0];
          } catch (Exception e) {
            e.printStackTrace();
          }

          // Add the rpg data
          data.set(type.getNamespacedKey(), PersistentDataType.STRING, value);
          ((RPGDataString) type).modifyItem(meta, item.getType(), value);
        }

        // Update the item's lore
        meta.setLore(RPGDataUtil.generateLore(data));

        // Update the item's meta
        item.setItemMeta(meta);

        // Generate the new recipe
        if (recipe instanceof BlastingRecipe) {
          BlastingRecipe r = (BlastingRecipe) recipe;

          BlastingRecipe temp = new BlastingRecipe(r.getKey(), item, r.getInputChoice(), r.getExperience(),
              r.getCookingTime());
          temp.setGroup(r.getGroup());

          recipe = temp;
        } else if (recipe instanceof CampfireRecipe) {
          CampfireRecipe r = (CampfireRecipe) recipe;

          CampfireRecipe temp = new CampfireRecipe(r.getKey(), item, r.getInputChoice(), r.getExperience(),
              r.getCookingTime());
          temp.setGroup(r.getGroup());

          recipe = temp;

        } else if (recipe instanceof FurnaceRecipe) {
          FurnaceRecipe r = (FurnaceRecipe) recipe;

          FurnaceRecipe temp = new FurnaceRecipe(r.getKey(), item, r.getInput().getType(), r.getExperience(),
              r.getCookingTime());
          temp.setGroup(r.getGroup());

          recipe = temp;
        } else if (recipe instanceof MerchantRecipe) {
          MerchantRecipe r = (MerchantRecipe) recipe;
          recipe = new MerchantRecipe(item, r.getUses(), r.getMaxUses(), r.hasExperienceReward(),
              r.getVillagerExperience(), r.getPriceMultiplier());

        } else if (recipe instanceof ShapedRecipe) {
          ShapedRecipe r = (ShapedRecipe) recipe;

          ShapedRecipe temp = new ShapedRecipe(r.getKey(), item);
          temp.shape(r.getShape());
          temp.setGroup(r.getGroup());

          Map<Character, RecipeChoice> map = r.getChoiceMap();
          for (Character key : map.keySet()) {

            RecipeChoice choice = map.get(key);
            if (choice == null) {
              continue;
            }
            temp.setIngredient(key, choice);
          }

          recipe = temp;
        } else if (recipe instanceof ShapelessRecipe) {
          ShapelessRecipe r = (ShapelessRecipe) recipe;

          ShapelessRecipe temp = new ShapelessRecipe(r.getKey(), item);
          temp.setGroup(r.getGroup());

          for (RecipeChoice t : r.getChoiceList()) {
            temp.addIngredient(t);
          }

          recipe = temp;

        } else if (recipe instanceof SmithingRecipe) {
          SmithingRecipe r = (SmithingRecipe) recipe;
          SmithingRecipe temp = new SmithingRecipe(r.getKey(), item, r.getBase(), r.getAddition());

          recipe = temp;
        } else if (recipe instanceof SmokingRecipe) {
          SmokingRecipe r = (SmokingRecipe) recipe;
          SmokingRecipe temp = new SmokingRecipe(r.getKey(), item, r.getInputChoice(), r.getExperience(),
              r.getCookingTime());

          temp.setGroup(r.getGroup());
          recipe = temp;
        } else if (recipe instanceof StonecuttingRecipe) {
          StonecuttingRecipe r = (StonecuttingRecipe) recipe;
          StonecuttingRecipe temp = new StonecuttingRecipe(r.getKey(), item, r.getInputChoice());

          temp.setGroup(r.getGroup());

          recipe = temp;
        }

        // Update the entry in the list
        recipes.set(i, recipe);
      }

      // Update the incrementor
      i++;
    }
  }

}
