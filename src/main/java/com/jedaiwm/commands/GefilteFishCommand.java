package com.jedaiwm.commands;

import com.jedaiwm.JedaiWM;
import com.jedaiwm.managers.JewManager;
import com.jedaiwm.models.JewPlayer;
import com.jedaiwm.utils.ActionBarUtil;
import com.jedaiwm.utils.TextUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.util.List;
import java.util.Random;

public class GefilteFishCommand implements CommandExecutor {

    private final JedaiWM plugin;
    private final JewManager jewManager;
    private final Random random = new Random();

    public GefilteFishCommand(JedaiWM plugin) {
        this.plugin = plugin;
        this.jewManager = plugin.getJewManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be run by a player.");
            return true;
        }

        if (!plugin.getConfig().getBoolean("gefilte-fish.enabled", true)) {
            player.sendMessage(TextUtil.errorMessage("Gefilte Fish is not enabled."));
            return true;
        }

        ItemStack gefilteFish = createGefilteFish();
        
        if (player.getInventory().containsAtLeast(gefilteFish, 1)) {
            player.getInventory().removeItem(gefilteFish);
            consumeGefilteFish(player);
        } else {
            player.getInventory().addItem(gefilteFish);
            player.sendMessage(TextUtil.successMessage("You received Gefilte Fish!"));
        }

        return true;
    }

    private ItemStack createGefilteFish() {
        ItemStack fish = new ItemStack(Material.COD, 1);
        ItemMeta meta = fish.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Gefilte Fish");
        meta.setLore(List.of(ChatColor.GRAY + "A traditional Jewish dish", ChatColor.YELLOW + "+8 Piety when eaten"));
        fish.setItemMeta(meta);
        return fish;
    }

    private void consumeGefilteFish(Player player) {
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
    }
}
