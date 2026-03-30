package com.jedaiwm.listeners;

import com.jedaiwm.JedaiWM;
import com.jedaiwm.commands.GefilteFishCommand;
import com.jedaiwm.managers.JewManager;
import com.jedaiwm.models.JewPlayer;
import com.jedaiwm.utils.ActionBarQueue;
import com.jedaiwm.utils.EffectsUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;

import java.io.File;

public class GefilteFishListener implements Listener {

    private final JedaiWM plugin;
    private final JewManager jewManager;

    public GefilteFishListener(JedaiWM plugin) {
        this.plugin = plugin;
        this.jewManager = plugin.getJewManager();
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        if (!GefilteFishCommand.isGefilteFish(event.getItem())) return;

        event.setCancelled(true);
        Player player = event.getPlayer();

        if (jewManager.isJew(player)) {
            JewPlayer jew = jewManager.getJew(player);
            int pietyGain = plugin.getConfig().getInt("gefilte-fish.piety-gain", 8);
            jew.addPiety(pietyGain);
            jew.updateLevel();
            jew.save(new File(plugin.getDataFolder(), "jews"));

            EffectsUtil.playSoundPietyGain(player);
            EffectsUtil.spawnPietyGainParticles(player);

            ActionBarQueue.typewriter(player, "\uD83D\uDC1F +" + pietyGain + " Piety",
                ActionBarQueue.PRIORITY_INFO, 2, 40);
        } else {
            int nauseaSeconds = plugin.getConfig().getInt("gefilte-fish.nausea-seconds", 5);
            player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.NAUSEA, nauseaSeconds * 20, 1));
            ActionBarQueue.typewriter(player, "\u26A0 You don't understand this food.",
                ActionBarQueue.PRIORITY_INFO, 2, 30);
        }

        if (player.getInventory().containsAtLeast(event.getItem(), 1)) {
            player.getInventory().removeItem(event.getItem());
        }
    }
}
