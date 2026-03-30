package com.jedaiwm.utils;

import com.jedaiwm.JedaiWM;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ActionBarUtil {

    public static void sendActionBar(Player player, String message) {
        Component component = LegacyComponentSerializer.legacyAmpersand().deserialize(message);
        player.sendActionBar(component);
    }

    public static void sendActionBar(Player player, String message, int duration) {
        sendActionBar(player, message);
    }

    public static void sendCountdown(Player player, String prefix, int totalSeconds) {
        JedaiWM plugin = JedaiWM.getInstance();
        new BukkitRunnable() {
            int remaining = totalSeconds;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }
                if (remaining <= 0) {
                    cancel();
                    return;
                }
                int filled = totalSeconds - remaining + 1;
                int empty = remaining - 1;
                StringBuilder sb = new StringBuilder(prefix);
                int shown = Math.min(totalSeconds, 10);
                int filledShown = Math.min(filled, shown);
                int emptyShown = shown - filledShown;
                for (int i = 0; i < filledShown; i++) sb.append("\u25CF");
                for (int i = 0; i < emptyShown; i++) sb.append("\u25CB");
                sendActionBar(player, sb.toString());
                remaining--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public static void sendFlashingActionBar(Player player, String messageA, String messageB,
                                              int intervalTicks, int durationTicks) {
        JedaiWM plugin = JedaiWM.getInstance();
        new BukkitRunnable() {
            int elapsed = 0;
            boolean toggle = false;

            @Override
            public void run() {
                if (!player.isOnline() || elapsed >= durationTicks) {
                    cancel();
                    return;
                }
                sendActionBar(player, toggle ? messageB : messageA);
                toggle = !toggle;
                elapsed += intervalTicks;
            }
        }.runTaskTimer(plugin, 0L, intervalTicks);
    }

    public static void sendTypewriterActionBar(Player player, String message, int ticksPerChar) {
        JedaiWM plugin = JedaiWM.getInstance();
        new BukkitRunnable() {
            int index = 1;

            @Override
            public void run() {
                if (!player.isOnline() || index > message.length()) {
                    if (player.isOnline()) sendActionBar(player, message);
                    cancel();
                    return;
                }
                sendActionBar(player, message.substring(0, index));
                index++;
            }
        }.runTaskTimer(plugin, 0L, ticksPerChar);
    }

    public static void sendPersistentActionBar(Player player, String message, int durationTicks) {
        JedaiWM plugin = JedaiWM.getInstance();
        new BukkitRunnable() {
            int elapsed = 0;

            @Override
            public void run() {
                if (!player.isOnline() || elapsed >= durationTicks) {
                    cancel();
                    return;
                }
                sendActionBar(player, message);
                elapsed += 10;
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }

    public static String formatPietyStatus(int piety, String levelName) {
        return "\u2721 Piety: " + piety + " | Level: " + levelName;
    }

    public static String formatShabbatStatus(int piety, String levelName, String shabbatTime) {
        if (JedaiWM.getInstance().getShabbatManager().isShabbat()) {
            return "\uD83D\uDD4F Shabbat | Piety: " + piety + " | Rest. Do not labor.";
        }
        return "\u2721 Piety: " + piety + " | Level: " + levelName + " | \uD83D\uDD4F Shabbat in: " + shabbatTime;
    }
}
