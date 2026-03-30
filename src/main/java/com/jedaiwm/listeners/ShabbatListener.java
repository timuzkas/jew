package com.jedaiwm.listeners;

import com.jedaiwm.JedaiWM;
import com.jedaiwm.managers.JewManager;
import com.jedaiwm.models.JewPlayer;
import com.jedaiwm.utils.ActionBarQueue;
import com.jedaiwm.utils.EffectsUtil;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ShabbatListener implements Listener {

    private final JedaiWM plugin;
    private final JewManager jewManager;
    private final Set<UUID> warnedPlayers = new HashSet<>();

    public ShabbatListener(JedaiWM plugin) {
        this.plugin = plugin;
        this.jewManager = plugin.getJewManager();

        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            plugin.getShabbatManager().updateShabbatStatus();
            if (!plugin.getShabbatManager().isShabbat()) {
                warnedPlayers.clear();
                return;
            }
            for (org.bukkit.entity.Player player : plugin.getServer().getOnlinePlayers()) {
                if (!jewManager.isJew(player)) continue;
                if (ActionBarQueue.currentPriority(player) <= ActionBarQueue.PRIORITY_AMBIENT) {
                    ActionBarQueue.send(player,
                        "\uD83D\uDD4F Shabbat Shalom \u2014 Rest is commanded.",
                        ActionBarQueue.PRIORITY_AMBIENT, 80);
                }
            }
        }, 0L, 300L);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!plugin.getShabbatManager().isShabbat()) return;
        if (!jewManager.isJew(event.getPlayer())) return;

        event.setCancelled(true);
        applyViolation(event.getPlayer());
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!plugin.getShabbatManager().isShabbat()) return;
        if (!jewManager.isJew(event.getPlayer())) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;

        Material blockType = event.getClickedBlock().getType();
        boolean isWorkBlock = blockType == Material.FURNACE || blockType == Material.BLAST_FURNACE
            || blockType == Material.SMOKER || blockType == Material.CRAFTING_TABLE || blockType == Material.ANVIL
            || blockType == Material.GRINDSTONE || blockType == Material.STONECUTTER || blockType == Material.LOOM
            || blockType == Material.CARTOGRAPHY_TABLE || blockType == Material.BOOKSHELF;

        if (isWorkBlock) {
            event.setCancelled(true);
            applyViolation(event.getPlayer());
        }
    }

    private void applyViolation(org.bukkit.entity.Player player) {
        int pietyLoss = plugin.getConfig().getInt("piety.shabbat-violation-loss", 5);
        JewPlayer jew = jewManager.getJew(player);
        jew.removePiety(pietyLoss);
        jew.updateLevel();
        jew.save(new File(plugin.getDataFolder(), "jews"));

        EffectsUtil.playSoundSin(player);
        EffectsUtil.spawnPietyLossParticles(player);

        ActionBarQueue.typewriter(player,
            "\u26A0 Shabbat. You cannot work.",
            ActionBarQueue.PRIORITY_SIN, 2, 50);

        plugin.getLowPietyManager().onPietyLoss(player, jew.getPiety());
    }
}
