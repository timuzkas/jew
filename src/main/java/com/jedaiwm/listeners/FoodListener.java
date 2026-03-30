package com.jedaiwm.listeners;

import com.jedaiwm.JedaiWM;
import com.jedaiwm.managers.JewManager;
import com.jedaiwm.models.JewPlayer;
import com.jedaiwm.utils.ActionBarUtil;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class FoodListener implements Listener {

    private final JedaiWM plugin;
    private final JewManager jewManager;

    public FoodListener(JedaiWM plugin) {
        this.plugin = plugin;
        this.jewManager = plugin.getJewManager();
    }

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        if (!(event.getPlayer() instanceof org.bukkit.entity.Player player)) {
            return;
        }

        if (!jewManager.isJew(player)) {
            return;
        }

        JewPlayer jew = jewManager.getJew(player);
        ItemStack item = event.getItem();
        Material type = item.getType();

        List<String> forbiddenFoods = plugin.getConfig().getStringList("restrictions.forbidden-foods");
        boolean isTreif = forbiddenFoods.contains(type.toString());

        if (isTreif) {
            event.setCancelled(true);
            int pietyLoss = plugin.getConfig().getInt("piety.treif-loss", 10);
            jew.removePiety(pietyLoss);
            jew.updateLevel();
            jew.save(new java.io.File(plugin.getDataFolder(), "jews"));
            
            ActionBarUtil.sendActionBar(player, "\u26A0 Treif! You have sinned.");
            player.sendMessage(com.jedaiwm.utils.TextUtil.errorMessage("You cannot eat that! It's forbidden (treif). -" + pietyLoss + " piety"));
            return;
        }

        if (plugin.getConfig().getBoolean("restrictions.milk-meat-check", true)) {
            if (isDairy(type)) {
                if (hasMeatInInventory(player)) {
                    event.setCancelled(true);
                    ActionBarUtil.sendActionBar(player, "\u26A0 Cannot mix meat and dairy!");
                    player.sendMessage(com.jedaiwm.utils.TextUtil.errorMessage("You cannot eat dairy while holding meat in your inventory!"));
                    return;
                }
            } else if (isMeat(type)) {
                if (hasDairyInInventory(player)) {
                    event.setCancelled(true);
                    ActionBarUtil.sendActionBar(player, "\u26A0 Cannot mix meat and dairy!");
                    player.sendMessage(com.jedaiwm.utils.TextUtil.errorMessage("You cannot eat meat while holding dairy in your inventory!"));
                    return;
                }
            }
        }
    }

    private boolean isDairy(Material type) {
        return type.toString().contains("MILK") || 
               type.toString().contains("CHEESE") ||
               type == Material.CAKE ||
               type == Material.BREAD;
    }

    private boolean isMeat(Material type) {
        return type.toString().contains("PORK") ||
               type.toString().contains("BEEF") ||
               type.toString().contains("CHICKEN") ||
               type.toString().contains("MUTTON") ||
               type.toString().contains("RABBIT") ||
               type.toString().contains("STEW");
    }

    private boolean hasMeatInInventory(org.bukkit.entity.Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && isMeat(item.getType())) {
                return true;
            }
        }
        return false;
    }

    private boolean hasDairyInInventory(org.bukkit.entity.Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && isDairy(item.getType())) {
                return true;
            }
        }
        return false;
    }
}
