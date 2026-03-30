package com.jedaiwm.utils;

import com.jedaiwm.JedaiWM;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ActionBarQueue {

    public static final int PRIORITY_AMBIENT = 0;
    public static final int PRIORITY_INFO = 1;
    public static final int PRIORITY_RITUAL = 2;
    public static final int PRIORITY_SIN = 3;
    public static final int PRIORITY_DIVINE = 4;

    private record ActiveBar(int priority, BukkitTask task) {}

    private static final Map<UUID, ActiveBar> activeBars = new ConcurrentHashMap<>();

    public static boolean claim(Player player, int priority) {
        UUID uuid = player.getUniqueId();
        ActiveBar current = activeBars.get(uuid);
        if (current != null) {
            if (priority < current.priority()) return false;
            current.task().cancel();
        }
        return true;
    }

    public static void register(Player player, int priority, BukkitTask task) {
        activeBars.put(player.getUniqueId(), new ActiveBar(priority, task));
    }

    public static void release(Player player) {
        activeBars.remove(player.getUniqueId());
    }

    public static void send(Player player, String message, int priority, int durationTicks) {
        if (!claim(player, priority)) return;
        JedaiWM plugin = JedaiWM.getInstance();

        BukkitTask[] taskRef = new BukkitTask[1];
        taskRef[0] = new BukkitRunnable() {
            int elapsed = 0;
            @Override public void run() {
                if (!player.isOnline() || elapsed >= durationTicks) {
                    release(player);
                    cancel();
                    return;
                }
                raw(player, message);
                elapsed += 10;
            }
        }.runTaskTimer(plugin, 0L, 10L);
        register(player, priority, taskRef[0]);
    }

    public static void typewriter(Player player, String message, int priority,
                                   int ticksPerChar, int holdTicks) {
        if (!claim(player, priority)) return;
        JedaiWM plugin = JedaiWM.getInstance();

        BukkitTask[] taskRef = new BukkitTask[1];
        taskRef[0] = new BukkitRunnable() {
            int index = 1;
            int holdElapsed = 0;
            boolean holding = false;

            @Override public void run() {
                if (!player.isOnline()) { release(player); cancel(); return; }

                if (!holding) {
                    raw(player, message.substring(0, index));
                    if (index >= message.length()) {
                        holding = true;
                    } else {
                        index++;
                    }
                } else {
                    raw(player, message);
                    holdElapsed += ticksPerChar;
                    if (holdElapsed >= holdTicks) {
                        release(player);
                        cancel();
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, ticksPerChar);
        register(player, priority, taskRef[0]);
    }

    public static void countdown(Player player, String prefix, int priority, int totalSeconds) {
        if (!claim(player, priority)) return;
        JedaiWM plugin = JedaiWM.getInstance();

        BukkitTask[] taskRef = new BukkitTask[1];
        taskRef[0] = new BukkitRunnable() {
            int remaining = totalSeconds;

            @Override public void run() {
                if (!player.isOnline() || remaining <= 0) {
                    release(player);
                    cancel();
                    return;
                }
                int filled = totalSeconds - remaining + 1;
                int shown = Math.min(totalSeconds, 10);
                int fs = Math.min(filled, shown);
                int es = shown - fs;
                StringBuilder sb = new StringBuilder(prefix);
                for (int i = 0; i < fs; i++) sb.append("●");
                for (int i = 0; i < es; i++) sb.append("○");
                raw(player, sb.toString());
                remaining--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
        register(player, priority, taskRef[0]);
    }

    public static void flash(Player player, String msgA, String msgB,
                              int priority, int intervalTicks, int durationTicks) {
        if (!claim(player, priority)) return;
        JedaiWM plugin = JedaiWM.getInstance();

        BukkitTask[] taskRef = new BukkitTask[1];
        taskRef[0] = new BukkitRunnable() {
            int elapsed = 0;
            boolean toggle = false;

            @Override public void run() {
                if (!player.isOnline() || elapsed >= durationTicks) {
                    release(player);
                    cancel();
                    return;
                }
                raw(player, toggle ? msgB : msgA);
                toggle = !toggle;
                elapsed += intervalTicks;
            }
        }.runTaskTimer(plugin, 0L, intervalTicks);
        register(player, priority, taskRef[0]);
    }

    static void raw(Player player, String message) {
        Component c = LegacyComponentSerializer.legacyAmpersand().deserialize(message);
        player.sendActionBar(c);
    }

    public static boolean isRitualActive(Player player) {
        ActiveBar bar = activeBars.get(player.getUniqueId());
        return bar != null && bar.priority() >= PRIORITY_RITUAL;
    }

    public static int currentPriority(Player player) {
        ActiveBar bar = activeBars.get(player.getUniqueId());
        return bar == null ? -1 : bar.priority();
    }
}
