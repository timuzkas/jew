package com.jedaiwm.commands;

import com.jedaiwm.JedaiWM;
import com.jedaiwm.managers.JewManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class JewCommandCompleter implements TabCompleter {

    private final JedaiWM plugin;
    private final JewManager jewManager;

    public JewCommandCompleter(JedaiWM plugin) {
        this.plugin = plugin;
        this.jewManager = plugin.getJewManager();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> subCommands = new ArrayList<>();
            subCommands.add("make");
            subCommands.add("remove");
            subCommands.add("status");
            subCommands.add("list");
            subCommands.add("convert");
            subCommands.add("accept");
            subCommands.add("decline");

            if (sender.hasPermission("jedaiwm.admin")) {
                for (String sub : subCommands) {
                    if (sub.toLowerCase().startsWith(args[0].toLowerCase())) {
                        completions.add(sub);
                    }
                }
            } else {
                if (jewManager.isJew((Player) sender)) {
                    for (String sub : new String[]{"convert", "accept", "decline", "status"}) {
                        if (sub.toLowerCase().startsWith(args[0].toLowerCase())) {
                            completions.add(sub);
                        }
                    }
                }
            }
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();

            if (subCommand.equals("make") || subCommand.equals("remove") || 
                subCommand.equals("status") || subCommand.equals("convert") ||
                subCommand.equals("accept") || subCommand.equals("decline")) {

                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(player.getName());
                    }
                }
            }
        }

        return completions;
    }
}
