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

public class TabCompleters {

    public static class PrayCompleter implements TabCompleter {
        @Override
        public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
            return new ArrayList<>();
        }
    }

    public static class TorahCompleter implements TabCompleter {
        @Override
        public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
            return new ArrayList<>();
        }
    }

    public static class TeshuvahCompleter implements TabCompleter {
        @Override
        public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
            return new ArrayList<>();
        }
    }

    public static class GefilteFishCompleter implements TabCompleter {
        @Override
        public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
            return new ArrayList<>();
        }
    }
}
