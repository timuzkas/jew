package com.jedaiwm.listeners;

import com.jedaiwm.JedaiWM;
import com.jedaiwm.commands.GefilteFishCommand;
import com.jedaiwm.managers.JewManager;
import com.jedaiwm.models.JewPlayer;
import com.jedaiwm.utils.ActionBarUtil;
import com.jedaiwm.utils.TextUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.util.Random;

public class GefilteFishListener implements Listener {

    private final JedaiWM plugin;
    private final JewManager jewManager;
    private final Random random = new Random();

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
            
            player.sendMessage(TextUtil.successMessage("You eat the Gefilte Fish. +" + pietyGain + " Piety"));
            
            String[] phrases = {"Just like bubbie made.", "A true delicacy.", "Mmm, tradition.", "Baruch atah Adonai."};
            player.sendMessage(ChatColor.GOLD + "* " + phrases[random.nextInt(phrases.length)] + " *");
            
            ActionBarUtil.sendActionBar(player, "\u2721 Piety: " + jew.getPiety());
        } else {
            int nauseaSeconds = plugin.getConfig().getInt("gefilte-fish.nausea-seconds", 5);
            player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, nauseaSeconds * 20, 1));
            ActionBarUtil.sendActionBar(player, "You don't understand this food.");
            player.sendMessage(TextUtil.errorMessage("You don't understand this traditional food..."));
        }
        
        if (player.getInventory().containsAtLeast(event.getItem(), 1)) {
            player.getInventory().removeItem(event.getItem());
        }
    }
}
