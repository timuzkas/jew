package com.jedaiwm.commands;

import com.jedaiwm.JedaiWM;
import com.jedaiwm.managers.JewManager;
import com.jedaiwm.models.JewPlayer;
import com.jedaiwm.utils.ActionBarUtil;
import com.jedaiwm.utils.TextUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.UUID;

public class JewCommand implements CommandExecutor {

    private final JedaiWM plugin;
    private final JewManager jewManager;

    public JewCommand(JedaiWM plugin) {
        this.plugin = plugin;
        this.jewManager = plugin.getJewManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be run by a player.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            showStatus(player, player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "make" -> {
                if (!player.hasPermission("jedaiwm.admin")) {
                    player.sendMessage(TextUtil.errorMessage("You don't have permission to use this command."));
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(TextUtil.errorMessage("Usage: /jew make <player>"));
                    return true;
                }
                String targetName = args[1];
                OfflinePlayer target = Bukkit.getOfflinePlayerIfCached(targetName);
                if (target == null) {
                    player.sendMessage(TextUtil.errorMessage("Player not found."));
                    return true;
                }
                if (jewManager.isJew(target.getUniqueId())) {
                    player.sendMessage(TextUtil.errorMessage(target.getName() + " is already a Jew."));
                    return true;
                }
                JewPlayer jew = jewManager.makeJew(target, "admin");
                player.sendMessage(TextUtil.successMessage(target.getName() + " is now a Jew!"));
                if (target.isOnline()) {
                    target.getPlayer().sendMessage(TextUtil.infoMessage("You have been made a Jew! Use /jew to see your status."));
                }
                return true;
            }

            case "remove" -> {
                if (!player.hasPermission("jedaiwm.admin")) {
                    player.sendMessage(TextUtil.errorMessage("You don't have permission to use this command."));
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(TextUtil.errorMessage("Usage: /jew remove <player>"));
                    return true;
                }
                String targetName = args[1];
                OfflinePlayer target = Bukkit.getOfflinePlayerIfCached(targetName);
                if (target == null) {
                    player.sendMessage(TextUtil.errorMessage("Player not found."));
                    return true;
                }
                if (!jewManager.isJew(target.getUniqueId())) {
                    player.sendMessage(TextUtil.errorMessage(target.getName() + " is not a Jew."));
                    return true;
                }
                jewManager.removeJew(target.getUniqueId());
                player.sendMessage(TextUtil.successMessage(target.getName() + " has been removed from the Jews."));
                return true;
            }

            case "status" -> {
                if (args.length >= 2) {
                    if (!player.hasPermission("jedaiwm.admin")) {
                        player.sendMessage(TextUtil.errorMessage("You don't have permission to view other players' status."));
                        return true;
                    }
                    String targetName = args[1];
                    OfflinePlayer target = Bukkit.getOfflinePlayerIfCached(targetName);
                    if (target == null) {
                        player.sendMessage(TextUtil.errorMessage("Player not found."));
                        return true;
                    }
                    if (!jewManager.isJew(target.getUniqueId())) {
                        player.sendMessage(TextUtil.errorMessage(target.getName() + " is not a Jew."));
                        return true;
                    }
                    showStatus(player, target.getPlayer());
                } else {
                    showStatus(player, player);
                }
                return true;
            }

            case "list" -> {
                if (!player.hasPermission("jedaiwm.admin")) {
                    player.sendMessage(TextUtil.errorMessage("You don't have permission to use this command."));
                    return true;
                }
                Collection<JewPlayer> allJews = jewManager.getAllJews();
                if (allJews.isEmpty()) {
                    player.sendMessage(TextUtil.infoMessage("No Jews found."));
                    return true;
                }
                player.sendMessage(Component.text("=== Jews (" + allJews.size() + ") ===", NamedTextColor.GOLD));
                for (JewPlayer jew : allJews) {
                    player.sendMessage(Component.text(jew.getName() + " - Level " + jew.getLevel() + " (" + jew.getLevelName() + ") | Piety: " + jew.getPiety(), NamedTextColor.AQUA));
                }
                return true;
            }

            case "convert" -> {
                if (!jewManager.isJew(player)) {
                    player.sendMessage(TextUtil.errorMessage("You are not a Jew."));
                    return true;
                }
                JewPlayer jew = jewManager.getJew(player);
                if (jew.getLevel() < 3) {
                    player.sendMessage(TextUtil.errorMessage("You must be at least Level 3 (Ben Torah) to convert others."));
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(TextUtil.errorMessage("Usage: /jew convert <player>"));
                    return true;
                }
                String targetName = args[1];
                Player target = Bukkit.getPlayer(targetName);
                if (target == null) {
                    player.sendMessage(TextUtil.errorMessage("Player not found or not online."));
                    return true;
                }
                if (jewManager.isJew(target.getUniqueId())) {
                    player.sendMessage(TextUtil.errorMessage(target.getName() + " is already a Jew."));
                    return true;
                }
                TextUtil.sendConversionInvite(target, player);
                player.sendMessage(TextUtil.successMessage("Invitation sent to " + target.getName()));
                return true;
            }

            case "accept" -> {
                if (jewManager.isJew(player)) {
                    player.sendMessage(TextUtil.errorMessage("You are already a Jew."));
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(TextUtil.errorMessage("Usage: /jew accept <player>"));
                    return true;
                }
                Player inviter = Bukkit.getPlayer(args[1]);
                if (inviter == null) {
                    player.sendMessage(TextUtil.errorMessage("Inviter not found."));
                    return true;
                }
                if (!jewManager.isJew(inviter)) {
                    player.sendMessage(TextUtil.errorMessage("That player is not a Jew."));
                    return true;
                }
                startConversion(player, inviter);
                return true;
            }

            case "decline" -> {
                if (args.length < 2) {
                    player.sendMessage(TextUtil.errorMessage("Usage: /jew decline <player>"));
                    return true;
                }
                Player inviter = Bukkit.getPlayer(args[1]);
                if (inviter != null) {
                    inviter.sendMessage(TextUtil.errorMessage(player.getName() + " declined your invitation."));
                }
                player.sendMessage(TextUtil.infoMessage("You declined the invitation."));
                return true;
            }

            default -> {
                showStatus(player, player);
            }
        }
        return true;
    }

    private void showStatus(Player viewer, Player target) {
        if (!jewManager.isJew(target.getUniqueId())) {
            viewer.sendMessage(TextUtil.errorMessage(target.getName() + " is not a Jew."));
            return;
        }

        JewPlayer jew = jewManager.getJew(target.getUniqueId());
        String shabbatTime = plugin.getShabbatManager().getShabbatTimeRemaining();
        
        String actionBarMsg = ActionBarUtil.formatShabbatStatus(jew.getPiety(), jew.getLevelName(), shabbatTime);
        ActionBarUtil.sendActionBar(target, actionBarMsg);

        viewer.sendMessage(Component.text("=== " + target.getName() + "'s Status ===", NamedTextColor.GOLD));
        viewer.sendMessage(Component.text("Level: " + jew.getLevel() + " (" + jew.getLevelName() + ")", NamedTextColor.AQUA));
        viewer.sendMessage(Component.text("Piety: " + jew.getPiety() + "/100", NamedTextColor.AQUA));
        
        if (jew.getConvertedBy() != null) {
            viewer.sendMessage(Component.text("Converted by: " + jew.getConvertedBy(), NamedTextColor.GRAY));
        }
        
        if (plugin.getShabbatManager().isShabbat()) {
            viewer.sendMessage(Component.text("It is currently Shabbat!", NamedTextColor.YELLOW));
        } else {
            viewer.sendMessage(Component.text("Next Shabbat in: " + shabbatTime, NamedTextColor.YELLOW));
        }
    }

    private void startConversion(Player target, Player inviter) {
        Location targetStart = target.getLocation().clone();
        Location inviterStart = inviter.getLocation().clone();
        
        inviter.sendMessage(TextUtil.infoMessage("Starting conversion ritual... Stand still for 10 seconds!"));
        target.sendMessage(TextUtil.infoMessage("Starting conversion ritual... Stand still for 10 seconds!"));
        
        ActionBarUtil.sendCountdown(inviter, "Mikveh ritual... ", 10);
        ActionBarUtil.sendCountdown(target, "Mikveh ritual... ", 10);
        
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!target.isOnline() || !inviter.isOnline()) {
                inviter.sendMessage(TextUtil.errorMessage("Conversion failed: player disconnected."));
                return;
            }
            
            if (target.getLocation().distance(targetStart) > 0.5) {
                inviter.sendMessage(TextUtil.errorMessage("Conversion failed: target moved."));
                target.sendMessage(TextUtil.errorMessage("You moved! The ritual was cancelled."));
                return;
            }
            
            if (inviter.getLocation().distance(inviterStart) > 0.5) {
                inviter.sendMessage(TextUtil.errorMessage("Conversion failed: you moved!"));
                target.sendMessage(TextUtil.errorMessage("Conversion failed: inviter moved."));
                return;
            }
            
            if (target.getLocation().distance(inviter.getLocation()) > 10) {
                inviter.sendMessage(TextUtil.errorMessage("Conversion failed: too far apart."));
                target.sendMessage(TextUtil.errorMessage("Conversion failed: too far apart."));
                return;
            }
            
            JewPlayer newJew = jewManager.makeJew(target, inviter.getName());
            JewPlayer converter = jewManager.getJew(inviter);
            converter.addPiety(plugin.getConfig().getInt("piety.conversion-gain", 15));
            converter.updateLevel();
            
            target.sendTitle("\u05D1\u05E8\u05D5\u05DA \u05D4\u05D1\u05D0", "Welcome to the covenant.", 10, 70, 20);
            inviter.sendTitle("\u05D1\u05E8\u05D5\u05DA \u05D4\u05D1\u05D0", "Welcome to the covenant.", 10, 70, 20);
            
            if (plugin.getConfig().getBoolean("broadcast-conversions", true)) {
                Bukkit.broadcast(TextUtil.infoMessage("\u2721 " + target.getName() + " has joined the covenant!"));
            }
            
        }, 200L);
    }
}
