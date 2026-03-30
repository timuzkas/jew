package com.jedaiwm.utils;

import com.jedaiwm.JedaiWM;
import org.bukkit.entity.Player;

public class ActionBarUtil {

    public static void sendActionBar(Player player, String message) {
        ActionBarQueue.send(player, message, ActionBarQueue.PRIORITY_AMBIENT, 30);
    }

    public static void sendCountdown(Player player, String prefix, int totalSeconds) {
        ActionBarQueue.countdown(player, prefix, ActionBarQueue.PRIORITY_RITUAL, totalSeconds);
    }

    public static void sendFlashingActionBar(Player player, String messageA, String messageB,
                                              int intervalTicks, int durationTicks) {
        ActionBarQueue.flash(player, messageA, messageB,
            ActionBarQueue.PRIORITY_SIN, intervalTicks, durationTicks);
    }

    public static void sendTypewriterActionBar(Player player, String message, int ticksPerChar) {
        ActionBarQueue.typewriter(player, message,
            ActionBarQueue.PRIORITY_INFO, ticksPerChar, 40);
    }

    public static void sendPersistentActionBar(Player player, String message, int durationTicks) {
        ActionBarQueue.send(player, message, ActionBarQueue.PRIORITY_AMBIENT, durationTicks);
    }

    public static String formatPietyStatus(int piety, String levelName) {
        return "\u2721 Piety: " + piety + " | Level: " + levelName;
    }

    public static String formatShabbatStatus(int piety, String levelName, String shabbatTime) {
        String weekday = JedaiWM.getInstance().getShabbatManager().getCurrentWeekday();
        if (JedaiWM.getInstance().getShabbatManager().isShabbat()) {
            return "\uD83D\uDD4F " + weekday + " | Shabbat | Piety: " + piety;
        }
        return "\u2721 " + weekday + " | Piety: " + piety + " | Level: " + levelName
            + " | \uD83D\uDD4F in: " + shabbatTime;
    }
}
