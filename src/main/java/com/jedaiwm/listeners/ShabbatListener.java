package com.jedaiwm.listeners;

import com.jedaiwm.JedaiWM;
import com.jedaiwm.managers.JewManager;
import com.jedaiwm.models.JewPlayer;
import com.jedaiwm.utils.ActionBarUtil;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ShabbatListener implements Listener {

    private final JedaiWM plugin;
    private final JewManager jewManager;
    private final Set<UUID> movingDuringShabbat = new HashSet<>();

    public ShabbatListener(JedaiWM plugin) {
        this.plugin = plugin;
        this.jewManager = plugin.getJewManager();
        
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            plugin.getShabbatManager().updateShabbatStatus();
            boolean isShabbat = plugin.getShabbatManager().isShabbat();
            
            for (org.bukkit.entity.Player player : plugin.getServer().getOnlinePlayers()) {
                if (jewManager.isJew(player) && isShabbat) {
                    String msg = "\uD83D\uDD4F Shabbat Shalom \u2014 Rest is commanded.";
                    ActionBarUtil.sendActionBar(player, msg);
                }
            }
        }, 0L, 100L);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!plugin.getShabbatManager().isShabbat()) {
            return;
        }

        if (!jewManager.isJew(event.getPlayer())) {
            return;
        }

        event.setCancelled(true);
        ActionBarUtil.sendActionBar(event.getPlayer(), "\u26A0 Shabbat! You cannot work.");
        event.getPlayer().sendMessage(com.jedaiwm.utils.TextUtil.errorMessage("You cannot break blocks on Shabbat!"));
        
        int pietyLoss = plugin.getConfig().getInt("piety.shabbat-violation-loss", 5);
        JewPlayer jew = jewManager.getJew(event.getPlayer());
        jew.removePiety(pietyLoss);
        jew.updateLevel();
        jew.save(new java.io.File(plugin.getDataFolder(), "jews"));
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!plugin.getShabbatManager().isShabbat()) {
            return;
        }

        if (!jewManager.isJew(event.getPlayer())) {
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }

        Material blockType = event.getClickedBlock().getType();
        
        if (blockType == Material.FURNACE || 
            blockType == Material.BLAST_FURNACE || 
            blockType == Material.SMOKER ||
            blockType == Material.CRAFTING_TABLE ||
            blockType == Material.ANVIL ||
            blockType == Material.GRINDSTONE ||
            blockType == Material.STONECUTTER ||
            blockType == Material.LOOM ||
            blockType == Material.CARTOGRAPHY_TABLE ||
            blockType == Material.BOOKSHELF) {
            
            event.setCancelled(true);
            ActionBarUtil.sendActionBar(event.getPlayer(), "\u26A0 Shabbat! You cannot work with this.");
            event.getPlayer().sendMessage(com.jedaiwm.utils.TextUtil.errorMessage("You cannot use this on Shabbat!"));
            
            int pietyLoss = plugin.getConfig().getInt("piety.shabbat-violation-loss", 5);
            JewPlayer jew = jewManager.getJew(event.getPlayer());
            jew.removePiety(pietyLoss);
            jew.updateLevel();
            jew.save(new java.io.File(plugin.getDataFolder(), "jews"));
        }
    }
}
