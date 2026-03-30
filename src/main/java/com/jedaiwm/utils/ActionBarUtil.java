package com.jedaiwm.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;

public class ActionBarUtil {

    public static void sendActionBar(Player player, String message) {
        Component component = LegacyComponentSerializer.legacyAmpersand().deserialize(message);
        player.sendActionBar(component);
    }

    public static void sendActionBar(Player player, String message, int duration) {
        sendActionBar(player, message);
    }

    public static void sendCountdown(Player player, String prefix, int totalSeconds) {
        new Thread(() -> {
            for (int i = totalSeconds; i > 0; i--) {
                StringBuilder sb = new StringBuilder(prefix);
                int filled = totalSeconds - i + 1;
                int empty = i - 1;
                for (int j = 0; j < filled && j < 10; j++) sb.append("\u25CF");
                for (int j = 0; j < empty && j < 10; j++) sb.append("\u25CB");
                sb.append(" ").append(i);
                
                final String msg = sb.toString();
                if (player.isOnline()) {
                    sendActionBar(player, msg);
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }).start();
    }

    public static String formatPietyStatus(int piety, String levelName) {
        return "\u2721 Piety: " + piety + " | Level: " + levelName;
    }

    public static String formatShabbatStatus(int piety, String levelName, String shabbatTime) {
        if (com.jedaiwm.JedaiWM.getInstance().getShabbatManager().isShabbat()) {
            return "\uD83D\uDD4F Shabbat | Piety: " + piety + " | Rest. Do not labor.";
        }
        return "\uD83D\uDD4F Piety: " + piety + " | Level: " + levelName + " | \uD83D\uDD4F Shabbat in: " + shabbatTime;
    }
}
