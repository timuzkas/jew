package com.jedaiwm.listeners;

import com.jedaiwm.JedaiWM;
import com.jedaiwm.commands.GefilteFishCommand;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

public class RecipeListener implements Listener {

    private final JedaiWM plugin;

    public RecipeListener(JedaiWM plugin) {
        this.plugin = plugin;
        registerRecipes();
    }

    private void registerRecipes() {
        NamespacedKey key = new NamespacedKey(plugin, "gefilte_fish");
        ShapedRecipe recipe = new ShapedRecipe(key, GefilteFishCommand.createGefilteFish());
        recipe.shape("pep", "cfc", "pep");
        recipe.setIngredient('p', Material.PAPER);
        recipe.setIngredient('e', Material.EGG);
        recipe.setIngredient('c', Material.COOKED_COD);
        recipe.setIngredient('f', Material.CARROT);
        plugin.getServer().addRecipe(recipe);
    }

    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        CraftingInventory inv = event.getInventory();
        ItemStack result = inv.getResult();
        if (result == null) return;
        if (!GefilteFishCommand.isGefilteFish(result)) return;
        if (!plugin.getConfig().getBoolean("gefilte-fish.enabled", true)) {
            inv.setResult(null);
        }
    }
}
