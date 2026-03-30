package com.jedaiwm.commands;

import com.jedaiwm.JedaiWM;
import com.jedaiwm.managers.JewManager;
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

import java.util.List;

public class GefilteFishCommand implements CommandExecutor {

    private final JedaiWM plugin;
    private final JewManager jewManager;

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

        if (!player.hasPermission("jedaiwm.admin")) {
            ActionBarUtil.sendActionBar(player, "\u26A0 No permission.");
            return true;
        }

        if (!plugin.getConfig().getBoolean("gefilte-fish.enabled", true)) {
            ActionBarUtil.sendActionBar(player, "\u26A0 Gefilte Fish is not enabled.");
            return true;
        }

        ItemStack gefilteFish = createGefilteFish();
        player.getInventory().addItem(gefilteFish);
        ActionBarUtil.sendActionBar(player, "\uD83D\uDC1F Gefilte Fish given.");

        return true;
    }

    public static ItemStack createGefilteFish() {
        ItemStack fish = new ItemStack(Material.COOKED_COD, 1);
        ItemMeta meta = fish.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Gefilte Fish");
        meta.setLore(List.of(ChatColor.GRAY + "A traditional Jewish dish", ChatColor.YELLOW + "+8 Piety when eaten"));
        fish.setItemMeta(meta);
        return fish;
    }

    public static boolean isGefilteFish(ItemStack item) {
        if (item == null || item.getType() != Material.COOKED_COD) return false;
        if (!item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        return meta.hasDisplayName() && meta.getDisplayName().contains("Gefilte Fish");
    }
}
