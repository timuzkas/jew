package com.jedaiwm.listeners;

import com.jedaiwm.JedaiWM;
import com.jedaiwm.managers.JewManager;
import com.jedaiwm.models.JewPlayer;
import com.jedaiwm.utils.ActionBarUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.MerchantInventory;

import java.util.Random;

public class HaggleListener implements Listener {

    private final JedaiWM plugin;
    private final JewManager jewManager;
    private final Random random = new Random();

    public HaggleListener(JedaiWM plugin) {
        this.plugin = plugin;
        this.jewManager = plugin.getJewManager();
    }

    @EventHandler
    public void onVillagerTrade(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!jewManager.isJew(player)) return;
        if (!(event.getInventory() instanceof MerchantInventory)) return;
        
        JewPlayer jew = jewManager.getJew(player);
        if (jew == null) return;
        
        if (random.nextDouble() < getHaggleChance(jew.getLevel())) {
            ActionBarUtil.sendActionBar(player, "\uD83D\uDCB0 You negotiated well.");
        }
    }

    private double getHaggleChance(int level) {
        if (level >= 5) return 0.25;
        return 0.10;
    }
}
