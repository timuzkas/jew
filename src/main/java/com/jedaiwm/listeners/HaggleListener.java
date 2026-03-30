package com.jedaiwm.listeners;

import com.jedaiwm.JedaiWM;
import com.jedaiwm.managers.JewManager;
import com.jedaiwm.models.JewPlayer;
import com.jedaiwm.utils.ActionBarUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantInventory;
import org.bukkit.inventory.MerchantRecipe;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class HaggleListener implements Listener {

    private final JedaiWM plugin;
    private final JewManager jewManager;
    private final Random random = new Random();

    private final Map<UUID, Integer> haggledTrades = new HashMap<>();

    public HaggleListener(JedaiWM plugin) {
        this.plugin = plugin;
        this.jewManager = plugin.getJewManager();
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMerchantResultClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!(event.getInventory() instanceof MerchantInventory merchantInv)) return;

        if (event.getRawSlot() != 2) return;

        if (merchantInv.getItem(2) == null) return;

        if (!jewManager.isJew(player)) return;

        JewPlayer jew = jewManager.getJew(player);
        if (jew == null) return;

        MerchantRecipe recipe = merchantInv.getSelectedRecipe();
        if (recipe == null) return;

        int recipeKey = System.identityHashCode(recipe);
        UUID uuid = player.getUniqueId();

        if (haggledTrades.getOrDefault(uuid, -1) == recipeKey) return;
        haggledTrades.put(uuid, recipeKey);

        double chance = getHaggleChance(jew.getLevel());
        if (random.nextDouble() >= chance) return;

        int maxDiscount = jew.getLevel() >= 5 ? 2 : 1;
        boolean discounted = applyDiscount(merchantInv, recipe, maxDiscount);

        if (discounted) {
            ActionBarUtil.sendActionBar(player, "\uD83D\uDCB0 You negotiated well.");
            triggerTradePhrase(player);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        if (!(event.getInventory() instanceof MerchantInventory)) return;
        haggledTrades.remove(player.getUniqueId());
    }

    private boolean applyDiscount(MerchantInventory merchantInv, MerchantRecipe recipe, int maxDiscount) {
        List<ItemStack> ingredients = recipe.getIngredients();
        if (ingredients.isEmpty()) return false;

        int emeraldSlot = -1;
        for (int i = 0; i < ingredients.size(); i++) {
            ItemStack ing = ingredients.get(i);
            if (ing != null && ing.getType() == Material.EMERALD) {
                emeraldSlot = i;
                break;
            }
        }

        if (emeraldSlot == -1) return false;

        ItemStack emeraldStack = ingredients.get(emeraldSlot);
        int currentCost = emeraldStack.getAmount();
        if (currentCost <= 1) return false;

        int discount = Math.min(maxDiscount, currentCost - 1);
        int newCost = currentCost - discount;

        ItemStack discountedEmerald = emeraldStack.clone();
        discountedEmerald.setAmount(newCost);

        List<ItemStack> newIngredients = new java.util.ArrayList<>(ingredients);
        newIngredients.set(emeraldSlot, discountedEmerald);
        recipe.setIngredients(newIngredients);

        merchantInv.setItem(emeraldSlot, discountedEmerald);

        return true;
    }

    private double getHaggleChance(int level) {
        if (level >= 5) return plugin.getConfig().getDouble("haggle.chance-level5", 0.25);
        return plugin.getConfig().getDouble("haggle.chance-base", 0.10);
    }

    private void triggerTradePhrase(Player player) {
        List<String> phrases = plugin.getConfig().getStringList("phrases.trade");
        if (phrases.isEmpty()) return;
        String phrase = phrases.get(random.nextInt(phrases.size()));
        player.sendMessage(org.bukkit.ChatColor.GOLD + "* " + phrase + " *");
    }
}
